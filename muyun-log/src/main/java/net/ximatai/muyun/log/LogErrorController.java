package net.ximatai.muyun.log;

import jakarta.ws.rs.Path;
import net.ximatai.muyun.service.ILogError;

@Path("/log/error")
public class LogErrorController extends LogBaseController implements ILogError {

    @Override
    public String getMainTable() {
        return "log_error";
    }

}
