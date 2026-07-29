package org.zyneonstudios.apex.nexusapp.main;

import org.zyneonstudios.apex.nexusapp.events.DownloadEndEvent;
import org.zyneonstudios.apex.nexusapp.events.PageLoadedEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code NexusEventHandler} class manages event listeners for the Nexus application.
 * It provides methods to add, remove, and retrieve listeners for different types of events,
 * such as {@link DownloadEndEvent} and {@link PageLoadedEvent}.
 */
public class NexusEventHandler {

    // Lists to store event listeners
    private final List<DownloadEndEvent> DownloadEndEvents = new ArrayList<>();
    private final List<PageLoadedEvent> pageLoadedEvents = new ArrayList<>();

    /**
     * Gets the list of registered DownloadEndEvent listeners.
     *
     * @return The list of DownloadEndEvent listeners.
     */
    public List<DownloadEndEvent> getDownloadEndEvents() {
        return DownloadEndEvents;
    }

    /**
     * Adds a DownloadEndEvent listener to the list.
     *
     * @param event The DownloadEndEvent listener to add.
     */
    public void addDownloadEndEvent(DownloadEndEvent event) {
        if (!DownloadEndEvents.contains(event)) {
            DownloadEndEvents.add(event);
        }
    }

    /**
     * Removes a DownloadEndEvent listener from the list.
     *
     * @param event The DownloadEndEvent listener to remove.
     */
    public void removeDownloadEndEvent(DownloadEndEvent event) {
        DownloadEndEvents.remove(event);
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