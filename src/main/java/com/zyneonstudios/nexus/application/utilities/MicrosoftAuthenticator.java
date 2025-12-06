package com.zyneonstudios.nexus.application.utilities;

import com.google.gson.JsonArray;
import com.starxg.keytar.Keytar;
import com.zyneonstudios.nexus.application.Main;
import com.zyneonstudios.nexus.application.main.NexusApplication;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import live.nerotv.zyneon.auth.ZyneonAuth;

import java.util.*;

public class MicrosoftAuthenticator {

    private static AuthInfos authInfos = null;
    private static ArrayList<String> authenticatedUUIDs;

    public static void startLogin(boolean save) {
        showOverlay(false);

        try {
            setAuthInfos(ZyneonAuth.getAuthInfos(), save);
        } catch (Exception exception) {
            NexusApplication.getLogger().printErr("NEXUS","AUTHENTICATION","Couldn't fetch the Microsoft token.",exception.getMessage(), exception.getStackTrace());
        }

        refreshBrowser();
    }

    public static void refresh(String token, boolean save) {
        showOverlay(true);

        try {
            setAuthInfos(ZyneonAuth.getAuthInfos(token), save);
        } catch (Exception exception) {
            NexusApplication.getLogger().printErr("NEXUS","AUTHENTICATION","Couldn't refresh the Microsoft token.",exception.getMessage(), exception.getStackTrace());
        }

        refreshBrowser();
    }

    private static void setAuthInfos(HashMap<ZyneonAuth.AuthInfo, String> authData, boolean save) {
        authInfos = new AuthInfos(authData.get(ZyneonAuth.AuthInfo.USERNAME), authData.get(ZyneonAuth.AuthInfo.ACCESS_TOKEN), authData.get(ZyneonAuth.AuthInfo.UUID));
        NexusApplication.setAuthInfos(authInfos);
        if(save) {
            save(authData);
        }
    }

    private static void save(HashMap<ZyneonAuth.AuthInfo, String> authData) {
        try {
            String UUID = Base64.getEncoder().encodeToString(authData.get(ZyneonAuth.AuthInfo.UUID).getBytes());
            String token = Base64.getEncoder().encodeToString(authData.get(ZyneonAuth.AuthInfo.REFRESH_TOKEN).getBytes());
            Keytar.getInstance().setPassword("ZNA||00||00","0",UUID);
            Keytar.getInstance().setPassword("ZNA||01||00",UUID+"_0",token);
            NexusApplication.getInstance().getData().ensure("data.authentication.uuids",new JsonArray());
            if(!authenticatedUUIDs.contains(UUID)) {
                authenticatedUUIDs.add(UUID);
            }
            NexusApplication.getInstance().getData().set("data.authentication.uuids",authenticatedUUIDs);
            NexusApplication.getInstance().getData().set("data.authentication.names."+UUID,Base64.getEncoder().encodeToString(getUsername().getBytes()));
        } catch (Exception e) {
            NexusApplication.getLogger().printErr("NEXUS","AUTHENTICATION","Couldn't save credentials.",e.getMessage(), e.getStackTrace());
        }
    }

    public static void logout() {
        showOverlay(true);
        logout(authInfos.getUuid());
    }

    public static void logout(String decryptedUUID) {
        showOverlay(true);
        try {
            String encryptedUUID = Base64.getEncoder().encodeToString(decryptedUUID.getBytes());
            Keytar.getInstance().deletePassword("ZNA||00||00","0");
            Keytar.getInstance().deletePassword("ZNA||01||00",encryptedUUID+"_0");
            NexusApplication.getInstance().getData().ensure("data.authentication.uuids",new JsonArray());
            if(authenticatedUUIDs.contains(encryptedUUID)) {
                authenticatedUUIDs.remove(encryptedUUID);
                NexusApplication.getInstance().getData().set("data.authentication.uuids",authenticatedUUIDs);
            }
            NexusApplication.getInstance().getData().delete("data.authentication.names."+encryptedUUID);
        } catch (Exception e) {
            NexusApplication.getLogger().printErr("NEXUS","AUTHENTICATION","Couldn't delete credentials.",e.getMessage(), e.getStackTrace());
        }

        if(Objects.equals(getUUID(), decryptedUUID)) {
            authInfos = null;
            NexusApplication.setAuthInfos(null);
        }

        refreshBrowser();

        if(authInfos == null) {
            if (!authenticatedUUIDs.isEmpty()) {
                try {
                    refresh(new String(Base64.getDecoder().decode(Keytar.getInstance().getPassword("ZNA||01||00", authenticatedUUIDs.getFirst() + "_0"))), true);
                } catch (Exception e) {
                    NexusApplication.getLogger().printErr("NEXUS", "AUTHENTICATION", "Couldn't refresh the Microsoft token.", e.getMessage(), e.getStackTrace());
                }
            }
        }
    }

    public static String getUUID() {
        if(authInfos!=null) {
            return authInfos.getUuid();
        }
        return null;
    }

    public static String getUsername() {
        if(authInfos!=null) {
            return authInfos.getUsername();
        }
        return null;
    }

    public static boolean isLoggedIn() {
        return authInfos != null;
    }

    public static void init() {
        NexusApplication.getInstance().getData().ensure("data.authentication.uuids",new JsonArray());
        authenticatedUUIDs = (ArrayList<String>)NexusApplication.getInstance().getData().get("data.authentication.uuids");
    }

    public static List<String> getAuthenticatedUUIDs() {
        return List.copyOf(authenticatedUUIDs);
    }

    public static String getAuthenticatedUsername(String UUID) {
        if(authenticatedUUIDs.contains(UUID)) {
            return NexusApplication.getInstance().getData().getString("data.authentication.names."+UUID);
        }
        return null;
    }

    public static List<String> getDecryptedAuthenticatedUUIDs() {
        ArrayList<String> decryptedUUIDs = new ArrayList<>();
        for(String s:authenticatedUUIDs) {
            decryptedUUIDs.add(new String(Base64.getDecoder().decode(s)));
        }
        return decryptedUUIDs;
    }

    public static String getDecryptedAuthenticatedUsername(String UUID) {
        String name = null;
        if(authenticatedUUIDs.contains(UUID)) {
            name = NexusApplication.getInstance().getData().getString("data.authentication.names."+UUID);
        } else if(authenticatedUUIDs.contains(Base64.getEncoder().encodeToString(UUID.getBytes()))) {
            UUID = Base64.getEncoder().encodeToString(UUID.getBytes());
            name = NexusApplication.getInstance().getData().getString("data.authentication.names."+UUID);
        }
        return new String(Base64.getDecoder().decode(name));
    }

    private static void refreshBrowser() {
        if(NexusApplication.getInstance().getApplicationFrame() != null) {
            if(NexusApplication.getInstance().getApplicationFrame().getBrowser().getURL().contains("page=settings")) {
                NexusApplication.getInstance().getApplicationFrame().getBrowser().loadURL(NexusApplication.getInstance().isOnlineUI() ? "https://nerofynetwork.github.io/NEXUS-App/src/main/html/index.html?page=settings.html&st=account-settings&app=true" : "localhost:" + Main.getPort() + "/index.html?page=settings.html&st=account-settings&app=true");
            } else if(NexusApplication.getInstance().getApplicationFrame().getBrowser().getURL().contains("page=library")||NexusApplication.getInstance().getApplicationFrame().getBrowser().getURL().contains("page=login")) {
                NexusApplication.getInstance().getApplicationFrame().getBrowser().reload();
            }
        }
    }

    private static void showOverlay(boolean pleaseWait) {
        if(NexusApplication.getInstance().getApplicationFrame()!=null) {
            if(pleaseWait) {
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.getElementById('login-overlay').innerText = 'Please wait...';");
            }
            NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.getElementById('login-overlay').classList.add('active');");
        }
    }
}