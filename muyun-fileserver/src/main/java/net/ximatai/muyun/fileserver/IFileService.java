package net.ximatai.muyun.fileserver;

import io.vertx.core.Future;

import java.io.File;

public interface IFileService {
    String save(File file);

    String save(File file, String assignName);

    File get(String idOrName);
    
    boolean delete(String id);

    FileInfoEntity info(String id);

    Future<FileInfoEntity> asyncInfo(String id);

    // uid文件名处理方法
    default String suffixFileNameWithN(String fileName) {
        return fileName + "-n";
    }

    default String suffixFileNameWithO(String fileName) {
        return fileName + "-o";
    }
}
