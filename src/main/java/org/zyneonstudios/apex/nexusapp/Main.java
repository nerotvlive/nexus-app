package org.zyneonstudios.apex.nexusapp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.zyneonstudios.nexus.desktop.NexusDesktop;
import com.zyneonstudios.nexus.utilities.file.FileGetter;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;
import com.zyneonstudios.nexus.utilities.strings.StringGenerator;
import com.zyneonstudios.nexus.utilities.system.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.zyneonstudios.apex.nexusapp.frame.ZyneonSplash;
import org.zyneonstudios.apex.nexusapp.main.NexusApplication;
import org.zyneonstudios.apex.nexusapp.utilities.ApplicationLogger;
import org.zyneonstudios.apex.nexusapp.utilities.DiscordRichPresence;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The {@code Main} class is the primary entry point for the Nexus application.
 * It initializes the application, parses command-line arguments, starts the web server (if necessary),
 * and launches the main application frame. It also handles the application's lifecycle and provides
 * access to the application's logger and port.
 */
@SpringBootApplication
public class Main {

    // Application Logger
    private static final ApplicationLogger logger = new ApplicationLogger("NEXUS");
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static String[] args;

    // Application Configuration
    private static String path = getDefaultPath();
    private static String ui = null;
    private static int port = 8094;
    private static final String INSTANCE_LOCK_FILE = ".nexus-app.instance";
    private static final String INSTANCE_PING_OK = "OK";
    private static final String INSTANCE_PING_HUNG = "HUNG";
    private static final String INSTANCE_PING_STARTING = "STARTING";
    private static final long EDT_PING_TIMEOUT_MS = 1000L;
    private static final String instanceId = UUID.randomUUID().toString();
    private static volatile boolean instanceOwner = false;
    private static ServerSocket focusServer;
    private static int focusPort = -1;
    private static String skippedUpdate = "0";

    /**
     * The main method, the entry point of the Nexus application.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        // Display the splash screen.
        ZyneonSplash splash = new ZyneonSplash();
        splash.setVisible(true);

        // Enforce single-instance execution with hung detection and focus.
        if (!ensureSingleInstance()) {
            splash.dispose();
            return;
        }

        // Resolve command-line arguments.
        Main.args = args;
        resolveArguments(args);

        // Initialize the Nexus desktop environment.
        NexusDesktop.init();
        logger.setName("NEXUS",true);
        try {
            UIManager.put("TitlePane.menuBarEmbedded", true);
        } catch (Exception e) {
            logger.err(e.getMessage());
        }

        if(!logger.isDebugging()) {
            if (!checkVersion()) {
                System.exit(-1);
            }
        }

        // Create the main application instance.
        NexusApplication application = new NexusApplication(path, ui);

        // Start the web server if the application is not using the online UI.
        if (!application.isOnlineUI()) {
            try {
                startWebServer(args);
            } catch (Exception e) {
                System.exit(-1);
            }
        }

        // Launch the application and dispose of the splash screen if successful.
        if (application.launch()) {
            splash.dispose();
            System.gc();
        } else {
            // Stop the application if launching fails.
            NexusApplication.stop(1);
        }
    }

    /**
     * Starts the embedded web server for the application.
     *
     * @param args Command-line arguments passed to the application.
     */
    private static void startWebServer(String[] args) {
        if(port > 65535) {
            throw new RuntimeException("Port range exceeded, cannot launch application web server. Try to restart your computer or stopping port using applications and try again.");
        }
        try {
            // Configure and start the Spring Boot web server.
            new SpringApplicationBuilder(Main.class)
                    .properties("logging.level.root=WARN", "logging.pattern.console=", "server.port=" + port)
                    .run(args);
        } catch (Exception e) {
            // Increment the port and retry if the initial port is in use.
            port++;
            startWebServer(args);
        }
    }

    /**
     * Resolves and processes command-line arguments.
     *
     * @param args Command-line arguments passed to the application.
     */
    private static void resolveArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            try {
                String arg = args[i].toLowerCase();
                switch (arg) {
                    case "-h", "--help" -> {
                        // Display help message and exit.
                        logger.log("NEXUS App help:");
                        logger.log("  -d, --debug: Enables debug console output.");
                        logger.log("  -h, --help: This help message.");
                        logger.log("  -o, --online: Enables the connection to the online UI. Caution: This may cause problems with some modules.");
                        logger.log("  -p <path>, --path <path>: Lets you select the run folder.");
                        logger.log("  -u <path>, --ui <path>: Lets you select the folder where the user interface should be unpacked.");
                        System.exit(0);
                    }
                    case "-u", "--ui" -> ui = args[i + 1];
                    case "-p", "--path" -> path = args[i + 1];
                    case "-o", "--online" -> ui = "online";
                    case "-d", "--debug" -> logger.enableDebug();
                }
            } catch (Exception e) {
                // Handle argument parsing errors.
                logger.err(e.getMessage());
                logger.err("Use -h or --help at startup to view the startup arguments and their syntax.");
                System.exit(1);
            }
        }
    }

    /**
     * Gets the port used by the web server.
     *
     * @return The web server port.
     */
    public static int getPort() {
        return port;
    }

    public static String getDefaultPath() {
        String appData;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            appData = System.getenv("LOCALAPPDATA");
        } else if (os.contains("mac")) {
            appData = System.getProperty("user.home") + "/Library/Application Support";
        } else {
            appData = System.getProperty("user.home") + "/.local/share";
        }
        Path folderPath = Paths.get(appData, "Zyneon/NEXUS App");
        try {
            Files.createDirectories(folderPath);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return (folderPath + "/").replace("\\", "/");
    }

    public static String[] getArgs() {
        return args;
    }

    public static ApplicationLogger getLogger() {
        return logger;
    }

    private static boolean ensureSingleInstance() {
        Path lockPath = Paths.get(path, INSTANCE_LOCK_FILE);
        try {
            Files.createDirectories(Paths.get(path));
        } catch (Exception e) {
            logger.err("Couldn't create app directory: " + e.getMessage());
        }

        InstanceInfo existing = readInstanceInfo(lockPath);
        if (existing != null) {
            if (isProcessAlive(existing.pid)) {
                InstancePing ping = pingInstance(existing.port);
                if (ping == InstancePing.OK || ping == InstancePing.STARTING) {
                    sendFocus(existing.port);
                    return false;
                }
                // Hung or unresponsive -> allow new instance
                logger.log("Detected unresponsive instance, starting a new one.");
            } else {
                logger.log("Detected stale instance lock, starting a new one.");
            }
            deleteInstanceLock(lockPath, existing.id);
        }

        if (!startFocusServer()) {
            logger.err("Couldn't start focus server.");
            return true;
        }

        if (!writeInstanceInfo(lockPath)) {
            closeFocusServer();
            InstanceInfo latest = readInstanceInfo(lockPath);
            if (latest != null) {
                if (isProcessAlive(latest.pid)) {
                    InstancePing ping = pingInstance(latest.port);
                    if (ping == InstancePing.OK || ping == InstancePing.STARTING) {
                        sendFocus(latest.port);
                        return false;
                    }
                }
            }
        }

        instanceOwner = true;
        Runtime.getRuntime().addShutdownHook(new Thread(Main::releaseInstanceResources));
        return true;
    }

    private static boolean startFocusServer() {
        try {
            focusServer = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
            focusPort = focusServer.getLocalPort();
            Thread serverThread = new Thread(Main::runFocusServer, "nexus-focus-server");
            serverThread.setDaemon(true);
            serverThread.start();
            return true;
        } catch (Exception e) {
            logger.err("Failed to start focus server: " + e.getMessage());
            return false;
        }
    }

    private static void runFocusServer() {
        while (focusServer != null && !focusServer.isClosed()) {
            try (Socket socket = focusServer.accept()) {
                socket.setSoTimeout(1000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                String command = reader.readLine();
                if ("FOCUS".equalsIgnoreCase(command)) {
                    focusApplication();
                    writer.write(INSTANCE_PING_OK);
                } else if ("PING".equalsIgnoreCase(command)) {
                    writer.write(getInstanceStatus());
                } else {
                    writer.write(INSTANCE_PING_OK);
                }
                writer.newLine();
                writer.flush();
            } catch (IOException ignored) {
            }
        }
    }

    private static String getInstanceStatus() {
        try {
            if (NexusApplication.getInstance() == null
                    || !NexusApplication.getInstance().isLaunched()) {
                return INSTANCE_PING_STARTING;
            }
        } catch (Exception ignored) {
        }
        return isEdtResponsive(EDT_PING_TIMEOUT_MS) ? INSTANCE_PING_OK : INSTANCE_PING_HUNG;
    }

    private static boolean isEdtResponsive(long timeoutMs) {
        AtomicBoolean ran = new AtomicBoolean(false);
        try {
            SwingUtilities.invokeLater(() -> ran.set(true));
        } catch (Exception e) {
            return false;
        }
        long end = System.currentTimeMillis() + timeoutMs;
        while (!ran.get() && System.currentTimeMillis() < end) {
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return ran.get();
    }

    private static void focusApplication() {
        SwingUtilities.invokeLater(() -> {
            try {
                var app = NexusApplication.getInstance();
                if (app == null || app.getApplicationFrame() == null) {
                    return;
                }
                JFrame frame = app.getApplicationFrame().getAsJFrame();
                if (frame == null) {
                    return;
                }
                frame.setVisible(true);
                frame.setState(Frame.NORMAL);
                frame.toFront();
                frame.requestFocus();
                boolean rpc = true;
                if(NexusApplication.getInstance().getSettings().has("settings.discord.rpc")) {
                    try {
                        rpc = NexusApplication.getInstance().getSettings().getBool("settings.discord.rpc");
                    } catch (Exception ignore) {}
                }
                if(rpc) {
                    DiscordRichPresence.startRPC();
                }
            } catch (Exception ignored) {
            }
        });
    }

    private static boolean writeInstanceInfo(Path lockPath) {
        Properties props = new Properties();
        props.setProperty("pid", String.valueOf(ProcessHandle.current().pid()));
        props.setProperty("port", String.valueOf(focusPort));
        props.setProperty("id", instanceId);
        props.setProperty("ts", String.valueOf(System.currentTimeMillis()));
        try (BufferedWriter writer = Files.newBufferedWriter(
                lockPath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
        )) {
            props.store(writer, "NEXUS App instance lock");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static InstanceInfo readInstanceInfo(Path lockPath) {
        if (!Files.exists(lockPath)) {
            return null;
        }
        Properties props = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(lockPath, StandardCharsets.UTF_8)) {
            props.load(reader);
            long pid = parseLong(props.getProperty("pid"), -1L);
            int infoPort = (int) parseLong(props.getProperty("port"), -1L);
            String id = props.getProperty("id");
            long ts = parseLong(props.getProperty("ts"), 0L);
            if (pid <= 0 || infoPort <= 0) {
                return null;
            }
            return new InstanceInfo(pid, infoPort, id, ts);
        } catch (Exception e) {
            return null;
        }
    }

    private static void deleteInstanceLock(Path lockPath, String id) {
        if (!Files.exists(lockPath)) {
            return;
        }
        if (id != null) {
            InstanceInfo info = readInstanceInfo(lockPath);
            if (info != null && !id.equals(info.id)) {
                return;
            }
        }
        try {
            Files.deleteIfExists(lockPath);
        } catch (Exception ignored) {
        }
    }

    private static boolean isProcessAlive(long pid) {
        try {
            return ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    private static InstancePing pingInstance(int infoPort) {
        if (infoPort <= 0) {
            return InstancePing.NO_RESPONSE;
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", infoPort), 400);
            socket.setSoTimeout(400);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer.write("PING");
            writer.newLine();
            writer.flush();
            String response = reader.readLine();
            if (INSTANCE_PING_OK.equalsIgnoreCase(response)) {
                return InstancePing.OK;
            }
            if (INSTANCE_PING_HUNG.equalsIgnoreCase(response)) {
                return InstancePing.HUNG;
            }
            if (INSTANCE_PING_STARTING.equalsIgnoreCase(response)) {
                return InstancePing.STARTING;
            }
            return InstancePing.NO_RESPONSE;
        } catch (Exception e) {
            return InstancePing.NO_RESPONSE;
        }
    }

    private static void sendFocus(int infoPort) {
        if (infoPort <= 0) {
            return;
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", infoPort), 400);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            writer.write("FOCUS");
            writer.newLine();
            writer.flush();
        } catch (Exception ignored) {
        }
    }

    private static long parseLong(String value, long fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static void closeFocusServer() {
        try {
            if (focusServer != null) {
                focusServer.close();
            }
        } catch (Exception ignored) {
        }
    }

    private static void releaseInstanceResources() {
        if (instanceOwner) {
            deleteInstanceLock(Paths.get(path, INSTANCE_LOCK_FILE), instanceId);
        }
        closeFocusServer();
    }

    private enum InstancePing {
        OK,
        HUNG,
        STARTING,
        NO_RESPONSE
    }

    private record InstanceInfo(long pid, int port, String id, long ts) {
    }

    public static boolean checkVersion() {
        return checkVersionWin();
    }

    private static boolean checkVersionWin() {
        if(OperatingSystem.getType() == OperatingSystem.Type.Windows) {
            try {
                JsonObject jsonMeta = GsonUtility.getObject("https://zyneonstudios.github.io/apex-metadata/nexus-app/win-files/win-metadata.json");
                String latestVersion = jsonMeta.get("version").getAsString();
                if(latestVersion.equals(skippedUpdate())) {
                    return true;
                }

                String data = new String(Thread.currentThread().getContextClassLoader().getResourceAsStream("nexus.json").readAllBytes());
                JsonObject nexus = new Gson().fromJson(data, JsonObject.class);
                String currentVersion = nexus.get("version").getAsString();

                JFrame parent = null;
                if(NexusApplication.getInstance() != null) {
                    if(NexusApplication.getInstance().getApplicationFrame() != null) {
                        parent = NexusApplication.getInstance().getApplicationFrame();
                    }
                }

                if(!latestVersion.equals(currentVersion)) {
                    int update = JOptionPane.showConfirmDialog(
                            parent,
                            "Do you want to update to the latest version?\n\nCurrent version: " + currentVersion+"\nLatest version: "+latestVersion,
                            "NEXUS App update available!",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (update == JOptionPane.YES_OPTION) {
                        File tempDir = new File(getDefaultPath()+"temp/");
                        logger.deb("Created temp folder: "+tempDir.mkdirs());
                        tempDir.deleteOnExit();

                        JDialog frame = new JDialog(parent);
                        frame.setTitle("NEXUS App updater");
                        frame.setLayout(new BorderLayout());
                        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                        frame.setSize(400, 100);
                        frame.setLocationRelativeTo(null);
                        frame.setResizable(false);

                        JProgressBar indeterminator = new JProgressBar();
                        indeterminator.setIndeterminate(true);
                        indeterminator.setStringPainted(true);
                        indeterminator.setString("Downloading NEXUS App v"+latestVersion+"...");
                        indeterminator.setBorder(new EmptyBorder(10, 10, 10, 10));
                        frame.add(indeterminator, BorderLayout.CENTER);

                        frame.setVisible(true);

                        File updater = FileGetter.downloadFile(jsonMeta.get("downloadUrl").getAsString(), getDefaultPath()+"temp/"+ StringGenerator.generateAlphanumericString(12) +"-NEXUS-App-"+latestVersion+"-setup.exe");
                        if (updater != null && updater.exists()) {
                            try {
                                new ProcessBuilder(
                                        updater.getAbsolutePath(),
                                        "/SILENT",
                                        "/SUPPRESSMSGBOXES",
                                        "/MERGETASKS=runapp"
                                ).directory(updater.getParentFile()).start();
                                System.exit(0);
                            } catch (IOException e) {
                                logger.err(e.getMessage());
                                return true;
                            }
                            return false;
                        }
                    } else {
                        skippedUpdate = latestVersion;
                    }
                }
            } catch (Exception e) {
                logger.err(e.getMessage());
            }
        }
        return true;
    }

    public static String skippedUpdate() {
        return skippedUpdate;
    }
}
