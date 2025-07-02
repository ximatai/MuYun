package net.ximatai.muyun.platform.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.CookieSameSite;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import net.ximatai.muyun.MuYunConst;
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.core.exception.MuYunException;
import net.ximatai.muyun.model.ApiRequest;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.platform.model.LoginUser;
import net.ximatai.muyun.platform.model.RuntimeUser;
import net.ximatai.muyun.util.StringUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@Path("/sso")
@Tag(description = "登录相关")
public class SsoController implements IRuntimeAbility {

    private final Logger logger = LoggerFactory.getLogger(SsoController.class);

    private static final String ALL_PURPOSE_CODE_FOR_DEBUG = "muyun";
    private static final String KAPTCHA_COOKIE_KEY = "KCODE";

    protected Cache<String, String> codeCache = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .maximumSize(100)
        .build();

    // 锁定用户记录在此缓存
    protected Cache<String, LocalDateTime> lockUser = Caffeine.newBuilder()
        .build();

    // 登录失败的历次时间戳缓存
    protected Cache<String, Queue<Long>> loginFailTimestamps = Caffeine.newBuilder()
        .expireAfterAccess(10, TimeUnit.MINUTES) // 避免内存泄漏
        .build();

    @Inject
    UserController userController;

    @Inject
    UserInfoController userInfoController;

    @Inject
    RoutingContext routingContext;

    @Inject
    MuYunConfig config;

    @GET
    @Path("/login")
    @Operation(summary = "登录")
    public IRuntimeUser login(@QueryParam("username") String username, @QueryParam("password") String password, @QueryParam("code") String code) {
        ApiRequest apiRequest = getApiRequest();

        apiRequest.setModuleName(MuYunConst.SSO_MODULE_NAME);
        apiRequest.setActionName("登录");

        if (StringUtil.isBlank(username)) {
            throw new MuYunException("请输入用户名");
        }

        if (StringUtil.isBlank(password)) {
            throw new MuYunException("请输入密码");
        }

        if (StringUtil.isBlank(code)) {
            throw new MuYunException("请输入验证码");
        }

        apiRequest.setUsername(username);

        try {
            verificationCode(code);
        } catch (MuYunException e) {
            throw loginFail(username, "验证码错误", true, false);
        }

        LocalDateTime lockUserDateTime = lockUser.getIfPresent(username);

        if (lockUserDateTime != null && lockUserDateTime.isAfter(LocalDateTime.now())) {
            throw loginFail(username, "登录失败次数太多已被锁定，将于 %s 解锁".formatted(
                lockUserDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ), true, false);
        }

        PageResult pageResult = userController.query(Map.of("v_username", username));

        if (pageResult.getSize() == 0) {
            throw loginFail(username, "用户不存在，用户名：" + username, false, true);
        }

        Map userInDB = (Map) pageResult.getList().getFirst();
        String vPassword = userInDB.get("v_password").toString();
        String encryptedPassword = encryptPassword(vPassword, code);
        if (password.equals(encryptedPassword)
            || (!isProdMode() && password.equals(vPassword))) { // 非生产环境允许使用非加密密码

            // 非管理员要判断账号有效期
            if (!config.isSuperUser((String) userInDB.get("id")) && config.userValidateDays() > 0) {
                java.sql.Date invalidDate = (java.sql.Date) userInDB.get("d_invalid");

                if (invalidDate != null) {
                    LocalDate now = LocalDate.now();
                    LocalDate invalidLocalDate = invalidDate.toLocalDate();

                    if (now.isAfter(invalidLocalDate)) {
                        throw loginFail(username, "该账号已超过有效期，用户名：" + username, true, false);
                    }
                }
            }

            if ((boolean) userInDB.get("b_enabled")) {
                Map<String, ?> user = userInfoController.view((String) userInDB.get("id"));
                IRuntimeUser runtimeUser = mapToUser(user);

                if (config.useSession()) {
                    Session session = getRoutingContext().session();
                    if (session != null) {
                        session.put(MuYunConst.SESSION_USER_KEY, runtimeUser);
                    }
                }

                userController.checkIn((String) user.get("id"));
                unlockUser(username);
                return runtimeUser;
            } else {
                throw loginFail(username, "用户已停用，用户名：" + username, false, true);
            }
        } else {
            throw loginFail(username, "用户密码验证失败，用户名：" + username, false, true);
        }

    }

    /**
     * @param username
     * @param reason
     * @param openReason 是否对前端公开失败原因
     * @param isRecord   是否参与失败次数记录
     * @return 登录失败异常
     */
    protected MuYunException loginFail(String username, String reason, boolean openReason, boolean isRecord) {
        ApiRequest apiRequest = getApiRequest();
        logger.error(reason);
        apiRequest.setError(new RuntimeException(reason));
        int recentFailures = 0;

        int userFailureMaxCount = config.userFailureMaxCount();
        int userFailureLockMin = config.userFailureLockMin();

        if (userFailureMaxCount > 0 && userFailureLockMin > 0 && isRecord) {
            recordLoginFailure(username);

            recentFailures = getRecentFailures(username);

            if (recentFailures >= userFailureMaxCount) {
                LocalDateTime openTime = LocalDateTime.now().plusMinutes(userFailureLockMin);
                lockUser.put(username, openTime);
                return new MuYunException(
                    "登录失败次数太多已被锁定，将于 %s 解锁".formatted(openTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                );
            }

            return new MuYunException("用户名或密码错误，还有 %s 次重试机会".formatted(userFailureMaxCount - recentFailures));
        } else if (openReason) {
            return new MuYunException(reason);
        } else {
            return new MuYunException("用户名或密码错误");
        }

    }

    public void unlockUser(String username) {
        lockUser.invalidate(username);
        loginFailTimestamps.invalidate(username);
    }

    // 用户登录失败时，记录当前时间戳
    private void recordLoginFailure(String username) {
        loginFailTimestamps.asMap().compute(username, (key, queue) -> {
            if (queue == null) {
                queue = new ConcurrentLinkedQueue<>();
            }
            queue.add(System.currentTimeMillis());
            return queue;
        });
    }

    // 获取最近60秒内的登录失败次数
    private int getRecentFailures(String username) {
        Queue<Long> timestamps = loginFailTimestamps.getIfPresent(username);
        if (timestamps == null) {
            return 0;
        }

        long now = System.currentTimeMillis();
        long cutoff = now - 60_000; // 60秒前的时间戳

        // 移除过期的记录（60秒前的）
        while (!timestamps.isEmpty() && timestamps.peek() < cutoff) {
            timestamps.poll();
        }

        return timestamps.size();
    }

    private String encryptPassword(String password, String code) {
        String md5Password = DigestUtils.md5Hex(password).toUpperCase();
        return DigestUtils.md5Hex(md5Password + code).toUpperCase();
    }

    private void verificationCode(String code) throws MuYunException {
        code = code.trim().toLowerCase();
        if (!isProdMode() && ALL_PURPOSE_CODE_FOR_DEBUG.equals(code)) { // 非生产环境允许万能验证码
            return;
        }

        Cookie cookie = routingContext.request().getCookie(KAPTCHA_COOKIE_KEY);
        if (cookie == null) {
            throw new MuYunException("验证码已过期");
        }

        String hashCodeInCookie = cookie.getValue();

        if (hashCodeInCookie.equals(hashText(code))) {
            if (codeCache.getIfPresent(code) != null) {
                throw new MuYunException("验证码已过期");
            }

            codeCache.put(code, code);
        } else {
            throw new MuYunException("验证码不正确");
        }
    }

    @POST
    @Path("/login")
    @Operation(summary = "登录")
    public IRuntimeUser login(LoginUser user) {
        return login(user.username(), user.password(), user.code());
    }

    @GET
    @Path("/logout")
    @Operation(summary = "退出")
    public boolean logout() {
        ApiRequest apiRequest = getApiRequest();
        apiRequest.setModuleName(MuYunConst.SSO_MODULE_NAME);
        apiRequest.setActionName("退出");

        if (config.useSession()) {
            Session session = getRoutingContext().session();
            if (session != null) {
                session.destroy();
            }
        }

        return true;
    }

    @GET
    @Path("/kaptcha")
    @Operation(summary = "获取验证码")
    public Response kaptcha() {
        var response = routingContext.response();  // 这里改用 Inject 的 routingContext

        // 生成验证码
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);
        specCaptcha.setCharType(Captcha.TYPE_ONLY_UPPER);
        String text = specCaptcha.text().toLowerCase();

        // 生成 MD5
        response.addCookie(Cookie.cookie(KAPTCHA_COOKIE_KEY, hashText(text)).setHttpOnly(true).setPath("/").setSameSite(CookieSameSite.STRICT));

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            specCaptcha.out(os);  // 将图片写入到 ByteArrayOutputStream
            byte[] captchaBytes = os.toByteArray();

            // 使用 Response 构建响应
            return Response.ok(captchaBytes)
                .header("Content-Type", "image/gif")
                .header("Pragma", "No-cache")
                .header("Cache-Control", "no-cache")
                .header("Expires", "0")
                .build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error generating captcha")
                .build();
        }
    }

    private String hashText(String text) {
        return DigestUtils.sha256Hex(text + "MuYun").substring(0, 16);
    }

    private IRuntimeUser mapToUser(Map user) {
        return new RuntimeUser()
            .setUsername((String) user.get("v_username"))
            .setId((String) user.get("id"))
            .setName((String) user.get("v_name"))
            .setDepartmentId((String) user.get("id_at_org_department"))
            .setOrganizationId((String) user.get("id_at_org_organization"));
    }
}
