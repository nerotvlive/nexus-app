package org.zyneonstudios.apex.nexusapp.main.console.commands;

import org.zyneonstudios.apex.nexusapp.main.NexusApplication;
import org.zyneonstudios.apex.nexusapp.main.console.NexusConsoleCommand;

public class KillCommand extends NexusConsoleCommand {

    public KillCommand() {
        super("kill");
        addAliases("forcestop");
    }

    @Override
    public boolean run(String[] args) {
        if(args.length==1) {
            if(args[0].equalsIgnoreCase("-f")) {
                System.exit(-10);
                return true;
            }
        }
        NexusApplication.stop(0);
        return false;
    }
}
