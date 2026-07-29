package org.zyneonstudios.apex.nexusapp.utilities;

import com.zyneonstudios.nexus.utilities.system.OperatingSystem;
import org.zyneonstudios.apex.nexusapp.main.NexusApplication;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class NativeUtility {

    private static String activebgColorCode = "#000000";
    private static String backgroundColorCode = "#000000";
    private static String foregroundColorCode = "#ffffff";

    public static Color getBackgroundColor() {
        return Color.decode(backgroundColorCode);
    }

    public static Color getActiveBGColor() {
        return Color.decode(activebgColorCode);
    }

    public static Color getForegroundColor() {
        return Color.decode(foregroundColorCode);
    }

    public static String getBackgroundColorCode() {
        return backgroundColorCode;
    }

    public static String getActiveBGColorCode() {
        return activebgColorCode;
    }

    public static String getForegroundColorCode() {
        return foregroundColorCode;
    }

    public static void setActiveBGColor(Color color) {
        activebgColorCode = "#" + Integer.toHexString(color.getRGB() & 0xFFFFFF);
    }

    private static void setBackgroundColor(Color color) {
        backgroundColorCode = "#" + Integer.toHexString(color.getRGB() & 0xFFFFFF);
    }

    private static void setForegroundColor(Color color) {
        foregroundColorCode = "#" + Integer.toHexString(color.getRGB() & 0xFFFFFF);
    }

    private static void setActiveBGColor(int color) {
        activebgColorCode = "#" + Integer.toHexString(color);
    }

    private static void setBackgroundColor(int color) {
        backgroundColorCode = "#" + Integer.toHexString(color);
    }

    private static void setForegroundColor(int color) {
        foregroundColorCode = "#" + Integer.toHexString(color);
    }

    private static void setActiveBGColor(Color color, int alpha) {
        activebgColorCode = "#" + Integer.toHexString(color.getRGB() & 0xFFFFFF | (alpha << 24));
    }

    private static void setBackgroundColor(Color color, int alpha) {
        backgroundColorCode = "#" + Integer.toHexString(color.getRGB() & 0xFFFFFF | (alpha << 24));
    }

    private static void setForegroundColor(Color color, int alpha) {
        foregroundColorCode = "#" + Integer.toHexString(color.getRGB() & 0xFFFFFF | (alpha << 24));
    }

    private static void setActiveBGColor(int color, int alpha) {
        activebgColorCode = "#" + Integer.toHexString(color & 0xFFFFFF | (alpha << 24));
    }

    private static void setBackgroundColor(int color, int alpha) {
        backgroundColorCode = "#" + Integer.toHexString(color | (alpha << 24));
    }

    private static void setForegroundColor(int color, int alpha) {
        foregroundColorCode = "#" + Integer.toHexString(color | (alpha << 24));
    }

    private static void setActiveBGColor(String colorCode) {
        activebgColorCode = colorCode;
    }

    private static void setBackgroundColor(String colorCode) {
        backgroundColorCode = colorCode;
    }

    private static void setForegroundColor(String colorCode) {
        foregroundColorCode = colorCode;
    }

    public static void readColors() {
        try {
            if(!NexusApplication.getInstance().getLocalSettings().useNativeWindow()) {
                readFallbackColors();
            } else if (OperatingSystem.getType() == OperatingSystem.Type.Windows) {
                readWindowsColors();
            } else if (OperatingSystem.getType() == OperatingSystem.Type.Linux) {
                readLinuxColors();
            } else if (OperatingSystem.getType() == OperatingSystem.Type.macOS) {
                readMacColors();
            } else {
                throw new UnsupportedOperationException("[NativeUtility] This method is only supported on Windows 10/11+, Linux (GTK/QT) and macOS. Reading native colors is not supported on: " + OperatingSystem.getType() + ". Falling back to default colors!");
            }
        } catch (Exception e) {
            NexusApplication.getLogger().err("[NativeUtility] Failed to read native colors. Falling back to default colors!");
            readFallbackColors();
        }
    }

    private static void readWindowsColors() {
        NexusApplication.getLogger().deb("[NativeUtility] Trying to read Windows(10/11+) theme native colors...");
        if(OperatingSystem.getType() == OperatingSystem.Type.Windows) {
            NexusApplication.getLogger().deb("[NativeUtility] Falling back to default colors because Windows 10/11+ doesn't support custom themes.");
            readFallbackColors();
        } else {
            throw new UnsupportedOperationException("[NativeUtility] This method is only supported on Windows(10/11+).");
        }
    }

    private static void readLinuxColors() {
        NexusApplication.getLogger().deb("[NativeUtility] Trying to read Linux(GTK/QT) theme native colors...");
        if(OperatingSystem.getType() == OperatingSystem.Type.Linux) {
            String desktopEnv = System.getenv("XDG_CURRENT_DESKTOP");
            if (desktopEnv == null) desktopEnv = "";
            desktopEnv = desktopEnv.toUpperCase();
            try {
                if (desktopEnv.contains("KDE") || desktopEnv.contains("LXQT")) {
                    NexusApplication.getLogger().deb("[NativeUtility] Detected Qt-based Desktop Environment.");
                    readLinuxQtColors();
                } else {
                    NexusApplication.getLogger().deb("[NativeUtility] Detected GTK-based Desktop Environment.");
                    readGtkColors();
                }
            } catch (Exception e) {
                throw new RuntimeException("[NativeUtility] Found no supported Desktop Environment. Falling back to default colors!", e);
            }
        } else {
            throw new UnsupportedOperationException("[NativeUtility] This method is only supported on Linux.");
        }
    }

    private static void readLinuxQtColors() {
        NexusApplication.getLogger().deb("[NativeUtility] Trying to read Linux(QT6) theme native colors...");
        String agConfig = executeCommand("kreadconfig6", "--group", "Colors:Window", "--key", "BackgroundAlternate");
        if (agConfig.isEmpty()) {
            NexusApplication.getLogger().err("[NativeUtility] Could not read Linux(QT6) theme native colors. Falling back to Linux(QT5) colors and try again...");
            NexusApplication.getLogger().deb("[NativeUtility] Trying to read Linux(QT5) theme native colors...");
            agConfig = executeCommand("kreadconfig5", "--group", "Colors:Window", "--key", "BackgroundAlternate");
        }

        String bgConfig = executeCommand("kreadconfig6", "--group", "Colors:Window", "--key", "BackgroundNormal");
        if (bgConfig.isEmpty()) {
            bgConfig = executeCommand("kreadconfig5", "--group", "Colors:Window", "--key", "BackgroundNormal");
        }

        String fgConfig = executeCommand("kreadconfig6", "--group", "Colors:Window", "--key", "ForegroundNormal");
        if (fgConfig.isEmpty()) {
            fgConfig = executeCommand("kreadconfig5", "--group", "Colors:Window", "--key", "ForegroundNormal");
        }

        if (!bgConfig.isEmpty() && !fgConfig.isEmpty()) {
            try {
                String agHexCode = rgbStringToHex( agConfig );
                String bgHexCode = rgbStringToHex( bgConfig );
                String fgHexCode = rgbStringToHex( fgConfig );
                NexusApplication.getLogger().deb("[NativeUtility] Qt ActiveBGCo (HEX): " + agHexCode);
                NexusApplication.getLogger().deb("[NativeUtility] Qt Background (HEX): " + bgHexCode);
                NexusApplication.getLogger().deb("[NativeUtility] Qt Foreground (HEX): " + fgHexCode);
                setActiveBGColor(agHexCode);
                setForegroundColor(fgHexCode);
                setBackgroundColor(bgHexCode);
            } catch (Exception e) {
                e.printStackTrace();
                NexusApplication.getLogger().err("[NativeUtility] Could not parse Qt colors: "+e.getMessage());
                throw new RuntimeException("[NativeUtility] Could not parse Qt colors. Falling back to default colors!", e);
            }
        } else {
            NexusApplication.getLogger().deb("[NativeUtility] Could not fetch Qt colors. Missing kreadconfig?");
        }
    }

    private static void readGtkColors() {
        NexusApplication.getLogger().deb("[NativeUtility] Trying to read Linux(GTK) theme native colors...");
        try {
            String colorScheme = executeCommand("gsettings", "get", "org.gnome.desktop.interface", "color-scheme");
            String gtkTheme = executeCommand("gsettings", "get", "org.gnome.desktop.interface", "gtk-theme").toLowerCase();

            boolean isDark = colorScheme.contains("prefer-dark") || gtkTheme.contains("dark");
            boolean isBreeze = gtkTheme.contains("breeze");

            String agHexCode;
            String bgHexCode;
            String fgHexCode;

            if (isBreeze) {
                if (isDark) {
                    NexusApplication.getLogger().deb("[NativeUtility] GTK Breeze Dark Theme detected.");
                    bgHexCode = "#232629";
                    agHexCode = "#31363B";
                    fgHexCode = "#FCFCFC";
                } else {
                    NexusApplication.getLogger().deb("[NativeUtility] GTK Breeze Light Theme detected.");
                    bgHexCode = "#EFF0F1";
                    agHexCode = "#E3E5E7";
                    fgHexCode = "#232629";
                }
            } else {
                if (isDark) {
                    NexusApplication.getLogger().deb("[NativeUtility] GTK Adwaita Dark Mode detected.");
                    bgHexCode = "#242424";
                    agHexCode = "#303030";
                    fgHexCode = "#FFFFFF";
                } else {
                    NexusApplication.getLogger().deb("[NativeUtility] GTK Adwaita Light Mode detected.");
                    bgHexCode = "#FAFAFA";
                    agHexCode = "#EBEBEB";
                    fgHexCode = "#000000";
                }
            }

            NexusApplication.getLogger().deb("[NativeUtility] GTK ActiveBGCo (HEX): " + agHexCode);
            NexusApplication.getLogger().deb("[NativeUtility] GTK Background (HEX): " + bgHexCode);
            NexusApplication.getLogger().deb("[NativeUtility] GTK Foreground (HEX): " + fgHexCode);
            setActiveBGColor(agHexCode);
            setBackgroundColor(bgHexCode);
            setForegroundColor(fgHexCode);
        } catch (Exception e) {
            NexusApplication.getLogger().err("[NativeUtility] Could not detect GTK color scheme: " + e.getMessage());
            throw new RuntimeException("[NativeUtility] Could not detect GTK colors. Falling back to default colors!", e);
        }
    }

    private static void readMacColors() {
        NexusApplication.getLogger().deb("[NativeUtility] Trying to read macOS theme native colors...");
        if(OperatingSystem.getType() == OperatingSystem.Type.macOS) {
            NexusApplication.getLogger().deb("[NativeUtility] Falling back to default colors because we haven't implemented macOS native theme support yet.");
            readFallbackColors();
        } else {
            throw new UnsupportedOperationException("[NativeUtility] This method is only supported on Mac.");
        }
    }

    private static void readFallbackColors() {
        if(isDarkMode()) {
            NexusApplication.getLogger().deb("[NativeUtility] Detected dark mode. Setting colors to default dark mode colors. [FALLBACK]");
            setActiveBGColor("#000000");
            setBackgroundColor("#000000");
            setForegroundColor("#ffffff");
        } else {
            NexusApplication.getLogger().deb("[NativeUtility] Detected light mode. Setting colors to default light mode colors. [FALLBACK]");
            setActiveBGColor("#ffffff");
            setBackgroundColor("#ffffff");
            setForegroundColor("#000000");
        }
    }

    public static boolean isDarkMode() {
        try {
            if (OperatingSystem.getType() == OperatingSystem.Type.Windows) {
                NexusApplication.getLogger().deb("[NativeUtility] Trying to read Windows(10/11) native theme mode...");
                return isWindowsDarkMode();
            } else if (OperatingSystem.getType() == OperatingSystem.Type.Linux) {
                NexusApplication.getLogger().deb("[NativeUtility] Trying to read Linux(GTK/QT) native theme mode...");
                return isLinuxDarkMode();
            } else if (OperatingSystem.getType() == OperatingSystem.Type.macOS) {
                NexusApplication.getLogger().deb("[NativeUtility] Trying to read macOS native theme mode...");
                return isMacDarkMode();
            } else {
                NexusApplication.getLogger().deb("[NativeUtility] Detected unsupported operating system for reading native theme mode: " + OperatingSystem.getType() + "!");
                throw new UnsupportedOperationException("[NativeUtility] This method is only supported on Windows 10+, Linux (GTK/QT) and macOS. Reading native theme mode is not supported on: " + OperatingSystem.getType() + ". Falling back to light mode!");
            }
        } catch (Exception e) {
            String theme = "light";
            if(NexusApplication.getInstance().getLocalSettings().getTheme().equalsIgnoreCase("dark")) {
                theme = "dark";
            }
            NexusApplication.getLogger().err("[NativeUtility] Failed to read native theme mode. Falling back to "+theme+" mode!");
            return theme.equalsIgnoreCase("dark");
        }
    }

    private static boolean isWindowsDarkMode() {
        NexusApplication.getLogger().deb("[NativeUtility] Trying to read Windows(10/11) native theme mode...");
        if (OperatingSystem.getType() == OperatingSystem.Type.Windows) {
            try {
                String command = "reg query HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize /v AppsUseLightTheme";
                Process process = Runtime.getRuntime().exec(command);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("REG_DWORD") && line.contains("0x0")) {
                        NexusApplication.getLogger().deb("[NativeUtility] Detected Windows(10/11) native theme mode: Dark Mode");
                        return true;
                    }
                }
                process.waitFor();
            } catch (Exception e) {
                NexusApplication.getLogger().err("[NativeUtility] Failed to read Windows native theme mode. Using light mode colors.");
            }

            NexusApplication.getLogger().deb("[NativeUtility] Detected Windows(10/11) native theme mode: Light Mode");
            return false;
        } else {
            throw new UnsupportedOperationException("[NativeUtility] This method (isWindowsDarkMode:boolean) is only supported on Windows!");
        }
    }

    private static boolean isLinuxDarkMode() {
        NexusApplication.getLogger().deb("[NativeUtility] Trying to read Linux(GTK/QT) native theme mode...");
        if(OperatingSystem.getType() == OperatingSystem.Type.Linux) {
            String desktopEnv = System.getenv("XDG_CURRENT_DESKTOP");
            if (desktopEnv == null) desktopEnv = "";
            desktopEnv = desktopEnv.toUpperCase();
            try {
                if (desktopEnv.contains("KDE") || desktopEnv.contains("LXQT")) {
                    NexusApplication.getLogger().deb("[NativeUtility] Detected Qt-based Desktop Environment.");
                    return isQtDarkMode();
                } else {
                    NexusApplication.getLogger().deb("[NativeUtility] Detected GTK-based Desktop Environment.");
                    return isGtkDarkMode();
                }
            } catch (Exception e) {
                throw new RuntimeException("[NativeUtility] Found no supported Desktop Environment. Falling back to light mode!", e);
            }
        } else {
            throw new UnsupportedOperationException("[NativeUtility] This method (isLinuxDarkMode:boolean) is only supported on Linux!");
        }
    }

    private static boolean isQtDarkMode() {
        NexusApplication.getLogger().deb("[NativeUtility] Trying to read Linux(QT) native theme mode...");
        if(OperatingSystem.getType() == OperatingSystem.Type.Linux) {
            try {
                NexusApplication.getLogger().deb("[NativeUtility] Trying to read Linux(QT6) native theme mode...");
                String bgConfig = executeCommand("kreadconfig6", "--group", "Colors:Window", "--key", "BackgroundNormal");
                if (bgConfig.isEmpty()) {
                    NexusApplication.getLogger().err("[NativeUtility] Could not read Linux(QT6) native theme mode. Falling back to Linux(QT5) colors and try again...");
                    NexusApplication.getLogger().deb("[NativeUtility] Trying to read Linux(QT5) native theme mode...");
                    bgConfig = executeCommand("kreadconfig5", "--group", "Colors:Window", "--key", "BackgroundNormal");
                }

                if (!bgConfig.isEmpty()) {
                    String[] parts = bgConfig.split(",");
                    if (parts.length >= 3) {
                        int r = Integer.parseInt(parts[0].trim());
                        int g = Integer.parseInt(parts[1].trim());
                        int b = Integer.parseInt(parts[2].trim());
                        double luminance = (0.299 * r) + (0.587 * g) + (0.114 * b);
                        boolean isDark = luminance < 128;
                        NexusApplication.getLogger().deb("[NativeUtility] Qt Background (Luminance/Dark Mode): " + luminance+"/"+isDark);
                        return isDark;
                    }
                }
            } catch (Exception ignore) {}
            NexusApplication.getLogger().deb("[NativeUtility] Could not fetch Linux(QT) native theme mode. Falling back to light mode!");
            return false;
        } else {
            throw new UnsupportedOperationException("[NativeUtility] This method (isQtDarkMode:boolean) is only supported on Linux!");
        }
    }

    private static boolean isGtkDarkMode() {
        NexusApplication.getLogger().deb("[NativeUtility] Trying to read Linux(GTK) native theme mode...");
        if(OperatingSystem.getType() == OperatingSystem.Type.Linux) {
            try {
                String colorScheme = executeCommand("gsettings", "get", "org.gnome.desktop.interface", "color-scheme");
                String gtkTheme = executeCommand("gsettings", "get", "org.gnome.desktop.interface", "gtk-theme").toLowerCase();
                boolean isDark = colorScheme.contains("prefer-dark") || gtkTheme.contains("dark");
                NexusApplication.getLogger().deb("[NativeUtility] GTK Background (Dark Mode): " + isDark);
                return isDark;
            } catch (Exception ignore) {}
            NexusApplication.getLogger().deb("[NativeUtility] Could not fetch Linux(GTK) native theme mode. Falling back to light mode!");
            return false;
        } else {
            throw new UnsupportedOperationException("[NativeUtility] This method (isGtkDarkMode:boolean) is only supported on Linux!");
        }
    }

    private static boolean isMacDarkMode() {
        NexusApplication.getLogger().deb("[NativeUtility] Trying to read macOS native theme mode...");
        if(OperatingSystem.getType() == OperatingSystem.Type.macOS) {
            boolean isDark = false;
            try {
                isDark = "NSAppearanceNameDarkAqua".equals(Toolkit.getDefaultToolkit().getDesktopProperty("apple.awt.application.appearance"));
            } catch (Exception e) {
                NexusApplication.getLogger().deb("[NativeUtility] Could not fetch macOS native theme mode. Falling back to light mode! Error: "+e.getMessage());
            }
            NexusApplication.getLogger().deb("[NativeUtility] macOS Background (Dark Mode): " + isDark);
            return isDark;
        } else {
            throw new UnsupportedOperationException("[NativeUtility] This method (isMacDarkMode:boolean) is only supported on macOS!");
        }
    }

    private static String executeCommand(String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(Arrays.asList(command));
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            process.waitFor();
            return output.toString().trim().replace("'", "");
        } catch (IOException | InterruptedException e) {
            return "";
        }
    }

    private static String rgbStringToHex(String rgbStr) {
        if (rgbStr == null || rgbStr.trim().isEmpty()) {
            return "";
        }

        String[] parts = rgbStr.split(",");
        if (parts.length >= 3) {
            try {
                int r = Integer.parseInt(parts[0].trim());
                int g = Integer.parseInt(parts[1].trim());
                int b = Integer.parseInt(parts[2].trim());

                return String.format("#%02X%02X%02X", r, g, b);
            } catch (NumberFormatException e) {
                NexusApplication.getLogger().err("[NativeUtility] Failed to parse RGB values: " + rgbStr);
            }
        }
        return "";
    }

    public static boolean useDarkMode() {
        if(NexusApplication.getInstance().getLocalSettings().getTheme().equalsIgnoreCase("dark")) {
            try {
                return isDarkMode();
            } catch (Exception e) {
                return true;
            }
        } else if(NexusApplication.getInstance().getLocalSettings().getTheme().equalsIgnoreCase("light")) {
            return false;
        } else {
            try {
                return isDarkMode();
            } catch (Exception e) {
                return false;
            }
        }
    }
}