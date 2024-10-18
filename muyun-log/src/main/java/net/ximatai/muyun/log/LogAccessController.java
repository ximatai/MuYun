package net.ximatai.muyun.log;

import jakarta.ws.rs.Path;
import net.ximatai.muyun.service.ILogAccess;

@Path("/log/access")
public class LogAccessController extends LogBaseController implements ILogAccess {

    @Override
    public String getMainTable() {
        return "log_access";
    }

}
