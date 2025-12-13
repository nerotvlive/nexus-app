package org.zyneonstudios.apex.nexusapp.search;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.zyneonstudios.apex.nexusapp.main.NexusApplication;
import org.zyneonstudios.apex.nexusapp.search.curseforge.search.CurseForgeSearch;
import org.zyneonstudios.apex.nexusapp.search.curseforge.search.facets.CurseForgeFacetsBuilder;
import org.zyneonstudios.apex.nexusapp.search.modrinth.search.ModrinthSearch;
import org.zyneonstudios.apex.nexusapp.search.modrinth.search.facets.ModrinthFacetsBuilder;
import org.zyneonstudios.apex.nexusapp.search.modrinth.search.facets.ModrinthProjectType;
import org.zyneonstudios.apex.nexusapp.search.modrinth.search.facets.categories.ModrinthCategory;
import org.zyneonstudios.apex.nexusapp.search.zyndex.ZyndexSearch;
import org.zyneonstudios.apex.nexusapp.utilities.StringUtility;
import com.zyneonstudios.nexus.instance.ReadableZynstance;

import java.util.ArrayList;
import java.util.List;

public class CombinedSearch {

    private final ZyndexSearch NEXSearch;
    private final ModrinthSearch modrinthSearch;

    private int offset = 0;
    private int hits = 20;

    public CombinedSearch(int[] curseForgeCategoryIds, ModrinthCategory[] modrinthCategories) {
        NEXSearch = new ZyndexSearch(NexusApplication.getInstance().getNEX());
        modrinthSearch = new ModrinthSearch();
        modrinthSearch.setFacets(new ModrinthFacetsBuilder().withProjectType(ModrinthProjectType.modpack).withCategories(modrinthCategories).build());
    }

    public void setLimit(int limit) {
        this.hits = limit;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public ZyndexSearch getNEXSearch() {
        return NEXSearch;
    }

    public int getLimit() {
        return hits;
    }

    public int getOffset() {
        return offset;
    }

    public JsonArray search(String query) {
        modrinthSearch.setLimit(hits);
        modrinthSearch.setOffset(offset * hits);

        JsonArray results = new JsonArray();
        modrinthSearch.setQuery(StringUtility.encodeData(query));
        ArrayList<ReadableZynstance> nexResults;
        JsonObject modrinthResults;

        if (NexusApplication.getInstance().getLocalSettings().isDiscoverSearchNEX()) {
            nexResults = NEXSearch.search(query);
        } else {
            nexResults = new ArrayList<>();
        }

        if (NexusApplication.getInstance().getLocalSettings().isDiscoverSearchModrinth()) {
            modrinthResults = modrinthSearch.search();
        } else {
            modrinthResults = null;
        }

        try {
            List<JsonObject> nexJsonResults = new ArrayList<>();
            for (int i = offset*hits; i < nexResults.size(); i++) {
                ReadableZynstance instance = nexResults.get(i);
                JsonObject result = new JsonObject();
                result.addProperty("id", StringUtility.encodeData(instance.getId()));
                result.addProperty("iconUrl", instance.getThumbnailUrl());
                result.addProperty("name", StringUtility.encodeData(instance.getName()));
                result.addProperty("downloads", "hidden");
                result.addProperty("followers", "hidden");
                JsonArray authors = new JsonArray();
                for (String author : instance.getAuthors()) {
                    authors.add(StringUtility.encodeData(author));
                }
                result.add("authors", authors);
                result.addProperty("summary", StringUtility.encodeData(instance.getSummary()));
                result.addProperty("url", "hidden");
                result.addProperty("source", "NEX");
                result.addProperty("connector", "install.minecraft.nex." + instance.getId());
                nexJsonResults.add(result);
            }

            List<JsonObject> modrinthJsonResults = new ArrayList<>();
            if (modrinthResults != null) {
                for (JsonElement hit : modrinthResults.getAsJsonArray("hits")) {
                    JsonObject modrinthResult = hit.getAsJsonObject();
                    JsonObject result = new JsonObject();
                    result.addProperty("id", StringUtility.encodeData(modrinthResult.get("project_id").getAsString()));
                    result.addProperty("iconUrl", modrinthResult.get("icon_url").getAsString());
                    result.addProperty("name", StringUtility.encodeData(modrinthResult.get("title").getAsString()));
                    result.addProperty("downloads", modrinthResult.get("downloads").getAsString());
                    result.addProperty("followers", modrinthResult.get("follows").getAsString());
                    JsonArray authors = new JsonArray();
                    authors.add(StringUtility.encodeData(modrinthResult.get("author").getAsString()));
                    result.add("authors", authors);
                    result.addProperty("summary", StringUtility.encodeData(modrinthResult.get("description").getAsString()));
                    result.addProperty("url", "https:modrinth.com/modpack/" + modrinthResult.get("slug").getAsString());
                    result.addProperty("source", "Modrinth");
                    result.addProperty("connector", StringUtility.encodeData("install.minecraft.modrinth." + modrinthResult.get("project_id").getAsString()));
                    modrinthJsonResults.add(result);
                }
            }

            int i = 0;
            while (i < nexJsonResults.size() || i < modrinthJsonResults.size()) {
                if (i < nexJsonResults.size()) {
                    results.add(nexJsonResults.get(i));
                }
                if (i < modrinthJsonResults.size()) {
                    results.add(modrinthJsonResults.get(i));
                }
                i++;
            }

        } catch (Exception e) {
            NexusApplication.getLogger().printErr("NEXUS", "COMBINED SEARCH", "Couldn't process the search results...", e.getMessage(), e.getStackTrace());
        }
        return results;
    }
}