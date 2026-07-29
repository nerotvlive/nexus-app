package org.zyneonstudios.apex.nexusapp.main.console.commands;

import org.zyneonstudios.apex.nexusapp.main.NexusApplication;
import org.zyneonstudios.apex.nexusapp.main.console.NexusConsoleCommand;

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