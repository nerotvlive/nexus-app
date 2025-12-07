package org.zyneonstudios.apex.nexusapp.main;

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
    private String jre21path = "";
    private String jre17path = "";
    private String jre8path = "";

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

    public String getJava21Path() {
        if(jre21path==null) {
            return NexusApplication.getInstance().getWorkingPath()+"/libs/jre-21";
        }
        return jre21path;
    }

    public String getJava17Path() {
        if(jre17path==null) {
            return NexusApplication.getInstance().getWorkingPath()+"/libs/jre-17";
        }
        return jre17path;
    }

    public String getJava8Path() {
        if(jre8path==null) {
            return NexusApplication.getInstance().getWorkingPath()+"/libs/jre-8";
        }
        return jre8path;
    }

    public void setJre21path(String jre21path) {
        this.jre21path = jre21path;
        NexusApplication.getInstance().getSettings().set("settings.java.path21", jre21path);
    }

    public void setJre17path(String jre17path) {
        this.jre17path = jre17path;
        NexusApplication.getInstance().getSettings().set("settings.java.path17", jre17path);
    }

    public void setJre8path(String jre8path) {
        this.jre8path = jre8path;
        NexusApplication.getInstance().getSettings().set("settings.java.path8", jre8path);
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
        if(defaultMinecraftPath==null) {
            return NexusApplication.getInstance().getWorkingPath()+"/instances/";
        }
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