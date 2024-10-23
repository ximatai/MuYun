package net.ximatai.muyun.fileserver;

import java.io.File;

public interface IFileService {
    String save(File file, String originFileName);

    File get(String id);

    boolean delete(String id);
    
    FileInfoEntity info(String id);
}
