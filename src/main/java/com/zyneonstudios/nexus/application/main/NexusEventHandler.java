package com.zyneonstudios.nexus.application.main;

import com.zyneonstudios.nexus.application.events.DownloadFinishEvent;
import com.zyneonstudios.nexus.application.events.PageLoadedEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code NexusEventHandler} class manages event listeners for the Nexus application.
 * It provides methods to add, remove, and retrieve listeners for different types of events,
 * such as {@link DownloadFinishEvent} and {@link PageLoadedEvent}.
 */
public class NexusEventHandler {

    // Lists to store event listeners
    private final List<DownloadFinishEvent> downloadFinishEvents = new ArrayList<>();
    private final List<PageLoadedEvent> pageLoadedEvents = new ArrayList<>();

    /**
     * Gets the list of registered DownloadFinishEvent listeners.
     *
     * @return The list of DownloadFinishEvent listeners.
     */
    public List<DownloadFinishEvent> getDownloadFinishEvents() {
        return downloadFinishEvents;
    }

    /**
     * Adds a DownloadFinishEvent listener to the list.
     *
     * @param event The DownloadFinishEvent listener to add.
     */
    public void addDownloadFinishEvent(DownloadFinishEvent event) {
        if (!downloadFinishEvents.contains(event)) {
            downloadFinishEvents.add(event);
        }
    }

    /**
     * Removes a DownloadFinishEvent listener from the list.
     *
     * @param event The DownloadFinishEvent listener to remove.
     */
    public void removeDownloadFinishEvent(DownloadFinishEvent event) {
        downloadFinishEvents.remove(event);
    }

    /**
     * Gets the list of registered PageLoadedEvent listeners.
     *
     * @return The list of PageLoadedEvent listeners.
     */
    public List<PageLoadedEvent> getPageLoadedEvents() {
        return pageLoadedEvents;
    }

    /**
     * Adds a PageLoadedEvent listener to the list.
     *
     * @param event The PageLoadedEvent listener to add.
     */
    public void addPageLoadedEvent(PageLoadedEvent event) {
        if (!pageLoadedEvents.contains(event)) {
            pageLoadedEvents.add(event);
        }
    }

    /**
     * Removes a PageLoadedEvent listener from the list.
     *
     * @param event The PageLoadedEvent listener to remove.
     */
    public void removePageLoadedEvent(PageLoadedEvent event) {
        pageLoadedEvents.remove(event);
    }
}