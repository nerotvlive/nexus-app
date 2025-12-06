package com.zyneonstudios.nexus.application.main;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.zyneonstudios.nexus.application.downloads.Download;
import com.zyneonstudios.nexus.application.downloads.DownloadManager;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The {@code NexusRunner} class is responsible for running tasks at a fixed rate.
 * It uses a {@link ScheduledExecutorService} to schedule the execution of the {@link #run()} method.
 */
public class NexusRunner {

    /**
     * The unique identifier for this runner instance.
     */
    private final UUID runnerID = UUID.randomUUID();

    private UUID downloading = null;

    /**
     * Indicates whether the runner has been started.
     */
    private boolean started = false;

    /**
     * The executor service used to schedule tasks.
     */
    private ScheduledExecutorService executor;

    /**
     * Gets the unique identifier of this runner.
     *
     * @return The UUID of the runner.
     */
    public UUID getRunnerID() {
        return runnerID;
    }

    /**
     * Checks if the runner has been started.
     *
     * @return {@code true} if the runner has been started, {@code false} otherwise.
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Gets the executor service used by this runner.
     *
     * @return The {@link ScheduledExecutorService} instance.
     */
    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    /**
     * Starts the runner, scheduling the {@link #run()} method to be executed at a fixed rate.
     * If the runner is already started, this method does nothing.
     */
    public void start() {
        if (!started) {
            started = true;
            executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(this::run, 0, 1, TimeUnit.SECONDS);
            NexusApplication.getLogger().log("Started runner with ID: " + runnerID);
        }
    }

    /**
     * The method that is executed periodically by the runner.
     * Currently, it does nothing but can be extended to perform specific tasks.
     */
    int c = 0; int u = 119;
    protected void run() {
        if (!started) {
            started = true;
            return;
        }

        if(c == 300) {
            c = 0;
            System.gc();
        }

        CompletableFuture.runAsync(()-> {
            if(downloading != null) {
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.getElementById('downloads-button').style.color = 'var(--nex-primary)';");
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.getElementById('downloads-button').querySelector('span').style.textShadow = '0 0 0.3rem var(--nex-primary)';");
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.getElementById('downloads-button').querySelector('i').style.textShadow = '0 0 0.3rem var(--nex-primary)';");
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.getElementById('downloads-icon').classList.add('downloading');");
            } else {
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.getElementById('downloads-button').style.color = '';");
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.getElementById('downloads-button').querySelector('span').style.textShadow = '';");
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.getElementById('downloads-button').querySelector('i').style.textShadow = '';");
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.getElementById('downloads-icon').classList.remove('downloading');");
            }

            if (downloading != null) {
                Download download = NexusApplication.getInstance().getDownloadManager().getDownloads().get(downloading);
                if (download.isFinished()) {
                    downloading = null;
                }
            } else {
                NexusApplication.getInstance().getDownloadManager().getDownloads().forEach((uuid, download) -> {
                    if (download.getState().equals(DownloadManager.DownloadState.WAITING)) {
                        downloading = uuid;
                        download.start();
                    }
                });
            }
        });

        if (NexusApplication.getInstance().getApplicationFrame().getBrowser().getURL().contains("page=downloads")) {
            NexusApplication.getInstance().getDownloadManager().getDownloads().forEach((uuid, download) -> {
                if (download.getState().equals(DownloadManager.DownloadState.WAITING)) {
                    String title = "setDownload(\"" + download.getName().replace("\"", "''") + "\",";
                    String state = "\"" + download.getState().toString().replace("\"", "''") + "\",";
                    String elapsedTime = "\"0 seconds\",";
                    String downloadSpeed = "\"0 mb/s\",";
                    String remainingTime = "\"\",";
                    String downloadSize = "\"\",";
                    String fileSize = "\"0 mb\",";
                    String path = "\"" + download.getPath().toString().replace("\\","/").replace("\"", "''") + "\",";
                    String url = "\"" + download.getUrl().toString().replace("\\","/").replace("\"", "''") + "\",";
                    String id = "\"" + download.getId().replace("\"", "''") + "\",";
                    String progress = "\"" + download.getPercentString() + "\",";
                    String percent = download.getPercent() + ");";
                    String command = title + state + elapsedTime + downloadSpeed + remainingTime + downloadSize + fileSize + path + url + id + progress + percent;
                    NexusApplication.getInstance().getApplicationFrame().executeJavaScript(command);
                } else if (download.getState().equals(DownloadManager.DownloadState.RUNNING)) {
                    String title = "setDownload(\"" + download.getName().replace("\"", "''") + "\",";
                    String state = "\"" + download.getState().toString().replace("\"", "''") + "\",";
                    String elapsedTime = "\"" + download.getElapsedTime().getSeconds() + " seconds\",";
                    String downloadSpeed = "\"" + (int) download.getSpeedMbps() + " mb/s\",";
                    String remainingTime = "\"" + download.getEstimatedRemainingTime().getSeconds() + " seconds\",";
                    String downloadSize = "\"" + (download.getFileSize() / 1000) / 1000 + " mb\",";
                    String fileSize = "\"" + (download.getLastBytesRead() / 1000) / 1000 + " mb\",";
                    String path = "\"" + download.getPath().toString().replace("\\","/").replace("\"", "''") + "\",";
                    String url = "\"" + download.getUrl().toString().replace("\\","/").replace("\"", "''") + "\",";
                    String id = "\"" + download.getId().replace("\"", "''") + "\",";
                    String progress = "\"" + (int)download.getPercent() + "%\",";
                    String percent = download.getPercent() + ");";
                    String command = title + state + elapsedTime + downloadSpeed + remainingTime + downloadSize + fileSize + path + url + id + progress + percent;
                    NexusApplication.getInstance().getApplicationFrame().executeJavaScript(command);
                } else if (download.getState().equals(DownloadManager.DownloadState.FINISHED) || download.getState().equals(DownloadManager.DownloadState.FAILED)) {
                    String title = "setDownload(\"" + download.getName().replace("\"", "''") + "\",";
                    String state = "\"" + download.getState().toString().replace("\"", "''") + "\",";
                    String elapsedTime = "\"" + download.getElapsedTime().getSeconds() + " seconds\",";
                    String downloadSpeed = "\"0 mb/s\",";
                    String remainingTime = "\"" + download.getEstimatedRemainingTime().getSeconds() + " seconds\",";
                    String downloadSize = "\"" + (download.getFileSize() / 1000) / 1000 + " mb\",";
                    String fileSize = downloadSize;
                    String path = "\"" + download.getPath().toString().replace("\\","/").replace("\"", "''") + "\",";
                    String url = "\"" + download.getUrl().toString().replace("\\","/").replace("\"", "''") + "\",";
                    String id = "\"" + download.getId().replace("\"", "''") + "\",";
                    String progress = "\"" + download.getPercentString() + "\",";
                    String percent = download.getPercent() + ");";
                    String command = title + state + elapsedTime + downloadSpeed + remainingTime + downloadSize + fileSize + path + url + id + progress + percent;
                    NexusApplication.getInstance().getApplicationFrame().executeJavaScript(command);
                }
            });
        }

        try {
            JsonObject json = new Gson().fromJson(GsonUtility.getFromURL("https://raw.githubusercontent.com/zyneonstudios/nexus-nex/main/application/index.json"), JsonObject.class).getAsJsonArray("versions").get(0).getAsJsonObject();
            checkVersion(json);
        } catch (Exception ignore) {
        }
    }

    private boolean runUpdateCheck(JsonObject json) {
        /*NexusApplication.getLogger().dbg("[RUNNER] Checking for Updates...");
        NexusApplication.getLogger().dbg("[RUNNER] Parsed JSON Data...");
        String v = json.get("info").getAsJsonObject().get("version").getAsString();
        NexusApplication.getLogger().dbg("[RUNNER] Latest version: " + v + "...");
        NexusApplication.getLogger().dbg("[RUNNER] Current version: " + NexusApplication.getInstance().getVersion() + "...");
        if (!v.equals(NexusApplication.getInstance().getVersion())) {
            NexusApplication.getLogger().dbg("[RUNNER] The application is not up to date!");
            return true;
        }*/

        return false;
    }

    private void checkVersion(JsonObject json) {
        u++;
        if (u > 120) {
            u = 0;
            if (runUpdateCheck(json)) {
                NexusApplication.getLogger().dbg("[RUNNER] Sending notification...");
                //TODO: Application.getFrame().sendNotification("Update available!", "Version " + v + " has been released!", "<a onclick=\"callJavaMethod('button.exit');\" class='button'>Install</a><a onclick=\"callJavaMethod('button.online');\" class='button'>Dynamic update</a>", v, true);
            }
        }
    }
}