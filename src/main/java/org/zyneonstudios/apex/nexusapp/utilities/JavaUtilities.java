package org.zyneonstudios.apex.nexusapp.utilities;

import com.zyneonstudios.nexus.utilities.system.OperatingSystem;
import fr.flowarg.azuljavadownloader.*;
import fr.theshark34.openlauncherlib.JavaUtil;
import live.nerotv.aminecraftlauncher.launcher.MinecraftVersion;
import org.apache.commons.io.FileUtils;
import org.zyneonstudios.apex.nexusapp.main.NexusApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class JavaUtilities {

    public static String getJavaVersion(String javaPath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(javaPath + File.separator + "bin" + File.separator + "java", "-version");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("version")) {
                    if (line.contains("\"")) {
                        String version = line.split("\"")[1];
                        if (version.startsWith("1.")) {
                            return version.split("\\.")[1];
                        } else {
                            return version.split("\\.")[0];
                        }
                    }
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException ignore) {}
        return null;
    }

    public static boolean installJava(String version, String path) {
        File tempFolder = new File(NexusApplication.getInstance().getWorkingPath()+"/temp/");
        AzulJavaDownloader downloader = new AzulJavaDownloader();

        try {
            if(!tempFolder.exists()) {
                tempFolder.mkdirs();
            }

            AzulJavaArch arch;
            if(OperatingSystem.getArchitecture().equals(OperatingSystem.Architecture.arm64)) {
                arch = AzulJavaArch.AARCH64;
            } else {
                arch = AzulJavaArch.X64;
            }

            AzulJavaBuildInfo buildInfo;
            if(OperatingSystem.getType().equals(OperatingSystem.Type.Windows)) {
                buildInfo = downloader.getBuildInfo(new RequestedJavaInfo(version, AzulJavaType.JRE, AzulJavaOS.WINDOWS, arch));
            } else if(OperatingSystem.getType().equals(OperatingSystem.Type.macOS)) {
                buildInfo = downloader.getBuildInfo(new RequestedJavaInfo(version, AzulJavaType.JRE, AzulJavaOS.MACOS, arch));
            } else {
                buildInfo = downloader.getBuildInfo(new RequestedJavaInfo(version, AzulJavaType.JRE, AzulJavaOS.LINUX, arch));
            }
            Path javaPath = downloader.downloadAndInstall(buildInfo, tempFolder.toPath());

            if(path.equals("default")) {
                switch (version) {
                    case "8" -> path = NexusApplication.getInstance().getLocalSettings().getJava8Path();
                    case "17" -> path = NexusApplication.getInstance().getLocalSettings().getJava17Path();
                    case "21" -> path = NexusApplication.getInstance().getLocalSettings().getJava21Path();
                }
            }

            if(!path.equals("default")) {
                File dest = new File(path);
                FileUtils.moveDirectory(javaPath.toFile(), dest);
                fixPermissions(path);
            }

            NexusApplication.getInstance().getApplicationFrame().executeJavaScript("console.log('[CONNECTOR] settings.init.java');");
            System.gc();
            return true;
        } catch (Exception e) {
            NexusApplication.getLogger().printErr("NEXUS","JAVA INSTALLER","Couldn't install Java "+version+"!",e.getMessage(), e.getStackTrace());
        }
        System.gc();
        return false;
    }

    public static void setJava(MinecraftVersion.Type type) {
        try {
            NexusApplication.getLogger().log("Detected Minecraft version type " + type + "!");
            String javaHome = System.getProperty("java.home");
            if (type.equals(MinecraftVersion.Type.LEGACY)) {
                JavaUtil.setJavaCommand(null);
                String java = NexusApplication.getInstance().getLocalSettings().getJava8Path();
                if (!new File(java).exists()) {
                    NexusApplication.getLogger().log("Couldn't find compatible Java Runtime Environment! Starting download of Java 8...");
                    installJava("8", java);
                }
                javaHome = java;
            } else if (type.equals(MinecraftVersion.Type.SEMI_NEW)) {
                JavaUtil.setJavaCommand(null);
                String java = NexusApplication.getInstance().getLocalSettings().getJava17Path();
                if (!new File(java).exists()) {
                    NexusApplication.getLogger().log("Couldn't find compatible Java Runtime Environment! Starting download of Java 17...");
                    installJava("17", java);
                }
                javaHome = java;
            } else if (type.equals(MinecraftVersion.Type.NEW)) {
                JavaUtil.setJavaCommand(null);
                String java = NexusApplication.getInstance().getLocalSettings().getJava21Path();
                if (!new File(java).exists()) {
                    NexusApplication.getLogger().log("Couldn't find compatible Java Runtime Environment! Starting download of Java 21...");
                    installJava("21", java);
                }
                javaHome = java;
            }
            javaHome = javaHome.replace("\\","/");
            if(!javaHome.endsWith("/")) {
                javaHome += "/";
            }
            System.setProperty("java.home", javaHome);
            NexusApplication.getLogger().log("Set Java Home to: " + javaHome);
        } catch (Exception e) {
            NexusApplication.getLogger().err("Couldn't set java version: "+e.getMessage());
        }
    }

    private static void fixPermissions(String path) {
        try {
            if(!OperatingSystem.getType().equals(OperatingSystem.Type.Windows)) {
                ProcessBuilder pb = new ProcessBuilder("chmod", "-R", "+x", path);
                Process process = pb.start();
                int exitCode = process.waitFor();
                if(exitCode != 0) {
                    throw new RuntimeException("Failed to execute chmod command: " + path);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}