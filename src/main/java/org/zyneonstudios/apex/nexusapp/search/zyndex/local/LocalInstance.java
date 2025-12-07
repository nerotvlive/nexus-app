package org.zyneonstudios.apex.nexusapp.search.zyndex.local;

import com.zyneonstudios.nexus.instance.Zynstance;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;
import org.zyneonstudios.apex.nexusapp.main.NexusApplication;

import java.io.File;
import java.util.ArrayList;

public class LocalInstance {

    private JsonStorage settings;
    private final Zynstance instance;
    private String path;

    private boolean fullscreen = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftFullscreen();
    private int width = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftWindowWidth();
    private int height = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftWindowHeight();
    private int memory = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftMemory();
    private ArrayList<String> jvmArgs = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftJVMArgs();
    private ArrayList<String> envArgs = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftEnvArgs();
    private ArrayList<String> preLaunchHook = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPreLaunchCommands();
    private ArrayList<String> onLaunchHook = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnLaunchCommands();
    private ArrayList<String> onExitHook = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnExitCommands();


    /**
     * Creates a LocalInstance from a given path.
     * The path should point to a valid Zynstance JSON file.
     * * @throws IllegalArgumentException if the file does not exist
     * @param path
     */
    public LocalInstance(String path) {
        File instanceFile = new File(path);
        if(!instanceFile.exists()) {
            throw new IllegalArgumentException("Instance file does not exist: " + path);
        }
        this.instance = new Zynstance(instanceFile);
        this.path = path.replace("\\","/");
        reloadSettings();
    }

    public void reloadSettings() {
        this.settings = new JsonStorage(this.path.replace("zyneonInstance.json","zyneonSettings.json"));
        if (settings.has("settings.fullscreen")) {
            this.fullscreen = settings.getBool("settings.fullscreen");
        }
        if (settings.has("settings.width")) {
            this.width = settings.getInt("settings.width");
        }
        if (settings.has("settings.height")) {
            this.height = settings.getInt("settings.height");
        }
        if(settings.has("settings.memory")) {
            this.memory = settings.getInt("settings.memory");
        }
        if(settings.has("settings.jvmArgs")) {
            this.jvmArgs = (ArrayList<String>)settings.get("settings.jvmArgs");
        }
        if(settings.has("settings.envArgs")) {
            this.envArgs = (ArrayList<String>)settings.get("settings.envArgs");
        }
        if(settings.has("settings.preLaunchCommands")) {
            this.preLaunchHook = (ArrayList<String>)settings.get("settings.preLaunchCommands");
        }
        if(settings.has("settings.onLaunchCommands")) {
            this.onLaunchHook = (ArrayList<String>)settings.get("settings.onLaunchCommands");
        }
        if(settings.has("settings.onExitCommands")) {
            this.onExitHook = (ArrayList<String>)settings.get("settings.onExitCommands");
        }
    }

    public Zynstance getInstance() {
        return instance;
    }

    public String getPath() {
        return path.replace("zyneonInstance.json","").replace("\\","/");
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public ArrayList<String> getEnvArgs() {
        return envArgs;
    }

    public ArrayList<String> getJvmArgs() {
        return jvmArgs;
    }

    public ArrayList<String> getOnExitHook() {
        return onExitHook;
    }

    public ArrayList<String> getOnLaunchHook() {
        return onLaunchHook;
    }

    public ArrayList<String> getPreLaunchHook() {
        return preLaunchHook;
    }

    public int getHeight() {
        return height;
    }

    public int getMemory() {
        return memory;
    }

    public int getWidth() {
        return width;
    }

    public JsonStorage getSettings() {
        return settings;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        settings.set("settings.fullscreen", true);
    }

    public void setWidth(int width) {
        this.width = width;
        settings.set("settings.width", width);
    }

    public void setHeight(int height) {
        this.height = height;
        settings.set("settings.height", height);
    }

    public void setMemory(int memory) {
        this.memory = memory;
        settings.set("settings.memory", memory);
    }

    public void setJvmArgs(ArrayList<String> jvmArgs) {
        this.jvmArgs = jvmArgs;
        settings.set("settings.jvmArgs", jvmArgs);
    }

    public void setEnvArgs(ArrayList<String> envArgs) {
        this.envArgs = envArgs;
        settings.set("settings.envArgs", envArgs);
    }

    public void setPreLaunchHook(ArrayList<String> preLaunchHook) {
        this.preLaunchHook = preLaunchHook;
        settings.set("settings.preLaunchCommands",preLaunchHook);
    }

    public void setOnLaunchHook(ArrayList<String> onLaunchHook) {
        this.onLaunchHook = onLaunchHook;
        settings.set("settings.onLaunchCommands",onLaunchHook);
    }

    public void setOnExitHook(ArrayList<String> onExitHook) {
        this.onExitHook = onExitHook;
        settings.set("settings.onExitCommands",onExitHook);
    }
}