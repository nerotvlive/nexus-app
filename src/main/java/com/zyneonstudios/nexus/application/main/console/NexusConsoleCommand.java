package com.zyneonstudios.nexus.application.main.console;

import java.util.ArrayList;
import java.util.Collection;

public abstract class NexusConsoleCommand {

    private final String name;
    private final ArrayList<String> aliases = new ArrayList<>();

    public NexusConsoleCommand(String commandName) {
        this.name = commandName.toLowerCase();
    }

    public final String getName() {
        return name;
    }

    public final String[] getAliases() {
        return aliases.toArray(new String[0]);
    }

    public final void addAlias(String commandAlias) {
        if(!aliases.contains(commandAlias.toLowerCase())) {
            aliases.add(commandAlias.toLowerCase().replace(" ","_"));
        }
    }

    public final void addAliases(String... commandAliases) {
        for(String alias : commandAliases) {
            addAlias(alias.toLowerCase().replace(" ","_"));
        }
    }

    public final void addAliases(Collection<String> commandAliases) {
        for(String alias : commandAliases) {
            addAlias(alias.toLowerCase().replace(" ","_"));
        }
    }

    public final void removeAlias(String commandAlias) {
        aliases.remove(commandAlias.toLowerCase().replace(" ","_"));
    }

    public abstract boolean run(String[] args);
}