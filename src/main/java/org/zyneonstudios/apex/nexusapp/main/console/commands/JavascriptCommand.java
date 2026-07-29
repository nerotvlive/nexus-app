package org.zyneonstudios.apex.nexusapp.main.console.commands;

import org.zyneonstudios.apex.nexusapp.main.NexusApplication;
import org.zyneonstudios.apex.nexusapp.main.console.NexusConsoleCommand;

import java.util.Arrays;

public class JavascriptCommand extends NexusConsoleCommand {

    public JavascriptCommand() {
        super("javascript");
        addAliases("js");
    }

    @Override
    public boolean run(String[] args) {
        if(args.length == 0) {
            return false;
        } else {
            String command = Arrays.toString(args).replace("[", "").replace("]", "").replace(", ", " ");
            NexusApplication.getInstance().getApplicationFrame().executeJavaScript(command);
            return true;
        }
    }
}
