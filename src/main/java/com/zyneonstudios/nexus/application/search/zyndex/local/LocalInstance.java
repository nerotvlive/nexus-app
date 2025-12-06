package com.zyneonstudios.nexus.application.search.zyndex.local;

import com.zyneonstudios.nexus.instance.Zynstance;

import java.io.File;

public class LocalInstance {

    private final Zynstance instance;
    private String path;

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
        this.path = path;
    }

    public Zynstance getInstance() {
        return instance;
    }

    public String getPath() {
        return path.replace("zyneonInstance.json","").replace("\\","/");
    }

    public void setPath(String path) {
        this.path = path;
    }
}