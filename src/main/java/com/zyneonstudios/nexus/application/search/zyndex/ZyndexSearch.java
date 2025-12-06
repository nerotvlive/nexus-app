package com.zyneonstudios.nexus.application.search.zyndex;

import com.zyneonstudios.nexus.index.Index;
import com.zyneonstudios.nexus.index.ReadableZyndex;
import com.zyneonstudios.nexus.instance.ReadableZynstance;

import java.util.ArrayList;

public class ZyndexSearch {

    private final ArrayList<ReadableZynstance> instances;
    private ArrayList<ReadableZynstance> cachedResults = null;
    private String cachedSearchTerm = null;

    public ZyndexSearch(String zyndexUrl) {
        instances = new ReadableZyndex(zyndexUrl).getInstances();
    }

    public ZyndexSearch(Index zyndex) {
        instances = zyndex.getInstances();
    }

    @SuppressWarnings("all")
    public ArrayList<ReadableZynstance> search(String searchTerm) {
        if(!searchTerm.replace(" ","").isEmpty()) {
            cachedSearchTerm = searchTerm;
        }

        ArrayList<ReadableZynstance> results = new ArrayList<>();
        if(!instances.isEmpty()) {
            for(ReadableZynstance instance : instances) {
                boolean idMatching = false;
                if (searchTerm.equals(instance.getId())) {
                    idMatching = true;
                }

                if(instance.getName().toLowerCase().contains(searchTerm.toLowerCase())||instance.getAuthor().toLowerCase().contains(searchTerm.toLowerCase())||idMatching) {
                    if(!instance.isHidden()||idMatching) {
                        results.add(instance);
                    }
                }
            };
        }

        cachedResults = results;
        return cachedResults;
    }

    public ArrayList<ReadableZynstance> getCachedResults() {
        return cachedResults;
    }

    public String getCachedSearchTerm() {
        return cachedSearchTerm;
    }

}
