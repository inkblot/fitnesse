package util;

import com.google.inject.ImplementedBy;

import java.io.IOException;

@ImplementedBy(DiskFileSystem.class)
public interface FileSystem {
    void makeFile(String path, String content) throws IOException;

    void makeDirectory(String path) throws IOException;

    boolean exists(String path);

    String[] list(String path);

    String getContent(String path) throws IOException;
}
