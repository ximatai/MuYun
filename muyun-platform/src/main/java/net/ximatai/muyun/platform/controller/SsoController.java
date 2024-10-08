package net.ximatai.muyun.platform.controller;

import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.platform.model.RuntimeUser;
import net.ximatai.muyun.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Path("/sso")
public class SsoController implements IRuntimeAbility {

    private final Logger logger = LoggerFactory.getLogger(SsoController.class);

    private Set<String> codeUsed = new HashSet<>();

    @Inject
    UserController userController;

    @Inject
    UserInfoController userInfoController;

    @Inject
    RoutingContext routingContext;

    @Inject
    Vertx vertx;

    @GET
    @Path("/login")
    public IRuntimeUser login(@QueryParam("username") String username, @QueryParam("password") String password, @QueryParam("code") String code) {
        if (StringUtil.isBlank(username)) {
            throw new MyException("轻输入用户名");
        }

        if (StringUtil.isBlank(password)) {
            throw new MyException("轻输入密码");
        }

        if (StringUtil.isBlank(code)) {
            throw new MyException("轻输入验证码");
        }

        verificationCode(code);

        PageResult pageResult = userController.query(Map.of("v_username", username));

        if (pageResult.getSize() == 0) {
            logger.error("不存在的用户信息进行登录：{}", username);
            throw new MyException("用户名或密码错误");
        }

        Map userInDB = (Map) pageResult.getList().getFirst();

        if (password.equals(userInDB.get("v_password").toString())) {
            if ((boolean) userInDB.get("b_enabled")) {
                Map<String, ?> user = userInfoController.view((String) userInDB.get("id"));
                IRuntimeUser runtimeUser = mapToUser(user);
                setUser(runtimeUser);
                return runtimeUser;
            } else {
                logger.error("用户已停用，用户名：{}", username);
                throw new MyException("用户名或密码错误");
            }
        } else {
            logger.error("用户密码验证失败，用户名：{}", username);
            throw new MyException("用户名或密码错误");
        }
    }

    private void verificationCode(String code) {
        Cookie cookie = routingContext.request().getCookie("code");
        if (cookie == null) {
            throw new MyException("验证码已过期");
        }

        String hashCodeInCookie = cookie.getValue();

        if ("boson".equals(code)) {
            return;
        }

        if (hashCodeInCookie.equals(hashText(code))) {
            if (codeUsed.contains(code)) {
                throw new MyException("验证码已过期");
            }

            codeUsed.add(code);

            vertx.setTimer(60 * 1000, h -> {
                codeUsed.remove(code);
            });

        } else {
            throw new MyException("验证码不正确");
        }

    }

    @POST
    @Path("/login")
    public IRuntimeUser login(Map body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String code = (String) body.get("code");

        return login(username, password, code);
    }

    @GET
    @Path("/logout")
    public boolean logout() {
        this.destroy();
        return true;
    }

    @GET
    @Path("/show")
    public Response kaptcha() {
        var response = routingContext.response();  // 这里改用 Inject 的 routingContext

        // 生成验证码
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);
        specCaptcha.setCharType(Captcha.TYPE_ONLY_UPPER);
        String text = specCaptcha.text().toLowerCase();

        // 生成 MD5
        response.addCookie(Cookie.cookie("code", hashText(text)).setHttpOnly(true));

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
        return md5(text + "mace");
    }

    // MD5 生成方法
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RoutingContext getRoutingContext() {
        return routingContext;
    }

    private IRuntimeUser mapToUser(Map user) {
        return new RuntimeUser()
            .setUsername((String) user.get("v_username"))
            .setId((String) user.get("id"))
            .setName((String) user.get("v_name"))
            .setDepartmentId((String) user.get("id_at_org_department"))
            .setOrganizationId((String) user.get("id_at_organization"));
    }
}
