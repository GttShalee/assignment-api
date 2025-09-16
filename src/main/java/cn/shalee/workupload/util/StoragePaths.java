package cn.shalee.workupload.util;

import org.springframework.boot.system.ApplicationHome;

import java.io.File;
import java.nio.file.Path;

public final class StoragePaths {
    private StoragePaths() {}

    public static Path getUploadsBasePath() {
        ApplicationHome home = new ApplicationHome(StoragePaths.class);
        File homeDir = home.getDir();
        return new File(homeDir, "uploads").toPath();
    }
}





