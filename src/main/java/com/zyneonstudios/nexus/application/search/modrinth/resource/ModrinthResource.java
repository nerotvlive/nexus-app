package com.zyneonstudios.nexus.application.search.modrinth.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface ModrinthResource {

    String getSlug();
    String getTitle();
    String getId();
    String getDescription();
    String[] getCategories();
    PLATFORM_TYPE_STATE getClientSide();
    PLATFORM_TYPE_STATE getServerSide();
    String getBody();
    EXTENDED_STATUS getStatus();
    PROJECT_STATUS getRequestedStatus();
    String[] getAdditionalCategories();
    String getIssuesUrl();
    String getSourceUrl();
    String getWikiUrl();
    String getDiscordUrl();
    String[] getDonationUrls();
    PROJECT_TYPE getProjectType();
    int getDownloads();
    String getIconUrl();
    int getColor();
    String getThreadId();
    MONETIZATION_STATUS getMonetizationStatus();
    String getTeam();
    String getBodyUrl();
    String getModeratorMessage();
    String getPublished();
    String getUpdated();
    String getApproved();
    String getQueued();
    int getFollowers();
    JsonObject getLicense();
    String[] getVersions();
    String[] getGameVersions();
    String[] getLoaders();
    JsonArray getGallery();

    String getJson();
    String getUrl();

    enum PLATFORM_TYPE_STATE {
        REQUIRED,
        PLATFORM_TYPE_STATE,
        UNSUPPORTED,
        UNKNOWN
    }

    enum PROJECT_STATUS {
        APPROVED,
        ARCHIVED,
        UNLISTED,
        PRIVATE,
        DRAFT
    }

    enum EXTENDED_STATUS {
        APPROVED,
        ARCHIVED,
        REJECTED,
        PROCESSING,
        WITHHELD,
        SCHEDULED,
        UNKNOWN,
        UNLISTED,
        PRIVATE,
        DRAFT
    }

    enum PROJECT_TYPE {
        MOD,
        MODPACK,
        RESOURCEPACK,
        SHADER
    }

    enum MONETIZATION_STATUS {
        MONETIZED,
        DEMONITIZED,
        FORCE_DEMONITIZED
    }
}