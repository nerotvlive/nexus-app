package org.zyneonstudios.apex.nexusapp.main.console.commands;

import org.zyneonstudios.apex.nexusapp.main.NexusApplication;
import org.zyneonstudios.apex.nexusapp.main.console.NexusConsoleCommand;
import org.zyneonstudios.apex.nexusapp.utilities.ApplicationLogger;

public class HelpCommand extends NexusConsoleCommand {

    public HelpCommand() {
        super("help");
        addAliases("?","nexus","nexushelp","nexus?");
    }

    @Override
    public boolean run(String[] args) {
        ApplicationLogger logger = NexusApplication.getLogger();
        String separator = "============================================";

        logger.log(" ");
        logger.log("=(NEXUS APP)"+separator);
        logger.deb("NEXUS APP DEBUG MODE ENABLED");
        logger.log("NexusApplication version: "+NexusApplication.getInstance().getVersion());
        logger.log("For more help: https://apex.zyneonstudios.org/nexus-app/");
        logger.log(separator+"(NEXUS APP)=");
        logger.log(" ");

        System.gc();
        return true;
    }
}