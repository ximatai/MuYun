package net.ximatai.muyun.log;

import jakarta.ws.rs.Path;
import net.ximatai.muyun.service.ILogLogin;

@Path("/log/login")
public class LogLoginController extends LogBaseController implements ILogLogin {

    @Override
    public String getMainTable() {
        return "log_login";
    }

}
