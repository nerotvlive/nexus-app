package org.zyneonstudios.apex.nexusapp.main.console.commands;

import org.zyneonstudios.apex.nexusapp.main.NexusApplication;
import org.zyneonstudios.apex.nexusapp.main.console.NexusConsoleCommand;

public class SettingsCommand extends NexusConsoleCommand {

    public SettingsCommand() {
        super("settings");
    }

    @Override
    public boolean run(String[] args) {
        if(args.length==3) {
            if(args[0].equalsIgnoreCase("set")) {
                NexusApplication.getInstance().getSettings().set(args[1], args[2]);
                return true;
            }
        }
        return false;
    }
}
