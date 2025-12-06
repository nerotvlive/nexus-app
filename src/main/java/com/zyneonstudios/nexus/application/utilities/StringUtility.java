package com.zyneonstudios.nexus.application.utilities;

import com.zyneonstudios.nexus.utilities.strings.StringGenerator;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class StringUtility extends StringGenerator {

    public static String encodeData(String searchTerm) {
        try {
            return URLEncoder.encode(searchTerm, StandardCharsets.UTF_8).replace("+", "%20");
        } catch (Exception e) {
            return searchTerm.replace(" ", "%20");
        }
    }
}
