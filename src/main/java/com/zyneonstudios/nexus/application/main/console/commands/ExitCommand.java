package com.zyneonstudios.nexus.application.main.console.commands;

import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.application.main.console.NexusConsoleCommand;

public class ExitCommand extends NexusConsoleCommand {

    public ExitCommand() {
        super("exit");
        addAliases("quit","end","stop","shutdown","close");
    }

    @Override
    public boolean run(String[] args) {
        NexusApplication.stop(0);
        return true;
    }
}