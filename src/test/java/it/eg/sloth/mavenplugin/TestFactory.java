package it.eg.sloth.mavenplugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestFactory {

    public static final String OUTPUT_DIR = "target/test-output";

    public static void createOutputDir() throws IOException {
        Files.createDirectories(Paths.get(OUTPUT_DIR));
    }

    public static void createOutputDir(String relativePath) throws IOException {
        Files.createDirectories(Paths.get(OUTPUT_DIR));
        Files.createDirectories(Paths.get(OUTPUT_DIR + "/" + relativePath));
    }
}