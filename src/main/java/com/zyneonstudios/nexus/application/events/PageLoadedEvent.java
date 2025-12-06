package com.zyneonstudios.nexus.application.events;

import com.zyneonstudios.nexus.utilities.events.Event;

import java.util.UUID;

/**
 * The {@code PageLoadedEvent} class is an abstract event that is triggered when a new page is loaded
 * in the application's web browser. It provides information about the loaded URL and allows
 * modules to react to page load events.
 */
public abstract class PageLoadedEvent implements Event {

    // The unique identifier for this event instance.
    private final UUID uuid = UUID.randomUUID();

    // The URL of the loaded page.
    private String url;

    /**
     * Constructor for the PageLoadedEvent.
     *
     * @param page The URL of the page that was loaded.
     */
    public PageLoadedEvent(String page) {
        this.url = page;
    }

    /**
     * Gets the URL of the loaded page.
     *
     * @return The URL of the loaded page.
     */
    public final String getUrl() {
        return url;
    }

    /**
     * Sets the URL of the loaded page.
     *
     * @param url The new URL of the loaded page.
     */
    public final void setUrl(String url) {
        this.url = url;
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
     * Executes the event. This method calls the {@link #onLoad()} method.
     *
     * @return True if the event was successfully executed, false otherwise.
     */
    @Override
    public final boolean execute() {
        return onLoad();
    }

    /**
     * Called when the page is loaded.
     * This method should be implemented by subclasses to define the actions to be taken
     * when a page is loaded.
     *
     * @return True if the event was successfully handled, false otherwise.
     */
    public abstract boolean onLoad();
}