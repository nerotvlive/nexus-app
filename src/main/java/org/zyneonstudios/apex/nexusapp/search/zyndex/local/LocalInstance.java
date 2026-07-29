package org.zyneonstudios.apex.nexusapp.search.zyndex.local;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zyneonstudios.nexus.instance.Zynstance;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;
import org.zyneonstudios.apex.nexusapp.Main;
import org.zyneonstudios.apex.nexusapp.main.NexusApplication;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class LocalInstance {

    private JsonStorage settings;
    private JsonStorage contents;
    private final Zynstance instance;
    private String path;

    private final HashMap<String, LocalInstanceContent> contentsMap = new HashMap<>();
    private final HashMap<String, LocalInstanceContent> contentsMapByPath = new HashMap<>();

    private boolean fullscreen = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftFullscreen();
    private int width = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftWindowWidth();
    private int height = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftWindowHeight();
    private int memory = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftMemory();
    private ArrayList<String> jvmArgs = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftJVMArgs();
    private ArrayList<String> envArgs = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftEnvArgs();
    private ArrayList<String> preLaunchHook = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPreLaunchCommands();
    private ArrayList<String> onLaunchHook = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnLaunchCommands();
    private ArrayList<String> onExitHook = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnExitCommands();
    private String about = null;


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
        reloadContents();
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

    public void reloadContents() {
        reloadAbout();
        String baseDir = this.path.replace("zyneonInstance.json", "");
        this.contents = new JsonStorage(baseDir + "zyneonContents.json");
        this.contents.ensure("contents", new JsonArray());
        Map<String, JsonObject> metaCache = new HashMap<>();
        if (this.contents.has("contents")) {
            List<Object> rawList = (List<Object>) this.contents.get("contents");
            for (Object obj : rawList) {
                JsonObject contentMeta = this.contents.getGson().toJsonTree(obj).getAsJsonObject();
                if (contentMeta.has("path")) {
                    String p = contentMeta.get("path").getAsString().replace("\\", "/");
                    metaCache.put(p, contentMeta);
                }
            }
        }
        String[] targetDirs = {"mods", "saves", "resourcepacks", "shaderpacks"};
        for (String dirName : targetDirs) {
            File folder = new File(baseDir + dirName);
            File[] files = folder.listFiles();
            if (files == null) continue;
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.startsWith(".") || fileName.toLowerCase().endsWith(".txt")) continue;
                String relativePath = dirName + "/" + fileName;
                String name = fileName.replace(".jar", "").replace(".zip", "");
                String author = "custom source";
                String version = "Not added via Zyneon Desktop";
                String idOrSlug = relativePath;
                String link = null;
                JsonObject meta = metaCache.get(relativePath);
                if (meta != null) {
                    if (meta.has("id_or_slug")) idOrSlug = meta.get("id_or_slug").getAsString();
                    if (meta.has("name")) name = meta.get("name").getAsString();
                    if (meta.has("author")) author = meta.get("author").getAsString();
                    if (meta.has("version")) version = meta.get("version").getAsString();
                    if (meta.has("link")) link = meta.get("link").getAsString();
                }
                LocalInstanceContent value = new LocalInstanceContent(idOrSlug, name, author, version, relativePath, link);
                contentsMap.put(idOrSlug, value);
                contentsMapByPath.put(relativePath, value);
            }
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

    public HashMap<String, LocalInstanceContent> getContentsMap() {
        return contentsMap;
    }

    public HashMap<String, LocalInstanceContent> getContentsByPathMap() {
        return contentsMapByPath;
    }

    public JsonStorage getContents() {
        return contents;
    }

    public String getAbout() {
        if(about == null) {
            return getInstance().getDescription();
        }
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public void resetAbout() {
        this.about = null;
    }

    public void reloadAbout() {
        resetAbout();
        String baseDir = this.path.replace("zyneonInstance.json", "");
        File about = new File(baseDir + "zyneonAbout.md");
        if(about.exists()) {
            try {
                this.about = Files.readString(about.toPath());
            } catch (Exception e) {
                Main.getLogger().err(e.getMessage(),false);
            }
        }
    }
}