package de.micromata.borgbutler.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class DirUtils {
    public static void zipDirectory(Path srcPath, Path outputFile) throws IOException {
        List<Path> files = listFiles(srcPath);
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile.toFile()))) {
            for (Path path : files) {
                ZipEntry entry = new ZipEntry(path.toString());
                zipOutputStream.putNextEntry(entry);
               // FileInputStream in = new FileInputStream(Paths.get(rootDir, sourceDir, file.getName()).toString());
               // IOUtils.copy(in, out);
               // IOUtils.closeQuietly(in);
            }
        }
    }

    public static List<Path> listFiles(Path path) throws IOException {
        return listFiles(new ArrayList<>(), path);
    }

    private static List<Path> listFiles(List<Path> files, Path path) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    listFiles(files, entry);
                }
                files.add(entry);
            }
        }
        return files;
    }
}