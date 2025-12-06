package com.zyneonstudios.nexus.application.main.console;

import com.zyneonstudios.nexus.application.main.NexusApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class NexusConsoleHandler {

    private volatile boolean running = false;
    private final Scanner inputScanner = new Scanner(System.in);
    private final ArrayList<NexusConsoleCommand> commands = new ArrayList<>();

    public void startReading() {
        if(!running) {
            running = true;
            CompletableFuture.runAsync(this::read);
        }
    }

    public NexusConsoleCommand[] getCommands() {
        return commands.toArray(new NexusConsoleCommand[0]);
    }

    public void addCommand(NexusConsoleCommand command) {
        if (!commands.contains(command)) {
            commands.add(command);
        }
    }

    public void addCommands(NexusConsoleCommand... commands) {
        for (NexusConsoleCommand command : commands) {
            addCommand(command);
        }
    }

    public void addCommands(Collection<NexusConsoleCommand> commands) {
        for (NexusConsoleCommand command : commands) {
            addCommand(command);
        }
    }

    public void removeCommand(NexusConsoleCommand command) {
        commands.remove(command);
    }

    private void read() {
        while (running) {
            String input = inputScanner.nextLine();
            if(!runCommand(input+" ")) {
                NexusApplication.getLogger().err("Command not found: "+input);
            }
        }
    }

    public boolean runCommand(String input) {
        NexusApplication.getLogger().deb("Processing command \""+input+"\"...");
        if(!commands.isEmpty()) {
            for (NexusConsoleCommand command : commands) {
                if (input.toLowerCase().startsWith(command.getName()+" ")) {
                    String[] cmd = input.split(" ");
                    return command.run(new ArrayList<>(Arrays.asList(cmd).subList(1, cmd.length)).toArray(new String[0]));
                } else {
                    for (String alias : command.getAliases()) {
                        if (input.toLowerCase().startsWith(alias+" ")) {
                            String[] cmd = input.split(" ");
                            return command.run(new ArrayList<>(Arrays.asList(cmd).subList(1, cmd.length)).toArray(new String[0]));
                        }
                    }
                }
            }
        }
        NexusApplication.getLogger().err("Command not found: "+input);
        return false;
    }

    public boolean hasCommand(String input) {
        for(NexusConsoleCommand command : commands) {
            if(input.equalsIgnoreCase(command.getName())) {
                return true;
            }
            for (String alias : command.getAliases()) {
                if (input.equalsIgnoreCase(alias)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void stop() {
        running = false;
        inputScanner.close();
    }
}