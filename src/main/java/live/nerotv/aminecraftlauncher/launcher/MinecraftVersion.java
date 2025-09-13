package live.nerotv.aminecraftlauncher.launcher;

import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.verget.Verget;
import com.zyneonstudios.verget.minecraft.MinecraftVerget;
import fr.theshark34.openlauncherlib.JavaUtil;
import live.nerotv.aminecraftlauncher.installer.java.Architecture;
import live.nerotv.aminecraftlauncher.installer.java.Java;
import live.nerotv.aminecraftlauncher.installer.java.JavaInstaller;
import live.nerotv.aminecraftlauncher.installer.java.OperatingSystem;

import java.io.File;
import java.util.ArrayList;

public class MinecraftVersion {

    public ArrayList<String> supportedVersions = new ArrayList<>();
    private String path;

    public MinecraftVersion(String path) {
        this.path = path;
        syncVersions();
    }

    public void syncVersions() {
        supportedVersions = Verget.getMinecraftVersions(MinecraftVerget.Filter.BOTH);
    }

    public Type getType(String version) {
        if(version.contains(".")) {
            try {
                int i = Integer.parseInt(version.split("\\.")[1]);
                if (i < 13) {
                    return Type.LEGACY;
                } else if (i < 18) {
                    return Type.SEMI_NEW;
                } else {
                    return Type.NEW;
                }
            } catch (Exception e) {
                NexusUtilities.getLogger().err("[SYSTEM] Couldn't resolve Minecraft version "+version+": "+e.getMessage());
            }
        }
        return Type.NEW;
    }

    public ForgeType getForgeType(String mcVersion) {
        if(mcVersion.contains(".")) {
            try {
                int i = Integer.parseInt(mcVersion.split("\\.")[1]);
                if (i < 12) {
                    return ForgeType.OLD;
                } else {
                    return ForgeType.NEW;
                }
            } catch (Exception e) {
                NexusUtilities.getLogger().err("[SYSTEM] Couldn't resolve Minecraft version "+mcVersion+": "+e.getMessage());
            }
        }
        return null;
    }

    public boolean isMinecraftVersion(String version) {
        try {
            return getType(version) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public enum Type {
        LEGACY,
        SEMI_NEW,
        NEW
    }

    public enum ForgeType {
        OLD,
        NEW
    }

    public void setJava(MinecraftVersion.Type type) {
        NexusUtilities.getLogger().log("[LAUNCHER] Detected Minecraft version type "+type+"!");
        if(type.equals(MinecraftVersion.Type.LEGACY)) {
            JavaUtil.setJavaCommand(null);
            String java = path+"/jre-8/";
            if(!new File(java).exists()) {
                NexusUtilities.getLogger().err("[LAUNCHER] Couldn't find compatible Java Runtime Environment!");
                JavaInstaller javaInstaller = new JavaInstaller(Java.Runtime_8, getOS(), getArchitecture(), path);
                javaInstaller.install();
                NexusUtilities.getLogger().dbg("[LAUNCHER] Starting installation of missing java runtime "+javaInstaller.getVersionString()+"...");
            }
            System.setProperty("java.home", java);
        } else if(type.equals(MinecraftVersion.Type.SEMI_NEW)) {
            JavaUtil.setJavaCommand(null);
            String java = path+"/jre-11/";
            if(!new File(java).exists()) {
                NexusUtilities.getLogger().err("[LAUNCHER] Couldn't find compatible Java Runtime Environment!");
                JavaInstaller javaInstaller = new JavaInstaller(Java.Runtime_11, getOS(), getArchitecture(), path);
                javaInstaller.install();
                NexusUtilities.getLogger().deb("[LAUNCHER] Starting installation of missing java runtime "+javaInstaller.getVersionString()+"...");
            }
            System.setProperty("java.home", java);
        } else if(type.equals(MinecraftVersion.Type.NEW)) {
            JavaUtil.setJavaCommand(null);
            String java = path+"/jre/";
            if(!new File(java).exists()) {
                NexusUtilities.getLogger().err("[LAUNCHER] Couldn't find compatible Java Runtime Environment!");
                JavaInstaller javaInstaller = new JavaInstaller(Java.Runtime_21, getOS(), getArchitecture(), path);
                javaInstaller.install();
                NexusUtilities.getLogger().deb("[LAUNCHER] Starting installation of missing java runtime "+javaInstaller.getVersionString()+"...");
            }
            System.setProperty("java.home", java);
        }
    }

    private OperatingSystem getOS() {
        return OperatingSystem.Windows;
    }

    private Architecture getArchitecture() {
        return Architecture.x64;
    }
}
