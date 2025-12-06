package com.zyneonstudios.nexus.application.main.console.commands;

import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.application.main.console.NexusConsoleCommand;
import com.zyneonstudios.nexus.application.search.modrinth.ModrinthIntegration;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class ModrinthCommand extends NexusConsoleCommand {

    public ModrinthCommand() {
        super("modrinth-install");
        addAlias("labrinth-install");
    }

    @Override
    public boolean run(String[] args) {
        if(args.length>1) {
            String projectId = args[0];
            String versionId = args[1];
            CompletableFuture.runAsync(()-> ModrinthIntegration.installModpack(new File(NexusApplication.getInstance().getLocalSettings() .getDefaultMinecraftPath()),projectId,versionId));
            return true;
        }
        NexusApplication.getLogger().err("Syntax error: modrinth-install <project_id_or_slug> <versionId>");
        return false;
    }
}
