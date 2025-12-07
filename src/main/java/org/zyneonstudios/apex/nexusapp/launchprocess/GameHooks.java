package org.zyneonstudios.apex.nexusapp.launchprocess;

import com.zyneonstudios.nexus.utilities.system.OperatingSystem;
import live.nerotv.aminecraftlauncher.launcher.MinecraftLauncher;
import org.zyneonstudios.apex.nexusapp.main.NexusApplication;
import org.zyneonstudios.apex.nexusapp.search.zyndex.local.LocalInstance;

import java.awt.*;

public class GameHooks {

    public static InstanceLauncherHook getPreLaunchHook(MinecraftLauncher launcher, LocalInstance instance) {
        return new InstanceLauncherHook(launcher, instance) {
            @Override
            public void run() {
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.getElementById(\"launch-button\").innerHTML = \"STARTING...\";");
                for(String cmd: getLocalInstance().getPreLaunchHook()) {
                    if(cmd.startsWith("napp ")) {
                        NexusApplication.getInstance().getConsoleHandler().runCommand(cmd.replaceFirst("napp ", ""));
                    } else {
                        runCommand(cmd);
                    }
                }
            }
        };
    }

    public static InstanceLauncherHook getPostLaunchHook(MinecraftLauncher launcher, LocalInstance instance) {
        return new InstanceLauncherHook(launcher, instance) {
            @Override
            public void run() {
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.getElementById(\"launch-button\").innerHTML = \"<i class='bi bi-check-lg'></i> RUNNING\";");
                if(NexusApplication.getInstance().getLocalSettings().minimizeApp()) {
                    NexusApplication.getInstance().getApplicationFrame().setState(Frame.ICONIFIED);
                }
                for(String cmd: getLocalInstance().getOnLaunchHook()) {
                    if(cmd.startsWith("napp ")) {
                        NexusApplication.getInstance().getConsoleHandler().runCommand(cmd.replaceFirst("napp ", ""));
                    } else {
                        runCommand(cmd);
                    }
                }
            }
        };
    }

    public static InstanceLauncherHook getGameCloseHook(MinecraftLauncher launcher, LocalInstance instance) {
        return new InstanceLauncherHook(launcher,instance) {
            @Override
            public void run() {
                NexusApplication.getInstance().getInstanceManager().removeRunningInstance(launcher.getGameProcess());
                if(NexusApplication.getInstance().getApplicationFrame().getBrowser().getURL().contains("page=library")) {
                    NexusApplication.getInstance().getApplicationFrame().getBrowser().reload();
                }
                if(NexusApplication.getInstance().getLocalSettings().minimizeApp()) {
                    NexusApplication.getInstance().getApplicationFrame().setState(Frame.NORMAL);
                }
                for(String cmd: getLocalInstance().getOnExitHook()) {
                    if(cmd.startsWith("napp ")) {
                        NexusApplication.getInstance().getConsoleHandler().runCommand(cmd.replaceFirst("napp ", ""));
                    } else {
                        runCommand(cmd);
                    }
                }
            }
        };
    }

    private static void runCommand(String cmd) {
        try {
            Process process;
            String fullCommand;
            if (OperatingSystem.getType().equals(OperatingSystem.Type.Windows)) {
                fullCommand = String.format("cmd.exe /c start " + cmd, NexusApplication.getInstance().getWorkingDir());
            } else {
                fullCommand = String.format("/bin/sh -c nohup " + cmd + " &", NexusApplication.getInstance().getWorkingDir());
            }
            process = Runtime.getRuntime().exec(fullCommand);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}