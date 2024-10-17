package net.ximatai.muyun.service;

import net.ximatai.muyun.model.log.LogAccessItem;

public interface ILogService {

    void logAccess(LogAccessItem logItem);

    void logLogin(LogAccessItem logItem);

}
