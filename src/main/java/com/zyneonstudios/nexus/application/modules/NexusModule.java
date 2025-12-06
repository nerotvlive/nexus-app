package com.zyneonstudios.nexus.application.modules;

import java.util.ArrayList;
import java.util.Collections;

/**
 * The {@code NexusModule} class is an abstract base class for all modules within the Nexus application.
 * It defines the essential properties and lifecycle methods that each module must implement.
 * Modules extend this class to provide their specific functionality and metadata.
 */
public abstract class NexusModule {

    /**
     * Gets the unique identifier of the module.
     *
     * @return The module's ID.
     */
    public abstract String getModuleId();

    /**
     * Gets the human-readable name of the module.
     *
     * @return The module's name.
     */
    public abstract String getModuleName();

    /**
     * Gets the version of the module.
     *
     * @return The module's version.
     */
    public abstract String getModuleVersion();

    /**
     * Gets the owner of the module.
     *
     * @return The module's owner.
     */
    public abstract String getModuleOwner();

    /**
     * Gets the contributors of the module.
     *
     * @return An array of the module's contributors.
     */
    public abstract String[] getModuleContributors();

    /**
     * Called when the module is loaded.
     * This method is invoked after the module has been successfully read from its JAR file
     * and before it is activated.
     */
    public abstract void onLoad();

    /**
     * Called when the module is enabled.
     * This method is invoked when the module is activated and ready to function.
     */
    public abstract void onEnable();

    /**
     * Called when the module is disabled.
     * This method is invoked when the module is deactivated and should release any resources it holds.
     */
    public abstract void onDisable();

    /**
     * Gets all authors of the module, including the owner and contributors.
     *
     * @return An array of the module's authors.
     */
    public String[] getModuleAuthors() {
        ArrayList<String> authors = new ArrayList<>();
        authors.add(getModuleOwner());
        if (getModuleContributors() != null) {
            Collections.addAll(authors, getModuleContributors());
        }
        return authors.toArray(new String[0]);
    }
}