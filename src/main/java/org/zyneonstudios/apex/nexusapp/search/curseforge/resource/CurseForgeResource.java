package org.zyneonstudios.apex.nexusapp.search.curseforge.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.zyneonstudios.apex.nexusapp.main.NexusApplication;
import org.zyneonstudios.apex.nexusapp.search.curseforge.CurseForgeIntegration;

public class CurseForgeResource {

    private final String url;
    private final JsonObject json;

    private final int id;
    private final int gameId;
    private final String name;
    private final String slug;
    private final JsonObject links;
    private final String summary;
    private final int status;
    private final int downloads;
    private final boolean isFeatured;
    private final int primaryCategoryId;
    private final JsonArray categories;
    private final int classId;
    private final JsonArray authors;
    private final JsonObject logo;
    private final JsonArray screenshots;
    private final int mainFileId;
    private final JsonArray latestFiles;
    private final JsonArray latestFilesIndexes;
    private final JsonArray latestEarlyAccessFilesIndexes;
    private final String dateCreated;
    private final String dateModified;
    private final String dateReleased;
    private final boolean allowModDistribution;
    private final int gamePopularityRank;
    private final boolean isAvailable;
    private final int thumbsUpCount;

    public CurseForgeResource(int id) {
        this.url = "https://api.curseforge.com/v1/mods/" + id;
        this.json = NexusApplication.getInstance().getFastGson().fromJson(CurseForgeIntegration.accessAPI(url), JsonObject.class).getAsJsonObject("data");
        this.id = json.get("id").getAsInt();
        this.gameId = json.get("gameId").getAsInt();
        this.name = json.get("name").getAsString();
        this.slug = json.get("slug").getAsString();
        this.links = json.get("links").getAsJsonObject();
        this.summary = json.get("summary").getAsString();
        this.status = json.get("status").getAsInt();
        this.downloads = json.get("downloadCount").getAsInt();
        this.isFeatured = json.get("isFeatured").getAsBoolean();
        this.primaryCategoryId = json.get("primaryCategoryId").getAsInt();
        this.categories = json.get("categories").getAsJsonArray();
        this.classId = json.get("classId").getAsInt();
        this.authors = json.get("authors").getAsJsonArray();
        this.logo = json.get("logo").getAsJsonObject();
        this.screenshots = json.get("screenshots").getAsJsonArray();
        this.mainFileId = json.get("mainFileId").getAsInt();
        this.latestFiles = json.get("latestFiles").getAsJsonArray();
        this.latestFilesIndexes = json.get("latestFilesIndexes").getAsJsonArray();
        this.latestEarlyAccessFilesIndexes = json.get("latestEarlyAccessFilesIndexes").getAsJsonArray();
        this.dateCreated = json.get("dateCreated").getAsString();
        this.dateModified = json.get("dateModified").getAsString();
        this.dateReleased = json.get("dateReleased").getAsString();
        this.allowModDistribution = json.get("allowModDistribution").getAsBoolean();
        this.gamePopularityRank = json.get("gamePopularityRank").getAsInt();
        this.isAvailable = json.get("isAvailable").getAsBoolean();
        this.thumbsUpCount = json.get("thumbsUpCount").getAsInt();
    }

    public String getUrl() {
        return url;
    }

    public JsonObject getJson() {
        return json;
    }

    public int getId() {
        return id;
    }

    public int getGameId() {
        return gameId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public JsonObject getLinks() {
        return links;
    }

    public String getSummary() {
        return summary;
    }

    public int getStatus() {
        return status;
    }

    public int getDownloads() {
        return downloads;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public int getPrimaryCategoryId() {
        return primaryCategoryId;
    }

    public JsonArray getCategories() {
        return categories;
    }

    public int getClassId() {
        return classId;
    }

    public JsonArray getAuthors() {
        return authors;
    }

    public JsonObject getLogo() {
        return logo;
    }

    public JsonArray getScreenshots() {
        return screenshots;
    }

    public int getMainFileId() {
        return mainFileId;
    }

    public JsonArray getLatestFiles() {
        return latestFiles;
    }

    public JsonArray getLatestFilesIndexes() {
        return latestFilesIndexes;
    }

    public JsonArray getLatestEarlyAccessFilesIndexes() {
        return latestEarlyAccessFilesIndexes;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getDateModified() {
        return dateModified;
    }

    public String getDateReleased() {
        return dateReleased;
    }

    public boolean allowModDistribution() {
        return allowModDistribution;
    }

    public int getGamePopularityRank() {
        return gamePopularityRank;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public int getThumbsUpCount() {
        return thumbsUpCount;
    }
}