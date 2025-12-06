package com.zyneonstudios.nexus.application.search.zyndex.local;

import com.zyneonstudios.nexus.application.Main;
import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.application.utilities.ApplicationMigrator;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

/**
 * LocalInstanceManager is responsible for managing local instances of the application.
 * It provides methods to add, remove, and retrieve instances by their path, id, or name.
 * The instances are stored in a HashMap where the key is the instance path.
 */
public class LocalInstanceManager {

    private final JsonStorage instanceStorage;
    private HashMap<String, LocalInstance> instances = new HashMap<>();
    private final HashMap<Process, String> runningInstances = new HashMap<>();

    public LocalInstanceManager(JsonStorage instanceStorage) {
        this.instanceStorage = instanceStorage;
        reload();
    }

    /**
     * Reloads the instances from the local storage.
     * This method clears the current instances and reloads them from the specified directory.
     * It also migrates old instances if they exist.
     * If the instance directory does not exist, it attempts to create it.
     * If the directory is not a valid directory, it throws a RuntimeException.
     */
    public void reload() {
        instances = new HashMap<>();
        File instancePath = new File(NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPath());
        if(!instancePath.exists()) {
            if(!instancePath.mkdirs()) {
                throw new RuntimeException("Failed to create instances directory: " + instancePath.getAbsolutePath());
            }
        }
        if(NexusApplication.getInstance().getWorkingPath().equals(Main.getDefaultPath())) {
            if (ApplicationMigrator.getOldMinecraftInstances() != null) {
                for (String oldInstance : ApplicationMigrator.getOldMinecraftInstances()) {
                    File instanceFile = new File(oldInstance);
                    if (instanceFile.exists()) {
                        LocalInstance instance = new LocalInstance(instanceFile.getAbsolutePath());
                        if (!instances.containsKey(instance.getPath())) {
                            instances.put(instance.getPath(), instance);
                        }
                    }
                }
            }
        }
        if(instancePath.isDirectory()) {
            for(File file : Objects.requireNonNull(instancePath.listFiles())) {
                if(file.isDirectory()) {
                    File instanceFile = new File(file.getAbsolutePath()+"/zyneonInstance.json");
                    if(instanceFile.exists()) {
                        try {
                            LocalInstance instance = new LocalInstance(instanceFile.getAbsolutePath());
                            if (!instances.containsKey(instance.getPath())) {
                                instances.put(instance.getPath(), instance);
                            }
                        } catch (Exception e) {
                            NexusApplication.getLogger().err(e.getMessage());
                        }
                    }
                }
            }
        } else {
            throw new RuntimeException("The specified instance path is not a directory: " + instancePath.getAbsolutePath());
        }
        System.gc();
    }

    /**
     * Retrieves the instances managed by this LocalInstanceManager.
     *
     * @return A HashMap containing LocalInstance objects, where the key is the instance path.
     *         This method returns all instances currently managed by the manager.
     *         If no instances are present, it returns an empty HashMap.
     */
    public HashMap<String, LocalInstance> getInstances() {
        return instances;
    }

    /**
     * Sets the instances in the manager.
     *
     * @param instances A HashMap containing LocalInstance objects, where the key is the instance path.
     *                  This method replaces the current instances with the provided ones.
     *                  If the provided instances contain paths that already exist in the manager,
     *                  those instances will be replaced.
     */
    public void setInstances(HashMap<String, LocalInstance> instances) {
        this.instances = instances;
    }

    /**
     * Retrieves a LocalInstance by its identifier.
     *
     * @param identifier Should be the path, id, or name of the instance.
     *                   If the identifier is a path, it will be used to find the instance.
     *                   If the identifier is an id, it will be used to find the instance.
     *                   If the identifier is a name, it will be used to find the instance.
     *                   If the identifier is not found, it will return null.
     * @return The LocalInstance object if found, otherwise null.
     */
    public LocalInstance getInstance(String identifier) {
        if (instances.containsKey(identifier)) {
            return instances.get(identifier);
        }
        for (LocalInstance instance : instances.values()) {
            if (instance.getInstance().getId().equals(identifier)) {
                return instance;
            }
        }
        for (LocalInstance instance : instances.values()) {
            if (instance.getPath().equals(identifier)) {
                return instance;
            }
        }
        for (LocalInstance instance : instances.values()) {
            if (instance.getInstance().getName().equals(identifier)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Adds a LocalInstance to the manager.
     *
     * @param instance The LocalInstance object to be added.
     *                 If the instance's path already exists in the manager, it will not be added.
     *                 This method checks for the uniqueness of the instance's path.
     * @return true if the instance was added successfully, false if the path already exists.
     */
    public boolean addInstance(LocalInstance instance) {
        if (!instances.containsKey(instance.getPath())) {
            instances.put(instance.getPath(), instance);
            reload();
            return true;
        }
        return false;
    }

    /**
     * Removes a LocalInstance from the manager by its path.
     *
     * @param path The path of the instance to be removed.
     *             If the path exists in the manager, it will be removed.
     * @return true if the instance was removed successfully, false if the path does not exist.
     */
    public boolean removeInstance(String path) {
        if (instances.containsKey(path)) {
            instances.remove(path);
            reload();
            return true;
        }
        return false;
    }

    /**
     * Retrieves the JsonStorage instance used by this LocalInstanceManager.
     *
     * @return The JsonStorage instance that manages the storage of local instances.
     *         This storage is used to persist the state of instances across application runs.
     */
    public JsonStorage getInstanceStorage() {
        return instanceStorage;
    }

    public void addRunningInstance(Process gameProcess, String identifier) {
        runningInstances.put(gameProcess, identifier);
    }

    public void removeRunningInstance(Process gameProcess) {
        runningInstances.remove(gameProcess);
    }

    public boolean hasRunningInstance(String identifier) {
        return runningInstances.containsValue(identifier);
    }
}