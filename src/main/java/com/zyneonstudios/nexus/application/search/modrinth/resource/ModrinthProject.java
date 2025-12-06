package com.zyneonstudios.nexus.application.search.modrinth.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;

public class ModrinthProject implements ModrinthResource {

    private final JsonObject root;
    private final String url;

    public ModrinthProject(String slug_or_id) {
        url = "https://api.modrinth.com/v2/project/"+slug_or_id;
        root = GsonUtility.getObject(url);
    }

    @Override
    public String getSlug() {
        return root.get("slug").getAsString();
    }

    @Override
    public String getTitle() {
        return root.get("title").getAsString();
    }

    @Override
    public String getId() {
        return root.get("id").getAsString();
    }

    @Override
    public String getDescription() {
        return root.get("description").getAsString();
    }

    @Override
    public String[] getCategories() {
        return java.util.stream.StreamSupport.stream(root.getAsJsonArray("categories").spliterator(), false)
                .map(com.google.gson.JsonElement::getAsString)
                .toArray(String[]::new);
    }

    @Override
    public PLATFORM_TYPE_STATE getClientSide() {
        return PLATFORM_TYPE_STATE.valueOf(root.get("client_side").getAsString().toUpperCase());
    }

    @Override
    public PLATFORM_TYPE_STATE getServerSide() {
        return PLATFORM_TYPE_STATE.valueOf(root.get("server_side").getAsString().toUpperCase());
    }

    @Override
    public String getBody() {
        return root.get("body").getAsString();
    }

    @Override
    public EXTENDED_STATUS getStatus() {
        return EXTENDED_STATUS.valueOf(root.get("status").getAsString());
    }

    @Override
    public PROJECT_STATUS getRequestedStatus() {
        return PROJECT_STATUS.valueOf(root.get("requested_status").getAsString());
    }

    @Override
    public String[] getAdditionalCategories() {
        return java.util.stream.StreamSupport.stream(root.getAsJsonArray("additional_categories").spliterator(), false)
                .map(com.google.gson.JsonElement::getAsString)
                .toArray(String[]::new);
    }

    @Override
    public String getIssuesUrl() {
        return root.get("issues_url").getAsString();
    }

    @Override
    public String getSourceUrl() {
        return root.get("source_url").getAsString();
    }

    @Override
    public String getWikiUrl() {
        return root.get("wiki_url").getAsString();
    }

    @Override
    public String getDiscordUrl() {
        return root.get("discord_url").getAsString();
    }

    @Override
    public String[] getDonationUrls() {
        return java.util.stream.StreamSupport.stream(root.getAsJsonArray("donation_urls").spliterator(), false)
                .map(com.google.gson.JsonElement::getAsString)
                .toArray(String[]::new);
    }

    @Override
    public PROJECT_TYPE getProjectType() {
        return PROJECT_TYPE.valueOf(root.get("project_type").getAsString().toUpperCase());
    }

    @Override
    public int getDownloads() {
        return root.get("downloads").getAsInt();
    }

    @Override
    public String getIconUrl() {
        return root.get("icon_url").getAsString();
    }

    @Override
    public int getColor() {
        return root.get("color").getAsInt();
    }

    @Override
    public String getThreadId() {
        return root.get("thread_id").getAsString();
    }

    @Override
    public MONETIZATION_STATUS getMonetizationStatus() {
        return MONETIZATION_STATUS.valueOf(root.get("monetization_status").getAsString().toUpperCase());
    }

    @Override
    public String getTeam() {
        return root.get("team").getAsString();
    }

    @Override
    public String getBodyUrl() {
        return root.get("body_url").getAsString();
    }

    @Override
    public String getModeratorMessage() {
        return root.get("moderator_message").getAsString();
    }

    @Override
    public String getPublished() {
        return root.get("published").getAsString();
    }

    @Override
    public String getUpdated() {
        return root.get("updated").getAsString();
    }

    @Override
    public String getApproved() {
        return root.get("approved").getAsString();
    }

    @Override
    public String getQueued() {
        return root.get("queued").getAsString();
    }

    @Override
    public int getFollowers() {
        return root.get("followers").getAsInt();
    }

    @Override
    public JsonObject getLicense() {
        return root.getAsJsonObject("license");
    }

    @Override
    public String[] getVersions() {
        return java.util.stream.StreamSupport.stream(root.getAsJsonArray("versions").spliterator(), false)
                .map(com.google.gson.JsonElement::getAsString)
                .toArray(String[]::new);
    }


    @Override
    public String[] getGameVersions() {
        return java.util.stream.StreamSupport.stream(root.getAsJsonArray("game_versions").spliterator(), false)
                .map(com.google.gson.JsonElement::getAsString)
                .toArray(String[]::new);
    }

    @Override
    public String[] getLoaders() {
        return java.util.stream.StreamSupport.stream(root.getAsJsonArray("loaders").spliterator(), false)
                .map(com.google.gson.JsonElement::getAsString)
                .toArray(String[]::new);
    }

    @Override
    public JsonArray getGallery() {
        return root.get("gallery").getAsJsonArray();
    }

    @Override
    public String getJson() {
        return root.toString();
    }

    @Override
    public String getUrl() {
        return url;
    }
}
