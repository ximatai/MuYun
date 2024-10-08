package net.ximatai.muyun.platform.controller;

import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.platform.model.RuntimeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

@Path("/sso")
public class SsoController implements IRuntimeAbility {

    private final Logger logger = LoggerFactory.getLogger(SsoController.class);

    @Inject
    UserController userController;

    @Inject
    UserInfoController userInfoController;

    @Inject
    RoutingContext routingContext;

    @GET
    @Path("/login")
    public IRuntimeUser login(@QueryParam("username") String username, @QueryParam("password") String password) {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

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

    @POST
    @Path("/login")
    public IRuntimeUser login(Map body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");

        return login(username, password);
    }

    @GET
    @Path("/logout")
    public boolean logout() {
        this.destroy();
        return true;
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
