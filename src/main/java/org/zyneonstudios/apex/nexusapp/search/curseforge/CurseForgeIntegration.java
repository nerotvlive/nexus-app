package org.zyneonstudios.apex.nexusapp.search.curseforge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zyneonstudios.nexus.instance.ZynstanceBuilder;
import com.zyneonstudios.nexus.utilities.file.FileActions;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;
import com.zyneonstudios.nexus.utilities.strings.StringGenerator;
import fr.flowarg.flowupdater.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.zyneonstudios.apex.nexusapp.downloads.Download;
import org.zyneonstudios.apex.nexusapp.events.DownloadEndEvent;
import org.zyneonstudios.apex.nexusapp.main.NexusApplication;
import org.zyneonstudios.apex.nexusapp.search.curseforge.resource.CurseForgeResource;
import org.zyneonstudios.apex.nexusapp.search.curseforge.resource.CurseForgeResourceVersion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CurseForgeIntegration {

    private static final int BUFFER_SIZE = 8192;
    private static final String API_KEY = "$2a$10$KasKOdKA23HXYEGVR5oml.T4cG.jFMZnLhpZLPH4sCMwiAkGd7BaK";

    public static void installModpack(File installDir, int projectId, int versionId) {
        CurseForgeResource project = new CurseForgeResource(projectId);
        CurseForgeResourceVersion version = new CurseForgeResourceVersion(projectId, versionId);

        String slug;
        if (!isBlank(project.getSlug())) {
            slug = project.getSlug();
        } else {
            slug = String.valueOf(project.getId());
        }
        installDir = getInstallDir(installDir, slug);

        String versionName;
        if (!isBlank(version.getDisplayName())) {
            versionName = version.getDisplayName();
        } else {
            versionName = String.valueOf(versionId);
        }

        String fileName = "curseforge-" + slug + "-" + versionName.replace(".zip", "") + ".zip";
        File tempDir = new File(NexusApplication.getInstance().getWorkingPath(), "temp");
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new IllegalStateException("Failed to create temp dir: " + tempDir.getAbsolutePath());
        }
        File download = new File(tempDir, fileName);
        if (download.exists()) {
            if (!download.delete()) {
                throw new IllegalStateException("Failed to delete old download: " + download.getAbsolutePath());
            }
        }

        try {
            if (isBlank(version.getDownloadUrl())) {
                throw new IllegalStateException("Missing download URL for CurseForge pack");
            }
            Download metaDownload = new Download("CurseForge " + slug + "-" + versionName + " metadata", new URI(version.getDownloadUrl()).toURL(), download.toPath());
            NexusApplication.getInstance().getDownloadManager().addDownload(metaDownload);
            while (!metaDownload.isFinished()) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            NexusApplication.getLogger().err(e.getMessage());
            throw new RuntimeException(e);
        }

        if (!download.exists()) {
            throw new IllegalStateException("Downloaded file not found: " + download.getAbsolutePath());
        }

        String curseForgePackPath = installDir.getAbsolutePath();
        if (unzip(download.getAbsolutePath(), curseForgePackPath)) {
            File overrides = new File(curseForgePackPath + "/overrides/");
            if (overrides.exists() && overrides.isDirectory()) {
                if (overrides.listFiles() != null) {
                    for (File overrideFile : Objects.requireNonNull(overrides.listFiles())) {
                        try {
                            File destFile = new File(overrides.getParent() + "/" + overrideFile.getName());
                            if (destFile.exists()) {
                                FileActions.deleteFolder(destFile);
                            }
                            if (overrideFile.isDirectory()) {
                                FileUtils.moveDirectory(overrideFile, destFile);
                            } else {
                                FileUtils.moveFile(overrideFile, destFile);
                            }
                        } catch (Exception e) {
                            NexusApplication.getLogger().err(e.getMessage());
                        }
                    }
                }
                FileActions.deleteFolder(overrides);
            }
            File index = new File(curseForgePackPath + "/manifest.json");
            if (index.exists()) {
                JsonObject indexJson = NexusApplication.getInstance().getFastGson().fromJson(GsonUtility.getFromFile(index), JsonObject.class);

                if (indexJson.has("files")) {
                    try {
                        CurseForgeDownload packDownload = new CurseForgeDownload(project, installDir.toPath());
                        NexusApplication.getInstance().getDownloadManager().addDownload(packDownload);
                        ArrayList<Download> fileDownloads = new ArrayList<>();
                        JsonArray files = indexJson.getAsJsonArray("files");
                        JsonArray contents = new JsonArray();
                        for (JsonElement file_ : files) {

                            JsonObject fileData = file_.getAsJsonObject();
                            int pId = fileData.get("projectID").getAsInt();
                            int fId = fileData.get("fileID").getAsInt();

                            CurseForgeResource resource = new CurseForgeResource(pId);
                            CurseForgeResourceVersion file = new CurseForgeResourceVersion(pId, fId);

                            String resourceVersion = file.getDisplayName();
                            String author = "CurseForge user";
                            try {
                                JsonObject authorObject = resource.getAuthors().get(0).getAsJsonObject();
                                if (authorObject.has("name")) {
                                    author = authorObject.get("name").getAsString();
                                }
                            } catch (Exception ignore) {
                            }

                            String link = "null";


                            String path = "mods/";
                            if (resource.getClassId() == 5) {
                                path = "plugins/";
                            } else if (resource.getClassId() == 12) {
                                path = "resourcepacks/";
                            } else if (resource.getClassId() == 17) {
                                path = "worlds/";
                            } else if (resource.getClassId() == 6552) {
                                path = "shaderpacks/";
                            } else if (resource.getClassId() == 6945) {
                                path = "datapacks/";
                            }


                            String url = file.getDownloadUrl();
                            try {
                                File filePath = new File(installDir.getAbsolutePath() + "/" + path + file.getFileName());
                                filePath.getParentFile().mkdirs();
                                Download fileDownload = new Download(project.getName() + " " + path + file.getFileName(), new URI(url).toURL(), filePath.toPath());

                                link = "curseforge";
                                JsonObject content = new JsonObject();
                                content.addProperty("id_or_slug", pId);
                                content.addProperty("name", resource.getName());
                                content.addProperty("author", author);
                                content.addProperty("version", resourceVersion);
                                content.addProperty("versionId", fId);
                                content.addProperty("path", path + file.getFileName());
                                content.addProperty("link", link);
                                contents.add(content);

                                fileDownloads.add(fileDownload);
                            } catch (Exception e) {
                                NexusApplication.getLogger().err("Cannot download file \"" + path + file.getFileName() + "\" for curseforge pack \"" + project.getName() + "\": " + e.getMessage(), false);
                            }

                        }

                        try {
                            packDownload.setFileDownloads(fileDownloads);
                            packDownload.setPreparing(false);
                            File finalInstallDir = installDir;
                            packDownload.setFinishEvent(new DownloadEndEvent(packDownload) {
                                @Override
                                public boolean onFinish() {
                                    String title = project.getName();
                                    ZynstanceBuilder instanceConverter = new ZynstanceBuilder(finalInstallDir + "/zyneonInstance.json");
                                    instanceConverter.setName(title);
                                    instanceConverter.setVersion(versionName);
                                    instanceConverter.setId("curseforge-" + slug);
                                    instanceConverter.setSummary(project.getSummary());
                                    instanceConverter.setDescription(project.getSummary());
                                    if (indexJson.has("minecraft")) {
                                        JsonObject dependencies = indexJson.get("minecraft").getAsJsonObject();
                                        if (dependencies.has("version")) {
                                            instanceConverter.setMinecraftVersion(dependencies.get("version").getAsString());
                                        }

                                        if (dependencies.has("modLoaders")) {
                                            JsonArray modLoaders = dependencies.getAsJsonArray("modLoaders");
                                            for (JsonElement loader_ : modLoaders) {
                                                JsonObject loader = loader_.getAsJsonObject();
                                                if (loader.has("primary") && loader.get("primary").getAsBoolean()) {
                                                    String[] mId = loader.get("id").getAsString().split("-", 2);
                                                    if (mId[0].equalsIgnoreCase("fabric")) {
                                                        instanceConverter.setMetaProperty("modloader", "fabric");
                                                        instanceConverter.setFabricVersion(mId[1]);
                                                    } else if (mId[0].equalsIgnoreCase("forge")) {
                                                        instanceConverter.setMetaProperty("modloader", "forge");
                                                        instanceConverter.setForgeVersion(mId[1]);
                                                    } else if (mId[0].equalsIgnoreCase("quilt")) {
                                                        instanceConverter.setMetaProperty("modloader", "quilt");
                                                        instanceConverter.setQuiltVersion(mId[1]);
                                                    } else if (mId[0].equalsIgnoreCase("neoforge")) {
                                                        instanceConverter.setMetaProperty("modloader", "neoforge");
                                                        instanceConverter.setNeoForgeVersion(mId[1]);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    instanceConverter.setDownloadUrl("curseforge");
                                    instanceConverter.setOriginUrl("local");
                                    ArrayList<String> tags = new ArrayList<>();

                                    if (project.getCategories() != null) {
                                        for (JsonElement category : project.getCategories()) {
                                            JsonObject cat = category.getAsJsonObject();
                                            tags.add(cat.get("name").getAsString());
                                        }
                                    }

                                    tags.add("curseforge");
                                    instanceConverter.setTags(tags);
                                    ArrayList<String> authors = new ArrayList<>();

                                    if (project.getAuthors() != null) {
                                        for (JsonElement author : project.getAuthors()) {
                                            JsonObject auth = author.getAsJsonObject();
                                            authors.add(auth.get("name").getAsString());
                                        }
                                    }

                                    instanceConverter.setAuthors(authors);

                                    if (project.getLogo() != null && project.getLogo().has("url")) {
                                        instanceConverter.setIconUrl(project.getLogo().get("url").getAsString());
                                    }

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
                    } catch (Exception e) {
                        NexusApplication.getLogger().err(e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            } else {
                NexusApplication.getLogger().err("Couldn't find CurseForge manifest json file: " + index.getAbsolutePath());
            }
        }

        System.gc();
        if (!download.delete()) {
            download.deleteOnExit();
        }
    }

    private static boolean unzip(String fileZip, String destDirPath) {
        File destDir = new File(destDirPath);
        if (!destDir.exists()) {
            NexusApplication.getLogger().deb("Created destination path: " + destDir.mkdirs());
        }
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            String destDirCanonical = destDir.getCanonicalPath() + File.separator;
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip))) {
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    File newFile = new File(destDir, zipEntry.getName());
                    String newFileCanonical = newFile.getCanonicalPath();
                    if (!newFileCanonical.startsWith(destDirCanonical)) {
                        throw new IOException("Blocked zip entry outside target dir: " + zipEntry.getName());
                    }
                    if (zipEntry.isDirectory()) {
                        if (!newFile.isDirectory() && !newFile.mkdirs()) {
                            throw new IOException("Failed to create directory " + newFile);
                        }
                    } else {
                        File parent = newFile.getParentFile();
                        if (!parent.isDirectory() && !parent.mkdirs()) {
                            throw new IOException("Failed to create directory " + parent);
                        }
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
            }
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

    public static String accessAPI(String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("x-api-key", API_KEY);
            int status = connection.getResponseCode();
            if (status >= 200 && status < 300) {
                return IOUtils.getContent(connection.getInputStream());
            }
            String errorBody = connection.getErrorStream() != null ? IOUtils.getContent(connection.getErrorStream()) : "";
            throw new IOException("CurseForge API request failed (" + status + "): " + errorBody);
        } catch (Exception e) {
            NexusApplication.getLogger().err(e.getMessage());
            return null;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
