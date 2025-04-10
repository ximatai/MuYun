package net.ximatai.muyun.fileserver;

import net.ximatai.muyun.fileserver.exception.FileException;

import java.io.File;

public interface IFileService {
    String save(File file);

    String save(File file, String assignName);

    File get(String idOrName) throws FileException;

    boolean delete(String id);

    FileInfoEntity info(String id) throws FileException;

    // uid文件名处理方法
    default String suffixFileNameWithN(String fileName) {
        return fileName + "-n";
    }

    default String suffixFileNameWithO(String fileName) {
        return fileName + "-o";
    }
}
