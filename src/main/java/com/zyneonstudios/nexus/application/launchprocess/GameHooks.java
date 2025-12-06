package com.zyneonstudios.nexus.application.launchprocess;

import com.zyneonstudios.nexus.application.main.NexusApplication;
import live.nerotv.aminecraftlauncher.launcher.LauncherHook;
import live.nerotv.aminecraftlauncher.launcher.MinecraftLauncher;

import java.awt.*;

public class GameHooks {

    public static LauncherHook getPreLaunchHook(MinecraftLauncher launcher) {
        return new LauncherHook(launcher) {
            @Override
            public void run() {
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.getElementById(\"launch-button\").innerHTML = \"STARTING...\";");
            }
        };
    }

    public static LauncherHook getPostLaunchHook(MinecraftLauncher launcher) {
        return new LauncherHook(launcher) {
            @Override
            public void run() {
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.getElementById(\"launch-button\").innerHTML = \"<i class='bi bi-check-lg'></i> RUNNING\";");
                if(NexusApplication.getInstance().getLocalSettings().minimizeApp()) {
                    NexusApplication.getInstance().getApplicationFrame().setState(Frame.ICONIFIED);
                }
            }
        };
    }

    public static LauncherHook getGameCloseHook(MinecraftLauncher launcher) {
        return new LauncherHook(launcher) {
            @Override
            public void run() {
                NexusApplication.getInstance().getInstanceManager().removeRunningInstance(launcher.getGameProcess());
                if(NexusApplication.getInstance().getApplicationFrame().getBrowser().getURL().contains("page=library")) {
                    NexusApplication.getInstance().getApplicationFrame().getBrowser().reload();
                }
                if(NexusApplication.getInstance().getLocalSettings().minimizeApp()) {
                    NexusApplication.getInstance().getApplicationFrame().setState(Frame.NORMAL);
                }
            }
        };
    }
}