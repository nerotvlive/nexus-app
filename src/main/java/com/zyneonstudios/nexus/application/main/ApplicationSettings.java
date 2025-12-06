package com.zyneonstudios.nexus.application.main;

import java.util.HashMap;

public class ApplicationSettings {

    private final HashMap<String, Object> temporarySettings = new HashMap<>();
    private int memory = 2048;
    private boolean minimizeApp = true;
    private boolean nativeWindow = true;
    private boolean discoverSearchNEX = true;
    private boolean discoverSearchCurseForge = true;
    private boolean discoverSearchModrinth = true;
    private String defaultMinecraftPath = "instances/";
    private String lastInstanceId = "";

    public HashMap<String, Object> getTemporarySettings() {
        return temporarySettings;
    }

    public void setTemporarySetting(String path, Object value) {
        temporarySettings.put(path, value);
    }

    public Object getTemporarySetting(String path) {
        return temporarySettings.get(path);
    }

    public String getTemporaryString(String path) {
        return (String) temporarySettings.get(path);
    }

    public int getTemporaryInt(String path) {
        return (int) temporarySettings.get(path);
    }

    public boolean getTemporaryBoolean(String path) {
        return (boolean) temporarySettings.get(path);
    }

    public double getTemporaryDouble(String path) {
        return (double) temporarySettings.get(path);
    }

    public void clearTemporarySettings() {
        temporarySettings.clear();
    }

    public void removeTemporarySetting(String path) {
        temporarySettings.remove(path);
    }

    public int getDefaultMemory() {
        return memory;
    }

    public void setDefaultMemory(int memory) {
        this.memory = memory;
        NexusApplication.getInstance().getSettings().set("settings.minecraft.defaultMemory", memory);
    }

    public boolean minimizeApp() {
        return minimizeApp;
    }

    public void setMinimizeApp(boolean minimizeApp) {
        this.minimizeApp = minimizeApp;
        NexusApplication.getInstance().getSettings().set("settings.window.minimizeOnStart", minimizeApp);
    }

    public boolean useNativeWindow() {
        return nativeWindow;
    }

    public void setUseNativeWindow(boolean nativeWindow) {
        this.nativeWindow = nativeWindow;
        NexusApplication.getInstance().getSettings().set("settings.window.nativeDecorations", nativeWindow);
    }

    public void setDiscoverSearchCurseForge(boolean discoverSearchCurseForge) {
        this.discoverSearchCurseForge = discoverSearchCurseForge;
        NexusApplication.getInstance().getSettings().set("settings.discover.search.curseforge.enabled", discoverSearchCurseForge);
    }

    public void setDiscoverSearchModrinth(boolean discoverSearchModrinth) {
        this.discoverSearchModrinth = discoverSearchModrinth;
        NexusApplication.getInstance().getSettings().set("settings.discover.search.modrinth.enabled", discoverSearchModrinth);
    }

    public void setDiscoverSearchNEX(boolean discoverSearchNEX) {
        this.discoverSearchNEX = discoverSearchNEX;
        NexusApplication.getInstance().getSettings().set("settings.discover.search.nex.enabled", discoverSearchNEX);
    }

    public boolean isDiscoverSearchNEX() {
        return discoverSearchNEX;
    }

    public boolean isDiscoverSearchCurseForge() {
        return discoverSearchCurseForge;
    }

    public boolean isDiscoverSearchModrinth() {
        return discoverSearchModrinth;
    }

    public String getDefaultMinecraftPath() {
        return defaultMinecraftPath;
    }

    public void setDefaultMinecraftPath(String defaultMinecraftPath) {
        this.defaultMinecraftPath = defaultMinecraftPath;
        NexusApplication.getInstance().getSettings().set("settings.minecraft.defaultPath", defaultMinecraftPath);
    }

    public String getLastInstanceId() {
        return lastInstanceId;
    }

    public void setLastInstanceId(String lastInstanceId) {
        this.lastInstanceId = lastInstanceId;
    }
}