package com.alexandre.proxy.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class MultiProcessingUtils {

    public static ProcessBuilder makeProcess(String processName, File workingDir, String... args) throws IOException {
        ArrayList<String> cmd = new ArrayList<>();
        cmd.add("screen");
        cmd.add("-dmS");
        cmd.add(processName.replace(" ", "_"));
        cmd.add(getJavaDir().toString());
        cmd.add("-Xmx1024M");
        cmd.add("-jar");
        cmd.add(new File(workingDir, "server.jar").getAbsolutePath());
        cmd.addAll(Arrays.asList(args));

        return new ProcessBuilder(cmd).directory(workingDir);
    }

    public static Process startProcess(String processName, File workingDir, String... args) throws IOException {
        return makeProcess(processName, workingDir, args).inheritIO().start();
    }

    public static Process startProcessWithIO(String processName, File workingDir, String... args) throws IOException {
        return makeProcess(processName, workingDir, args).start();
    }

    private static Path getJavaDir() {
        return Paths.get(System.getProperty("java.home"), "bin", "java");
    }

}