package org.zyneonstudios.apex.nexusapp.search.curseforge;

import fr.flowarg.flowupdater.utils.IOUtils;

import java.net.HttpURLConnection;
import java.net.URL;

public class CurseForgeIntegration {

    public static String makeRequest(String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("x-api-key", "$2a$10$KasKOdKA23HXYEGVR5oml.T4cG.jFMZnLhpZLPH4sCMwiAkGd7BaK");
            return IOUtils.getContent(connection.getInputStream());
        } catch (Exception e) {
            return null;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
}