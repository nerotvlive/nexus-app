package com.zyneonstudios.nexus.application.main.console.commands;

import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.application.main.console.NexusConsoleCommand;
import com.zyneonstudios.nexus.application.search.zyndex.local.LocalInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class LaunchCommand extends NexusConsoleCommand {

    private static final Logger log = LoggerFactory.getLogger(LaunchCommand.class);

    public LaunchCommand() {
        super("launch");
        addAliases("start","run","play","l","s","r","p");
    }

    @Override
    public boolean run(String[] args) {
        if(args.length>0) {
            String identifier = Arrays.toString(args).replace("[","").replace("]","").replace(", "," ");
            if(searchInstance(identifier)!=null) {
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("console.log(\"[CONNECTOR] library.start."+searchInstance(identifier).getPath()+"\");");
                return true;
            } else {
                NexusApplication.getLogger().err("Instance not found: "+identifier);
            }
        } else {
            NexusApplication.getLogger().err("Syntax error: launch <identifier>");
        }
        System.gc();
        return false;
    }

    public LocalInstance searchInstance(String query) {
        query = query.toLowerCase();
        for(LocalInstance instance : NexusApplication.getInstance().getInstanceManager().getInstances().values()) {
            String title = instance.getInstance().getName().toLowerCase();
            String id = instance.getInstance().getId().toLowerCase();
            String path = instance.getPath().toLowerCase();
            if(title.contains(query) || id.equals(query) || path.equals(query)) {
                return instance;
            }
        }
        return null;
    }
}