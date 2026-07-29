package org.zyneonstudios.apex.nexusapp.search.modrinth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zyneonstudios.nexus.instance.ZynstanceBuilder;
import com.zyneonstudios.nexus.utilities.file.FileActions;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;
import com.zyneonstudios.nexus.utilities.strings.StringGenerator;
import org.apache.commons.io.FileUtils;
import org.zyneonstudios.apex.nexusapp.downloads.Download;
import org.zyneonstudios.apex.nexusapp.events.DownloadEndEvent;
import org.zyneonstudios.apex.nexusapp.main.NexusApplication;
import org.zyneonstudios.apex.nexusapp.search.modrinth.resource.ModrinthResource;
import org.zyneonstudios.apex.nexusapp.search.modrinth.resource.ModrinthResourceVersion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ModrinthIntegration {

    private static final Pattern MOD_PATTERN = Pattern.compile("data/([^/]+)/versions/([^/]+)/");

    public static void installModpack(File installDir, String projectId, String versionId) {
        JsonObject data = GsonUtility.getObject("https://api.modrinth.com/v2/version/"+versionId);
        ModrinthResource project = new ModrinthResource(projectId);
        installDir = getInstallDir(installDir,project.getSlug());

        String fileName = "modrinth-"+projectId+"-"+versionId+".mrpack";
        String downloadName = (NexusApplication.getInstance().getWorkingPath()+"/temp/"+fileName).replace("\\","/").replace("//","/");
        File download = new File(downloadName);
        if(download.exists()) {
            if(!download.delete()) {
                throw new RuntimeException("Failed to delete old download");
            }
        }

        if(data.has("files")) {
            JsonArray files = data.getAsJsonArray("files");
            if(!files.isEmpty()) {
                for(JsonElement file : files) {
                    JsonObject f = file.getAsJsonObject();
                    if(f.has("primary") && f.get("primary").getAsBoolean()) {
                        try {
                            Download metaDownload = new Download("Modrinth " + projectId + "-" + versionId + " metadata", new URL(f.get("url").getAsString()), download.toPath());
                            NexusApplication.getInstance().getDownloadManager().addDownload(metaDownload);
                            while (!metaDownload.isFinished()) {
                                Thread.sleep(1000);
                            }
                        } catch (Exception e) {
                            NexusApplication.getLogger().err(e.getMessage());
                            throw new RuntimeException(e);
                        }
                        break;
                    }
                }
            }
        }

        if(!download.exists()) {
            throw new NullPointerException("Downloaded file "+downloadName+" not found!");
        }

        String modrinthPackPath = installDir.getAbsolutePath();
        if(unzip(download.getAbsolutePath(), modrinthPackPath)) {
            File overrides = new File(modrinthPackPath+"/overrides/");
            if(overrides.exists()&&overrides.isDirectory()) {
                if(overrides.listFiles()!=null) {
                    for (File overrideFile : Objects.requireNonNull(overrides.listFiles())) {
                        if (overrideFile.isDirectory()) {
                            try {
                                File destFile = new File(overrides.getParent() + "/" + overrideFile.getName());
                                if (destFile.exists()) {
                                    FileActions.deleteFolder(destFile);
                                }
                                FileUtils.moveDirectory(overrideFile, destFile);
                            } catch (Exception e) {
                                NexusApplication.getLogger().err(e.getMessage());
                            }
                        }
                    }
                }
                FileActions.deleteFolder(overrides);
            }
            File index = new File(modrinthPackPath+"/modrinth.index.json");

            if(index.exists()) {
                try {
                    ModrinthDownload packDownload = new ModrinthDownload(project, installDir.toPath());
                    NexusApplication.getInstance().getDownloadManager().addDownload(packDownload);
                    JsonObject indexJson = NexusApplication.getInstance().getFastGson().fromJson(GsonUtility.getFromFile(index), JsonObject.class);
                    if (indexJson.has("files")) {
                        JsonArray contents = new JsonArray();
                        final double[] progress = {0};
                        final int[] finished = {0};
                        ArrayList<Download> fileDownloads = new ArrayList<>();
                        JsonArray files = indexJson.getAsJsonArray("files");
                        for (JsonElement file_ : files) {
                            JsonObject file = file_.getAsJsonObject();
                            String path = file.get("path").getAsString();
                            for (JsonElement downloads : file.getAsJsonArray("downloads")) {
                                String url = downloads.getAsString();
                                ModInfo info = extractInfo(url);
                                String slug = path;
                                String name = path.split("/")[path.split("/").length - 1];
                                String version = "Unknown version";
                                String resourceId = "null";
                                String author = "Modrinth user";
                                String link = "null";

                                if (info != null && info.modId != null && info.versionId != null) {
                                    ModrinthResource resource = new ModrinthResource(info.modId());
                                    ModrinthResourceVersion resourceVersion = new ModrinthResourceVersion(resource, info.versionId());
                                    slug = resource.getSlug();
                                    name = resource.getTitle();
                                    version = resourceVersion.getVersionNumber();
                                    resourceId = info.versionId;
                                    try {
                                        JsonObject authorObject = GsonUtility.getObject("https://api.modrinth.com/v2/user/" + resourceVersion.getAuthorId());
                                        if (authorObject.has("username")) {
                                            author = authorObject.get("username").getAsString();
                                        }
                                    } catch (Exception e) {
                                        author = resourceVersion.getAuthorId();
                                    }
                                    link = "modrinth";
                                }

                                JsonObject content = new JsonObject();
                                content.addProperty("id_or_slug", slug);
                                content.addProperty("name", name);
                                content.addProperty("author", author);
                                content.addProperty("version", version);
                                content.addProperty("versionId", resourceId);
                                content.addProperty("path", path);
                                content.addProperty("link", link);
                                contents.add(content);

                                try {
                                    File filePath = new File(installDir.getAbsolutePath() + "/" + file.get("path").getAsString());
                                    filePath.getParentFile().mkdirs();
                                    Download fileDownload = new Download(project.getTitle() + " " + file.get("path").getAsString(), new URL(url), filePath.toPath());
                                    fileDownloads.add(fileDownload);
                                } catch (Exception e) {
                                    NexusApplication.getLogger().err("Cannot download file \"" + file.get("path").getAsString() + "\" for modrinth pack \"" + project.getTitle() + "\": " + e.getMessage(), false);
                                }
                            }
                        }
                        try {
                            packDownload.setFileDownloads(fileDownloads);
                            packDownload.setPreparing(false);
                            File finalInstallDir = installDir;
                            packDownload.setFinishEvent(new DownloadEndEvent(packDownload) {
                                @Override
                                public boolean onFinish() {
                                    String version = indexJson.get("versionId").getAsString();
                                    String title = indexJson.get("name").getAsString();
                                    ZynstanceBuilder instanceConverter = new ZynstanceBuilder(finalInstallDir + "/zyneonInstance.json");
                                    instanceConverter.setName(title);
                                    instanceConverter.setVersion(version);
                                    instanceConverter.setId("modrinth-" + projectId);
                                    instanceConverter.setSummary(project.getDescription());
                                    instanceConverter.setDescription(project.getBody());
                                    JsonObject dependencies = indexJson.get("dependencies").getAsJsonObject();
                                    instanceConverter.setMinecraftVersion(dependencies.get("minecraft").getAsString());
                                    if (dependencies.has("fabric-loader")) {
                                        instanceConverter.setMetaProperty("modloader", "fabric");
                                        instanceConverter.setFabricVersion(dependencies.get("fabric-loader").getAsString());
                                    } else if (dependencies.has("forge")) {
                                        instanceConverter.setMetaProperty("modloader", "forge");
                                        instanceConverter.setForgeVersion(dependencies.get("forge").getAsString());
                                    } else if (dependencies.has("neoforge")) {
                                        instanceConverter.setMetaProperty("modloader", "neoforge");
                                        instanceConverter.setNeoForgeVersion(dependencies.get("neoforge").getAsString());
                                    } else if (dependencies.has("quilt-loader")) {
                                        instanceConverter.setMetaProperty("modloader", "quilt");
                                        instanceConverter.setQuiltVersion(dependencies.get("quilt-loader").getAsString());
                                    }
                                    instanceConverter.setDownloadUrl("modrinth");
                                    instanceConverter.setOriginUrl("local");
                                    ArrayList<String> tags = new ArrayList<>(Arrays.asList(project.getCategories()));
                                    tags.add("modrinth");
                                    instanceConverter.setTags(tags);

                                    ArrayList<String> authors = new ArrayList<>();
                                    JsonArray members = NexusApplication.getInstance().getFastGson().fromJson(GsonUtility.getFromURL("https://api.modrinth.com/v2/project/" + projectId + "/members"), JsonArray.class);
                                    for (JsonElement member : members) {
                                        authors.add(member.getAsJsonObject().get("user").getAsJsonObject().get("username").getAsString());
                                    }
                                    instanceConverter.setAuthors(authors);
                                    instanceConverter.setIconUrl(project.getIconUrl());

                                    instanceConverter.create();

                                    JsonStorage instanceContents = new JsonStorage(finalInstallDir + "/zyneonContents.json");
                                    instanceContents.set("contents", contents);

                                    if (NexusApplication.getInstance().getApplicationFrame().getBrowser().getURL().toLowerCase().contains("page=library")) {
                                        NexusApplication.getInstance().getApplicationFrame().getBrowser().reload();
                                    }
                                    return false;
                                }
                            });
                        } catch (Exception e) {
                            packDownload.setPreparing(false);
                            packDownload.cancel();
                            NexusApplication.getLogger().err(e.getMessage());
                            throw new RuntimeException(e);
                        }
                    } else {
                        packDownload.setPreparing(false);
                        packDownload.cancel();
                    }
                } catch (Exception e) {
                    NexusApplication.getLogger().err(e.getMessage());
                    throw new RuntimeException(e);
                }
            } else {
                NexusApplication.getLogger().err("Couldn't find Modrinth index json file: "+index.getAbsolutePath());
            }
        }

        System.gc();
        if(!download.delete()) {
            download.deleteOnExit();
        }
    }

    private static boolean unzip(String fileZip, String destDirPath) {
        File destDir = new File(destDirPath);
        if (!destDir.exists()) {
            NexusApplication.getLogger().deb("Created destination path: "+destDir.mkdirs());
        }
        try {
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String s = (destDir +"/"+ zipEntry.getName()).replace("\\","/").replace("//","/");
                File newFile = new File(s);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
            return true;
        } catch (Exception e) {
            NexusApplication.getLogger().err(e.getMessage());
        }
        return false;
    }

    private static File getInstallDir(File installDir, String id) {
        File bak = installDir;
        if(!installDir.getName().equalsIgnoreCase(id)) {
            installDir = new File(installDir.getAbsolutePath() + "/" + id.replace("/","-")+"/");
        }
        if(!installDir.exists()) {
            if(!installDir.mkdirs()) {
                throw new NullPointerException("Could not find or create instance directory \""+installDir.getAbsolutePath()+"\"");
            }
        } else {
            return getInstallDir(bak, id+"-"+ StringGenerator.generateAlphanumericString(8));
        }
        return installDir;
    }

    private static ModInfo extractInfo(String url) {
        Matcher matcher = MOD_PATTERN.matcher(url);
        if (matcher.find()) {
            return new ModInfo(matcher.group(1), matcher.group(2));
        }
        return null;
    }

    record ModInfo(String modId, String versionId) {
        @Override
        public String toString() {
            return String.format("Mod: %s | Version: %s", modId, versionId);
        }
    }
}