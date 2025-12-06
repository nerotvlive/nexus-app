package com.zyneonstudios.nexus.application.events;

import com.zyneonstudios.nexus.application.downloads.Download;
import com.zyneonstudios.nexus.utilities.events.Event;

import java.util.UUID;

/**
 * The {@code DownloadFinishEvent} class is an abstract event that is triggered when a download
 * process is completed. It provides information about the finished download and allows
 * modules to react to download completion events.
 */
public abstract class DownloadFinishEvent implements Event {

    // The unique identifier for this event instance.
    private final UUID uuid = UUID.randomUUID();

    // The download that has finished.
    private final Download download;

    /**
     * Constructor for the DownloadFinishEvent.
     *
     * @param download The download that has finished.
     */
    public DownloadFinishEvent(Download download) {
        this.download = download;
    }

    /**
     * Gets the download that has finished.
     *
     * @return The finished download.
     */
    public Download getDownload() {
        return download;
    }

    /**
     * Gets the unique identifier of this event.
     *
     * @return The UUID of this event.
     */
    @Override
    public final UUID getUUID() {
        return uuid;
    }

    /**
     * Executes the event. This method calls the {@link #onFinish()} method.
     *
     * @return True if the event was successfully executed, false otherwise.
     */
    @Override
    public final boolean execute() {
        return onFinish();
    }

    /**
     * Called when the download is finished.
     * This method should be implemented by subclasses to define the actions to be taken
     * when a download is finished.
     *
     * @return True if the event was successfully handled, false otherwise.
     */
    public abstract boolean onFinish();
}