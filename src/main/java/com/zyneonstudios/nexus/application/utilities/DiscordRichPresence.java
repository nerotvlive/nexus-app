package com.zyneonstudios.nexus.application.utilities;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import com.zyneonstudios.nexus.application.main.NexusApplication;

public class DiscordRichPresence {

    private static DiscordRPC discordRPC = DiscordRPC.INSTANCE;
    private static DiscordEventHandlers handlers = new DiscordEventHandlers();
    private static club.minnced.discord.rpc.DiscordRichPresence presence = new club.minnced.discord.rpc.DiscordRichPresence();
    private static Thread discordThread;

    public static void startRPC() {
        String applicationId = "1407342863204880639";
        handlers.ready = (user) -> NexusApplication.getLogger().deb("Successfully started Discord Rich Presence!");
        discordRPC.Discord_Initialize(applicationId, handlers, true, "");
        presence = new club.minnced.discord.rpc.DiscordRichPresence();
        presence.startTimestamp = System.currentTimeMillis() / 1000;
        presence.details = "Launching application...";
        discordRPC.Discord_UpdatePresence(presence);
        // in a worker thread
        discordThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                discordRPC.Discord_RunCallbacks();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }, "RPC-Callback-Handler");
        discordThread.start();
    }

    public static void stopRPC() {
        discordRPC.Discord_Shutdown();
        discordThread.interrupt();
        discordThread = null;
    }

    public static void setDetails(String details) {
        if(presence!=null&&discordThread!=null) {
            presence.details = details;
            discordRPC.Discord_UpdatePresence(presence);
        }
    }
}
