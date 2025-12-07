package org.zyneonstudios.apex.nexusapp.utilities;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
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
                buildInfo = downloader.getBuildInfo(new RequestedJavaInfo(version, AzulJavaType.JDK, AzulJavaOS.WINDOWS, arch));
            } else if(OperatingSystem.getType().equals(OperatingSystem.Type.macOS)) {
                buildInfo = downloader.getBuildInfo(new RequestedJavaInfo(version, AzulJavaType.JDK, AzulJavaOS.MACOS, arch));
            } else {
                buildInfo = downloader.getBuildInfo(new RequestedJavaInfo(version, AzulJavaType.JDK, AzulJavaOS.LINUX, arch));
            }
            Path javaPath = downloader.downloadAndInstall(buildInfo, tempFolder.toPath());

            switch (version) {
                case "8" -> FileUtils.moveDirectory(javaPath.toFile(), new File(NexusApplication.getInstance().getLocalSettings().getJava8Path()));
                case "17" -> FileUtils.moveDirectory(javaPath.toFile(), new File(NexusApplication.getInstance().getLocalSettings().getJava17Path()));
                case "21" -> FileUtils.moveDirectory(javaPath.toFile(), new File(NexusApplication.getInstance().getLocalSettings().getJava21Path()));
                default -> FileUtils.moveDirectory(javaPath.toFile(), new File(path));
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

    public void setJava(MinecraftVersion.Type type) {
        NexusUtilities.getLogger().log("[LAUNCHER] Detected Minecraft version type "+type+"!");
        if(type.equals(MinecraftVersion.Type.LEGACY)) {
            JavaUtil.setJavaCommand(null);
            String java = NexusApplication.getInstance().getLocalSettings().getJava8Path();
            if(!new File(java).exists()) {
                NexusUtilities.getLogger().err("[LAUNCHER] Couldn't find compatible Java Runtime Environment!");
                installJava("8",java);
                NexusUtilities.getLogger().dbg("[LAUNCHER] Starting installation of missing java runtime 8...");
            }
            System.setProperty("java.home", java);
        } else if(type.equals(MinecraftVersion.Type.SEMI_NEW)) {
            JavaUtil.setJavaCommand(null);
            String java = NexusApplication.getInstance().getLocalSettings().getJava17Path();
            if(!new File(java).exists()) {
                NexusUtilities.getLogger().err("[LAUNCHER] Couldn't find compatible Java Runtime Environment!");
                installJava("17",java);
                NexusUtilities.getLogger().deb("[LAUNCHER] Starting installation of missing java runtime 17...");
            }
            System.setProperty("java.home", java);
        } else if(type.equals(MinecraftVersion.Type.NEW)) {
            JavaUtil.setJavaCommand(null);
            String java = NexusApplication.getInstance().getLocalSettings().getJava21Path();
            if(!new File(java).exists()) {
                NexusUtilities.getLogger().err("[LAUNCHER] Couldn't find compatible Java Runtime Environment!");
                installJava("21",java);
                NexusUtilities.getLogger().deb("[LAUNCHER] Starting installation of missing java runtime 21...");
            }
            System.setProperty("java.home", java);
        }
    }
}