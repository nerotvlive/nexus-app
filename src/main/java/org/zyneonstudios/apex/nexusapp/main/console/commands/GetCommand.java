package org.zyneonstudios.apex.nexusapp.main.console.commands;

import org.zyneonstudios.apex.nexusapp.main.NexusApplication;
import org.zyneonstudios.apex.nexusapp.main.console.NexusConsoleCommand;

import java.util.Arrays;

public class GetCommand extends NexusConsoleCommand {

    public GetCommand() {
        super("get");
        addAliases("download","search","install");
    }

    @Override
    public boolean run(String[] args) {
        if(args.length>0) {
            String query = Arrays.toString(args).replace("[", "").replace("]", "").replace(", ", " ");
            NexusApplication.getInstance().getApplicationFrame().executeJavaScript("loadPage('discover.html',false,\"&dt=search&q=" + query + "\");");
            return true;
        }
        NexusApplication.getLogger().err("Syntax error: get <identifier>");
        return false;
    }
}
