package com.zyneonstudios.nexus.application.downloads;

import com.zyneonstudios.nexus.application.events.DownloadFinishEvent;
import com.zyneonstudios.nexus.application.main.NexusApplication;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * The {@code Download} class represents a single download process within the Nexus application.
 * It handles the downloading of a file from a given URL to a specified path, tracks the download's
 * progress, and provides information about the download's state, speed, and estimated remaining time.
 */
public class Download {

    // The current state of the download.
    private DownloadManager.DownloadState state = DownloadManager.DownloadState.WAITING;

    // The current percentage of the download as a string.
    private String percentString = "0%";

    // The start time of the download.
    private Instant startTime;

    // The finish time of the download (null if not finished).
    private Instant finishTime = null;

    // The number of bytes read in the last check.
    private long lastBytesRead = 0;

    // The total size of the file to be downloaded.
    private int fileSize = 0;

    // The unique identifier of the download.
    private final String id;

    // The name of the download.
    private final String name;

    // The path where the file will be saved.
    private final Path path;

    // The URL from which the file will be downloaded.
    private final URL url;

    // Whether the download is finished.
    private boolean finished = false;

    // The current percentage of the download (0-100).
    private double percent = 0;

    // The event to be triggered when the download is finished.
    private DownloadFinishEvent event = null;

    /**
     * Constructor for the Download.
     *
     * @param uuid        The unique identifier for the download.
     * @param downloadUrl The URL from which the file will be downloaded.
     * @param path        The path where the file will be saved.
     */
    public Download(UUID uuid, URL downloadUrl, Path path) {
        this.id = uuid.toString();
        this.name = this.id;
        this.path = path;
        this.url = downloadUrl;
    }

    /**
     * Constructor for the Download.
     *
     * @param name        The name of the download.
     * @param downloadUrl The URL from which the file will be downloaded.
     * @param path        The path where the file will be saved.
     */
    public Download(String name, URL downloadUrl, Path path) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.path = path;
        this.url = downloadUrl;
    }

    /**
     * Constructor for the Download.
     *
     * @param uuid        The unique identifier for the download.
     * @param name        The name of the download.
     * @param downloadUrl The URL from which the file will be downloaded.
     * @param path        The path where the file will be saved.
     */
    public Download(UUID uuid, String name, URL downloadUrl, Path path) {
        this.id = uuid.toString();
        this.name = name;
        this.path = path;
        this.url = downloadUrl;
    }

    /**
     * Sets the DownloadFinishEvent to be triggered when the download is finished.
     *
     * @param event The DownloadFinishEvent to be triggered.
     */
    public void setFinishEvent(DownloadFinishEvent event) {
        this.event = event;
    }

    /**
     * Starts the download process.
     *
     * @return True if the download started successfully, false otherwise.
     */
    public boolean start() {
        // Check if the download is in the waiting state.
        if (state == DownloadManager.DownloadState.WAITING) {
            state = DownloadManager.DownloadState.RUNNING;
            startTime = Instant.now();
            Instant lastTimeCheck = startTime;

            try {
                // Open a connection to the URL.
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                fileSize = connection.getContentLength();
                int responseCode = connection.getResponseCode();
                // Check if the response code is OK.
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    File outputFile = new File(path.toString());
                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    long totalBytesRead = 0;
                    // Read the file in chunks.
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        totalBytesRead += bytesRead;
                        Instant now = Instant.now();
                        Duration elapsedTime = Duration.between(lastTimeCheck, now);
                        setPercent((totalBytesRead * 100.0) / fileSize);
                        lastBytesRead = totalBytesRead;
                        lastTimeCheck = now;
                        outputStream.write(buffer, 0, bytesRead);
                        // Update the percentage every second.
                        if (elapsedTime.getSeconds() >= 1) {

                            totalBytesRead += bytesRead;
                            setPercent((totalBytesRead * 100.0) / fileSize);
                            String s = (int) percent + "%";
                            if (!percentString.equals(s)) {
                                percentString = s;
                            }
                        }
                    }
                    inputStream.close();
                    outputStream.close();
                    setFinished();
                    return true;
                }
            } catch (Exception e) {
                // Log any errors that occur during the download.
                NexusApplication.getLogger().err("Couldn't download \"" + url + "\" to \"" + path.toString() + "\": " + e.getMessage());
            }
        }
        // Set the state to failed if the download failed.
        state = DownloadManager.DownloadState.FAILED;
        if (event != null) {
            event.execute();
        }
        return false;
    }

    /**
     * Gets the name of the download.
     *
     * @return The name of the download.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the start time of the download.
     *
     * @return The start time of the download.
     */
    public Instant getStartTime() {
        return startTime;
    }

    /**
     * Gets the total size of the file to be downloaded.
     *
     * @return The total size of the file.
     */
    public int getFileSize() {
        return fileSize;
    }

    /**
     * Gets the number of bytes read in the last check.
     *
     * @return The number of bytes read in the last check.
     */
    public long getLastBytesRead() {
        return lastBytesRead;
    }

    /**
     * Gets the current percentage of the download as a string.
     *
     * @return The current percentage of the download as a string.
     */
    public String getPercentString() {
        return percentString;
    }

    /**
     * Gets the path where the file will be saved.
     *
     * @return The path where the file will be saved.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Gets the URL from which the file will be downloaded.
     *
     * @return The URL from which the file will be downloaded.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Gets the current state of the download.
     *
     * @return The current state of the download.
     */
    public DownloadManager.DownloadState getState() {
        return state;
    }

    /**
     * Gets the elapsed time since the download started.
     *
     * @return The elapsed time since the download started.
     */
    public Duration getElapsedTime() {
        if (finishTime != null) {
            return Duration.between(startTime, finishTime);
        }
        return Duration.between(startTime, Instant.now());
    }

    /**
     * Gets the current download speed in megabits per second (Mbps).
     *
     * @return The current download speed in Mbps.
     */
    public double getSpeedMbps() {
        Duration elapsed = getElapsedTime();
        if (elapsed.getSeconds() == 0) {
            return 0;
        }

        long bytesDownloaded = (long) (percent / 100.0 * fileSize);
        double d = (bytesDownloaded / 1024.0 / 1024.0) / elapsed.getSeconds();
        return Math.round((d * 100) / 100);
    }

    /**
     * Gets the estimated remaining time for the download to complete.
     *
     * @return The estimated remaining time.
     */
    public Duration getEstimatedRemainingTime() {
        double speedMbps = getSpeedMbps();
        if (speedMbps == 0) {
            return Duration.ZERO;
        }

        long remainingBytes = (long) ((100 - percent) / 100.0 * fileSize);
        double remainingSeconds = remainingBytes / 1024.0 / 1024.0 / speedMbps;
        return Duration.ofSeconds((long) remainingSeconds);
    }

    /**
     * Gets the unique identifier of the download.
     *
     * @return The unique identifier of the download.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the unique identifier of the download as a UUID.
     *
     * @return The unique identifier of the download as a UUID.
     */
    public UUID getUuid() {
        return UUID.fromString(id);
    }

    /**
     * Gets the current percentage of the download (0-100).
     *
     * @return The current percentage of the download.
     */
    public double getPercent() {
        return percent;
    }

    /**
     * Checks if the download is finished.
     *
     * @return True if the download is finished, false otherwise.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Sets the current percentage of the download.
     *
     * @param percent The current percentage of the download.
     */
    private void setPercent(double percent) {
        this.percent = percent;
        if (percent > 100) {
            setFinished();
        } else if (percent < 0) {
            this.percent = 0;
        }
    }

    /**
     * Sets whether the download is finished.
     */
    private void setFinished() {
        this.finished = true;
        if (event != null) {
            event.execute();
        }
        finishTime = Instant.now();
        percent = 100;
        percentString = "100%";
        state = DownloadManager.DownloadState.FINISHED;
    }
}