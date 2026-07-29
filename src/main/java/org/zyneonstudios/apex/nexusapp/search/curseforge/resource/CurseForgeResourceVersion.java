package org.zyneonstudios.apex.nexusapp.search.curseforge.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.zyneonstudios.apex.nexusapp.main.NexusApplication;
import org.zyneonstudios.apex.nexusapp.search.curseforge.CurseForgeIntegration;

public class CurseForgeResourceVersion {

    private final String url;
    private final JsonObject json;

    private final Integer id;
    private final Integer gameId;
    private final Integer modId;
    private final Boolean isAvailable;
    private final String displayName;
    private final String fileName;
    private final Integer releaseType;
    private final Integer fileStatus;
    private final JsonArray hashes;
    private final String fileDate;
    private final Integer fileLength;
    private final Integer downloadCount;
    private final Integer fileSizeOnDisk;
    private final String downloadUrl;
    private final JsonArray gameVersions;
    private final JsonArray sortableGameVersions;
    private final JsonArray dependencies;
    private final Boolean exposeAsAlternative;
    private final Integer parentProjectFileId;
    private final Integer alternateFileId;
    private final Boolean isServerPack;
    private final Integer serverPackFileId;
    private final Boolean isEarlyAccessContent;
    private final String earlyAccessEndDate;
    private final Integer fileFingerprint;
    private final JsonArray modules;

    public CurseForgeResourceVersion(int modId, int fileId) {
        this.url = "https://api.curseforge.com/v1/mods/"+modId+"/files/"+fileId;
        this.json = NexusApplication.getInstance().getFastGson().fromJson(CurseForgeIntegration.accessAPI(url), JsonObject.class).getAsJsonObject("data");

        if(json.has("id")) {
            this.id = json.get("id").getAsInt();
        } else {
            this.id = null;
        }

        if(json.has("gameId")) {
            this.gameId = json.get("gameId").getAsInt();
        } else {
            this.gameId = null;
        }

        if(json.has("modId")) {
            this.modId = json.get("modId").getAsInt();
        } else {
            this.modId = null;
        }

        if(json.has("isAvailable")) {
            this.isAvailable = json.get("isAvailable").getAsBoolean();
        } else {
            this.isAvailable = null;
        }

        if(json.has("displayName")) {
            this.displayName = json.get("displayName").getAsString();
        } else {
            this.displayName = null;
        }

        if(json.has("fileName")) {
            this.fileName = json.get("fileName").getAsString();
        } else {
            this.fileName = null;
        }

        if(json.has("releaseType")) {
            this.releaseType = json.get("releaseType").getAsInt();
        } else {
            this.releaseType = null;
        }

        if(json.has("fileStatus")) {
            this.fileStatus = json.get("fileStatus").getAsInt();
        } else {
            this.fileStatus = null;
        }

        if(json.has("hashes")) {
            this.hashes = json.get("hashes").getAsJsonArray();
        } else {
            this.hashes = null;
        }

        if(json.has("fileDate")) {
            this.fileDate = json.get("fileDate").getAsString();
        } else {
            this.fileDate = null;
        }

        if(json.has("fileLength")) {
            this.fileLength = json.get("fileLength").getAsInt();
        } else {
            this.fileLength = null;
        }

        if(json.has("downloadCount")) {
            this.downloadCount = json.get("downloadCount").getAsInt();
        } else {
            this.downloadCount = null;
        }

        if(json.has("fileSizeOnDisk")) {
            this.fileSizeOnDisk = json.get("fileSizeOnDisk").getAsInt();
        } else {
            this.fileSizeOnDisk = null;
        }

        if (json.has("downloadUrl") && !json.get("downloadUrl").isJsonNull()) {
            this.downloadUrl = json.get("downloadUrl").getAsString();
        } else {
            if (this.id != null && this.fileName != null) {
                String fileIdStr = String.valueOf(this.id);
                if (fileIdStr.length() < 7) {
                    fileIdStr = String.format("%07d", this.id);
                }
                String part1 = fileIdStr.substring(0, 4);
                String part2 = fileIdStr.substring(4, 7);
                String download = "https://edge.forgecdn.net/files/" + part1 + "/" + part2 + "/" + this.fileName;
                this.downloadUrl = download.replace(" ", "%20");
            } else {
                this.downloadUrl = null;
            }
        }

        if(json.has("gameVersions")) {
            this.gameVersions = json.get("gameVersions").getAsJsonArray();
        } else {
            this.gameVersions = null;
        }

        if(json.has("sortableGameVersions")) {
            this.sortableGameVersions = json.get("sortableGameVersions").getAsJsonArray();
        } else {
            this.sortableGameVersions = null;
        }

        if(json.has("dependencies")) {
            this.dependencies = json.get("dependencies").getAsJsonArray();
        } else {
            this.dependencies = null;
        }

        if(json.has("exposeAsAlternative")) {
            this.exposeAsAlternative = json.get("exposeAsAlternative").getAsBoolean();
        } else {
            this.exposeAsAlternative = null;
        }

        if(json.has("parentProjectFileId")) {
            this.parentProjectFileId = json.get("parentProjectFileId").getAsInt();
        } else {
            this.parentProjectFileId = null;
        }

        if(json.has("alternateFileId")) {
            this.alternateFileId = json.get("alternateFileId").getAsInt();
        } else {
            this.alternateFileId = null;
        }

        if(json.has("isServerPack")) {
            this.isServerPack = json.get("isServerPack").getAsBoolean();
        } else {
            this.isServerPack = null;
        }

        if(json.has("serverPackFileId")) {
            this.serverPackFileId = json.get("serverPackFileId").getAsInt();
        } else {
            this.serverPackFileId = null;
        }

        if(json.has("isEarlyAccessContent")) {
            this.isEarlyAccessContent = json.get("isEarlyAccessContent").getAsBoolean();
        } else {
            this.isEarlyAccessContent = null;
        }

        if(json.has("earlyAccessEndDate")) {
            this.earlyAccessEndDate = json.get("earlyAccessEndDate").getAsString();
        } else {
            this.earlyAccessEndDate = null;
        }

        if(json.has("fileFingerprint")) {
            this.fileFingerprint = json.get("fileFingerprint").getAsInt();
        } else {
            this.fileFingerprint = null;
        }

        if(json.has("modules")) {
            this.modules = json.get("modules").getAsJsonArray();
        } else {
            this.modules = null;
        }
    }

    public String getUrl() {
        return url;
    }

    public JsonObject getJson() {
        return json;
    }

    public Integer getId() {
        return id;
    }

    public Integer getGameId() {
        return gameId;
    }

    public Integer getModId() {
        return modId;
    }

    public Boolean isAvailable() {
        return isAvailable;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFileName() {
        return fileName;
    }

    public Integer getReleaseType() {
        return releaseType;
    }

    public Integer getFileStatus() {
        return fileStatus;
    }

    public JsonArray getHashes() {
        return hashes;
    }

    public String getFileDate() {
        return fileDate;
    }

    public Integer getFileLength() {
        return fileLength;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public Integer getFileSizeOnDisk() {
        return fileSizeOnDisk;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public JsonArray getGameVersions() {
        return gameVersions;
    }

    public JsonArray getSortableGameVersions() {
        return sortableGameVersions;
    }

    public JsonArray getDependencies() {
        return dependencies;
    }

    public Boolean getExposeAsAlternative() {
        return exposeAsAlternative;
    }

    public Integer getParentProjectFileId() {
        return parentProjectFileId;
    }

    public Integer getAlternateFileId() {
        return alternateFileId;
    }

    public Boolean getIsServerPack() {
        return isServerPack;
    }

    public Integer getServerPackFileId() {
        return serverPackFileId;
    }

    public Boolean getIsEarlyAccessContent() {
        return isEarlyAccessContent;
    }

    public String getEarlyAccessEndDate() {
        return earlyAccessEndDate;
    }

    public Integer getFileFingerprint() {
        return fileFingerprint;
    }

    public JsonArray getModules() {
        return modules;
    }
}