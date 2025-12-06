package com.zyneonstudios.nexus.application.modules;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zyneonstudios.nexus.application.main.NexusApplication;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarFile;

/**
 * The {@code ModuleLoader} class is responsible for loading, managing, and unloading modules within the Nexus application.
 * It handles reading module information from JAR files, instantiating module classes, and managing their lifecycle.
 * It also provides methods to activate and deactivate modules.
 */
public class ModuleLoader {

    // Map to store loaded modules, keyed by their unique ID.
    private final HashMap<String, NexusModule> modules = new HashMap<>();

    // Map to store the paths of module JAR files, keyed by module ID.
    private final HashMap<String, String> moduleJars = new HashMap<>();

    /**
     * Constructor for the ModuleLoader.
     *
     * @param application The main NexusApplication instance.
     */
    public ModuleLoader(NexusApplication application) {
        // Check for modules marked for uninstallation and delete them.
        if (application.getSettings().has("settings.modules.uninstall")) {
            ArrayList<String> uninstallModules = (ArrayList<String>) application.getSettings().get("settings.modules.uninstall");
            for (String module : uninstallModules) {
                if(!new File(module).delete()) {
                    NexusApplication.getLogger().deb("Failed to delete module " + module);
                }
            }
            application.getSettings().delete("settings.modules.uninstall");
        }
    }

    /**
     * Gets a collection of all loaded Nexus modules.
     *
     * @return A collection of NexusModule instances.
     */
    public Collection<NexusModule> getNexusModules() {
        return modules.values();
    }

    /**
     * Gets a map of all loaded Nexus modules, keyed by their IDs.
     *
     * @return A map of module IDs to NexusModule instances.
     */
    public HashMap<String, NexusModule> getNexusModulesById() {
        return modules;
    }

    /**
     * Gets a set of all loaded Nexus module IDs.
     *
     * @return A set of module IDs.
     */
    public Set<String> getNexusModuleIds() {
        return modules.keySet();
    }

    /**
     * Reads module information from a JAR file.
     *
     * @param moduleJar The JAR file containing the module.
     * @return The NexusModule instance if successful, null otherwise.
     */
    public NexusModule readModule(File moduleJar) {
        try {
            String mainPath;
            String id;
            // Read module information from nexus-module.json within the JAR.
            try (JarFile jarFile = new JarFile(moduleJar.getAbsolutePath())) {
                InputStream is = jarFile.getInputStream(jarFile.getJarEntry("nexus-module.json"));
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
                JsonArray array = jsonObject.getAsJsonArray("modules");
                mainPath = array.get(0).getAsJsonObject().get("main").getAsString();
                id = array.get(0).getAsJsonObject().get("id").getAsString();
                // Check if a module with the same ID is already loaded.
                if (moduleJars.containsKey(id)) {
                    NexusApplication.getLogger().deb("Module with ID '" + id + "' is already loaded. Skipping duplicate.");
                    return null;
                }
            } catch (Exception e) {
                NexusApplication.getLogger().err("Couldn't read module " + moduleJar.getPath() + ": " + e.getMessage());
                return null;
            }
            // Load the module class using a URLClassLoader.
            URLClassLoader classLoader = new URLClassLoader(new URL[]{moduleJar.toURI().toURL()});
            Class<?> moduleClass = classLoader.loadClass(mainPath);
            Constructor<?> constructor = moduleClass.getConstructor();
            NexusModule module = (NexusModule) constructor.newInstance();
            // Validate the module ID and store the module path.
            if (id.equals(module.getModuleId())) {
                moduleJars.put(id, moduleJar.getAbsolutePath().replace("\\", "/"));
                return module;
            } else {
                NexusApplication.getLogger().err("Module ID mismatch in " + moduleJar.getPath() + ". Expected: " + id + ", Found: " + module.getModuleId());
            }
        } catch (Exception e) {
            NexusApplication.getLogger().err("Couldn't read module " + moduleJar.getPath() + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Loads a module into the application.
     *
     * @param module The NexusModule instance to load.
     */
    public void loadModule(NexusModule module) {
        // Check if a module with the same ID is already loaded.
        if (!modules.containsKey(module.getModuleId())) {
            NexusApplication.getLogger().log("Loading module " + module.getModuleId() + " v" + module.getModuleVersion() + " by " + module.getModuleOwner() + "...");
            modules.put(module.getModuleId(), module);
            module.onLoad();
        } else {
            NexusApplication.getLogger().err("Module with ID '" + module.getModuleId() + "' is already loaded. Skipping.");
        }
    }

    /**
     * Activates all loaded modules.
     */
    public void activateModules() {
        for (NexusModule module : modules.values()) {
            module.onEnable();
        }
    }

    /**
     * Deactivates all loaded modules.
     */
    public void deactivateModules() {
        for (NexusModule module : modules.values()) {
            module.onDisable();
        }
    }

    /**
     * Unloads a specific module.
     *
     * @param module The NexusModule instance to unload.
     */
    public void unloadModule(NexusModule module) {
        modules.remove(module.getModuleId());
    }

    /**
     * Unloads all loaded modules.
     */
    public void unloadModules() {
        for (NexusModule module : modules.values()) {
            unloadModule(module);
        }
        modules.clear();
        moduleJars.clear();
        System.gc();
    }
}