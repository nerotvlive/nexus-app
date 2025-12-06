package com.zyneonstudios.nexus.application.utilities;

import com.zyneonstudios.nexus.application.Main;
import com.zyneonstudios.nexus.utilities.file.FileActions;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ApplicationMigrator {

    public static String getOldApplicationPath(boolean ancient) {
        String folderName;
        if (ancient) {
            folderName = "Zyneon/Application";
        } else {
            folderName = "Zyneon/NEXUS App";
        }
        String appData;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            appData = System.getenv("LOCALAPPDATA");
        } else if (os.contains("mac")) {
            appData = System.getProperty("user.home") + "/Library/Application Support";
        } else {
            appData = System.getProperty("user.home") + "/.local/share";
        }
        Path folderPath = Paths.get(appData, folderName);
        try {
            Files.createDirectories(folderPath);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return (folderPath + "/").replace("\\", "/");
    }

    public static String[] getOldMinecraftInstances() {
        String path = getOldApplicationPath(false);
        try {
            File zyndexFile = new File(path + "modules/nexus-minecraft-module/zyndex/index.json");
            if (zyndexFile.exists()) {
                JsonStorage zyndex = new JsonStorage(zyndexFile);
                zyndexFile = null;
                System.gc();
                if (zyndex.has("instances")) {
                    ArrayList<String> instances = (ArrayList<String>) zyndex.get("instances");
                    if (!instances.isEmpty()) {
                        String[] instanceArray = new String[instances.size()];
                        for (int i = 0; i < instances.size(); i++) {
                            instanceArray[i] = instances.get(i);
                        }
                        zyndex = null;
                        System.gc();
                        FileActions.deleteFolder(new File(getOldApplicationPath(false)+"modules/nexus-minecraft-module"));
                        return instanceArray;
                    }
                }
                zyndex = null;
                System.gc();
                FileActions.deleteFolder(new File(getOldApplicationPath(false)+"modules/nexus-minecraft-module"));
            }
        } catch (Exception e) {
            Main.getLogger().printErr("NEXUS", "MIGRATOR", "Couldn't read old Minecraft instances.", e.getMessage(), e.getStackTrace());
        }
        return null;
    }

    public static void migrateFolder() {
        //Deleting ancient (very old) application 1.X folder, due to it being deprecated and migrating is over (> 2 years).
        //FileActions.deleteFolder(new File(getOldApplicationPath(true)));

        //Trying to migrate NEXUS Application 2.X to 3.X
        try {
            File appFolder = new File(getOldApplicationPath(false));
            if(!appFolder.exists()) {
                if(!appFolder.mkdirs()) {
                    throw new RuntimeException("Could not create path " + getOldApplicationPath(false));
                } else {
                    return;
                }
            }
            if(new File(appFolder.getAbsolutePath()+"/libraries/").exists()) {
                FileActions.deleteFolder(new File(appFolder.getAbsolutePath()+"/libraries/"));
                File settings = new File(appFolder.getAbsolutePath()+"/config/");
                if(settings.exists()) {
                    if(settings.isDirectory()) {
                        FileActions.deleteFolder(settings);
                    }
                }
            }
        } catch (Exception e) {
            Main.getLogger().printErr("NEXUS", "MIGRATOR", "Failed to migrate NEXUS Application 2.X to 3.X", e.getMessage(), e.getStackTrace());
        }
    }
}