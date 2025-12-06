package com.zyneonstudios.nexus.application.downloads;

import com.zyneonstudios.nexus.application.main.NexusRunner;
import com.zyneonstudios.nexus.application.main.NexusApplication;

import java.util.HashMap;
import java.util.UUID;

/**
 * The {@code DownloadManager} class is responsible for managing downloads within the Nexus application.
 * It provides methods to add, remove, and track downloads, as well as access to the application's
 * {@link NexusRunner}.
 */
public class DownloadManager {

    // Map to store downloads, keyed by their unique UUID.
    private final HashMap<UUID, Download> downloads = new HashMap<>();

    // The NexusRunner instance associated with this DownloadManager.
    private final NexusRunner runner;

    /**
     * Constructor for the DownloadManager.
     *
     * @param application The main NexusApplication instance.
     */
    public DownloadManager(NexusApplication application) {
        this.runner = application.getRunner();
    }

    /**
     * Gets the NexusRunner instance associated with this DownloadManager.
     *
     * @return The NexusRunner instance.
     */
    public NexusRunner getRunner() {
        return runner;
    }

    /**
     * Gets a map of all managed downloads, keyed by their UUIDs.
     *
     * @return A map of download UUIDs to Download instances.
     */
    public HashMap<UUID, Download> getDownloads() {
        return downloads;
    }

    /**
     * Adds a new download to the manager.
     *
     * @param download The Download instance to add.
     * @return True if the download was successfully added, false otherwise (e.g., if a download with the same UUID already exists).
     */
    public boolean addDownload(Download download) {
        // Check if a download with the same UUID or the same instance already exists.
        if (!downloads.containsKey(download.getUuid()) && !downloads.containsValue(download)) {
            downloads.put(download.getUuid(), download);
            return true;
        }
        return false;
    }

    /**
     * Removes a download from the manager by its instance.
     *
     * @param download The Download instance to remove.
     * @return True if the download was successfully removed, false otherwise.
     */
    public boolean removeDownload(Download download) {
        // Check if the download instance exists in the manager.
        if (downloads.containsValue(download)) {
            downloads.remove(download.getUuid(), download);
            return true;
        }
        return false;
    }

    /**
     * Removes a download from the manager by its UUID.
     *
     * @param uuid The UUID of the download to remove.
     * @return True if the download was successfully removed, false otherwise.
     */
    public boolean removeDownload(UUID uuid) {
        // Check if a download with the given UUID exists.
        if (downloads.containsKey(uuid)) {
            downloads.remove(uuid);
            return true;
        }
        return false;
    }

    /**
     * Enum representing the possible states of a download.
     */
    public enum DownloadState {
        WAITING,
        RUNNING,
        FINISHED,
        FAILED
    }
}