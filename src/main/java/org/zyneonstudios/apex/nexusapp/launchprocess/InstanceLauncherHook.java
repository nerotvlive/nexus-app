package org.zyneonstudios.apex.nexusapp.launchprocess;

import live.nerotv.aminecraftlauncher.launcher.LauncherHook;
import live.nerotv.aminecraftlauncher.launcher.MinecraftLauncher;
import org.zyneonstudios.apex.nexusapp.search.zyndex.local.LocalInstance;

public class InstanceLauncherHook extends LauncherHook {

    private final LocalInstance localInstance;

    public InstanceLauncherHook(MinecraftLauncher launcher, LocalInstance instance) {
        super(launcher);
        this.localInstance = instance;
    }

    public LocalInstance getLocalInstance() {
        return localInstance;
    }

    @Override
    public void run() {

    }
}
