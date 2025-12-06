package com.zyneonstudios.nexus.application.main;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.starxg.keytar.Keytar;
import com.zyneonstudios.nexus.application.Main;
import com.zyneonstudios.nexus.application.downloads.DownloadManager;
import com.zyneonstudios.nexus.application.frame.AppFrame;
import com.zyneonstudios.nexus.application.listeners.PageLoadListener;
import com.zyneonstudios.nexus.application.main.console.NexusConsoleHandler;
import com.zyneonstudios.nexus.application.main.console.commands.*;
import com.zyneonstudios.nexus.application.modules.ModuleLoader;
import com.zyneonstudios.nexus.application.search.curseforge.CurseForgeCategories;
import com.zyneonstudios.nexus.application.search.zyndex.local.LocalInstanceManager;
import com.zyneonstudios.nexus.application.utilities.ApplicationLogger;
import com.zyneonstudios.nexus.application.utilities.DiscordRichPresence;
import com.zyneonstudios.nexus.application.utilities.MicrosoftAuthenticator;
import com.zyneonstudios.nexus.desktop.frame.web.NexusWebSetup;
import com.zyneonstudios.nexus.index.ReadableZyndex;
import com.zyneonstudios.nexus.utilities.file.FileActions;
import com.zyneonstudios.nexus.utilities.file.FileExtractor;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;
import com.zyneonstudios.nexus.utilities.strings.StringGenerator;
import com.zyneonstudios.nexus.utilities.system.OperatingSystem;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import live.nerotv.aminecraftlauncher.launcher.*;
import org.cef.CefApp;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefBeforeDownloadCallback;
import org.cef.callback.CefDownloadItem;
import org.cef.handler.CefDownloadHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * The {@code NexusApplication} class is the main object and core component of the Nexus application.
 * It manages the application's lifecycle, including initialization, module loading, UI setup, and shutdown.
 * It also provides access to various application components and resources.
 */
public class NexusApplication {

    private static final Logger log = LoggerFactory.getLogger(NexusApplication.class);
    private ReadableZyndex NEX = null;
    private LocalInstanceManager instanceManager;

    //Authentication
    private static AuthInfos authInfos = null;

    //Download management
    private final DownloadManager downloadManager;

    // Static Instance and Directories
    private static NexusApplication instance = null;
    private String workingDir;
    private String uiPath = null;

    // Application Components
    private final NexusEventHandler eventHandler = new NexusEventHandler();
    private final NexusRunner runner = new NexusRunner();
    private NexusWebSetup webSetup;
    private ModuleLoader moduleLoader;
    private AppFrame applicationFrame = null;
    private final NexusConsoleHandler consoleHandler;

    // Configuration and State
    private final JsonStorage settings;
    private final JsonStorage data;
    private final ApplicationSettings localSettings = new ApplicationSettings();
    private final boolean onlineUI;
    private boolean launched = false;
    private String version = StringGenerator.generateAlphanumericString(12);

    /**
     * Constructor for the NexusApplication.
     *
     * @param path   The working directory path.
     * @param uiPath The path to the UI or "online" for online UI.
     */
    public NexusApplication(String path, String uiPath) {
        instance = this;
        consoleHandler = new NexusConsoleHandler();
        consoleHandler.startReading();
        initCommands();

        // Setup working directory
        File workingDirFile = new File(path);
        if (workingDirFile.mkdirs()) {
            getLogger().deb("Creating working directory (first run)...");
        }
        workingDir = workingDirFile.getAbsolutePath().replace("\\", "/");

        // Setup settings and data storage
        settings = new JsonStorage(workingDirFile.getAbsolutePath() + "/data/settings.json");
        data = new JsonStorage(workingDirFile.getAbsolutePath() + "/data/application.json");

        // Determine UI mode (online or local)
        this.onlineUI = uiPath != null && uiPath.equals("online");
        if (!onlineUI) {
            this.uiPath = (uiPath != null) ? uiPath : workingDirFile.getAbsolutePath() + "/temp/ui";
        }

        loadVersion();
        setupTempDirectory();
        setupWebEnvironment(workingDirFile);
        getLogger().log("Initializing application...");

        settings.ensure("settings.minecraft.defaultPath",workingDirFile.getAbsolutePath().replace("\\", "/") + "/instances/");
        localSettings.setDefaultMinecraftPath(settings.getString("settings.minecraft.defaultPath"));

        settings.ensure("settings.minecraft.defaultMemory",2048);
        localSettings.setDefaultMemory(settings.getInt("settings.minecraft.defaultMemory"));

        settings.ensure("settings.window.nativeDecorations", OperatingSystem.getType().equals(OperatingSystem.Type.Windows));
        localSettings.setUseNativeWindow(settings.getBool("settings.window.nativeDecorations"));

        settings.ensure("settings.window.minimizeOnStart",true);
        localSettings.setMinimizeApp(settings.getBool("settings.window.minimizeOnStart"));

        settings.ensure("settings.discover.search.nex.enabled", true);
        localSettings.setDiscoverSearchNEX(settings.getBool("settings.discover.search.nex.enabled"));

        settings.ensure("settings.discover.search.curseforge.enabled", true);
        localSettings.setDiscoverSearchCurseForge(settings.getBool("settings.discover.search.curseforge.enabled"));

        settings.ensure("settings.discover.search.modrinth.enabled", true);
        localSettings.setDiscoverSearchModrinth(settings.getBool("settings.discover.search.modrinth.enabled"));

        settings.ensure("settings.library.instance.last","");
        localSettings.setLastInstanceId(settings.getString("settings.library.instance.last"));

        instanceManager = new LocalInstanceManager(new JsonStorage(workingDirFile.getAbsolutePath() + "/data/instances.json"));

        boolean rpc = true;
        if(settings.has("settings.discord.rpc")) {
            rpc = settings.getBool("settings.discord.rpc");
        }
        if(rpc) {
            DiscordRichPresence.startRPC();
        }

        CurseForgeCategories.init();

        CompletableFuture.runAsync(()->{
            getLogger().log("Checking if user is logged in...");
            MicrosoftAuthenticator.init();
            try {
                if(Keytar.getInstance().getPassword("ZNA||00||00","0")!=null) {
                    MicrosoftAuthenticator.refresh(new String(Base64.getDecoder().decode(Keytar.getInstance().getPassword("ZNA||01||00",Keytar.getInstance().getPassword("ZNA||00||00","0")+"_0"))),true);
                }

            } catch (Exception e) {
                NexusApplication.getLogger().printErr("NEXUS","AUTHENTICATION","Couldn't initialize key management. Account credentials won't be saved. You'll have to login everytime you restart the application.",e.getMessage(), e.getStackTrace(), "If you're on Linux try to install libsecret.");
            }
        });

        CompletableFuture.runAsync(()->{
            getLogger().log("Initializing NEX connection...");
            NEX = new ReadableZyndex("https://zyneonstudios.github.io/nexus-nex/zyndex/index.json");
        });

        this.downloadManager = new DownloadManager(this);
    }

    private void initCommands() {
        consoleHandler.addCommand(new HelpCommand());
        consoleHandler.addCommand(new JavascriptCommand());
        consoleHandler.addCommand(new LaunchCommand());
        consoleHandler.addCommand(new GetCommand());
        consoleHandler.addCommand(new ExitCommand());
        consoleHandler.addCommand(new ConnectorCommand());
        consoleHandler.addCommand(new ModrinthCommand());
    }

    /**
     * Loads the application version from the nexus.json file.
     */
    private void loadVersion() {
        if(!getLogger().isDebugging()) {
            try {
                String data = new String(Thread.currentThread().getContextClassLoader().getResourceAsStream("nexus.json").readAllBytes());
                JsonObject nexus = new Gson().fromJson(data, JsonObject.class);
                version = nexus.get("version").getAsString();
            } catch (Exception e) {
                getLogger().err("Couldn't fetch version from nexus.json: " + e.getMessage());
            }
        }
    }

    /**
     * Sets up the temporary directory for the application.
     */
    private void setupTempDirectory() {
        File temp = new File(workingDir + "/temp");
        if (temp.exists()) {
            try {
                FileActions.deleteFolder(temp);
            } catch (Exception e) {
                getLogger().err("Couldn't delete old temp folder: " + e.getMessage());
                stop(1);
                return;
            }
        }
        if (temp.mkdirs()) {
            temp.deleteOnExit();
            try {
                FileExtractor.extractResourceFile("html.zip", workingDir + "/temp/ui.zip", Main.class);
                File uiZip = new File(workingDir + "/temp/ui.zip");
                FileExtractor.unzipFile(uiZip.getAbsolutePath(), workingDir + "/temp/ui/");
                if (!uiZip.delete()) {
                    uiZip.deleteOnExit();
                }
            } catch (Exception e) {
                getLogger().err("Error while extracting UI: " + e.getMessage());
                stop(1);
            }
        } else {
            getLogger().err("Couldn't create temp folder.");
            stop(1);
        }
    }

    /**
     * Sets up the web environment for the application.
     *
     * @param workingDirFile The working directory file.
     */
    private void setupWebEnvironment(File workingDirFile) {
        webSetup = new NexusWebSetup(workingDirFile.getAbsolutePath() + "/libs/cef/");
        webSetup.enableCache(true);
        webSetup.enableCookies(true);
        webSetup.setup();
        webSetup.getWebClient().addDownloadHandler(new CefDownloadHandlerAdapter() {
            @Override
            public boolean onBeforeDownload(CefBrowser browser, CefDownloadItem downloadItem, String suggestedName, CefBeforeDownloadCallback callback) {
                return true;
            }
        });
        webSetup.getWebClient().addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {
                if(!(browser.getURL().startsWith("http://localhost:" + Main.getPort()) || browser.getURL().isEmpty() || browser.getURL().startsWith("http://127.0.0.1:" + Main.getPort()))) {
                    browser.loadURL("http://localhost:"+Main.getPort()+"/601?url="+browser.getURL());
                }
            }
        });
        webSetup.getWebClient().addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                frame.executeJavaScript("version = \""+version+"\";", browser.getURL() ,0);
            }
        });

        // Setup page load listener
        eventHandler.addPageLoadedEvent(new PageLoadListener());
    }

    /**
     * Loads modules from the modules directory.
     */
    private void loadModules() {
        File modules = new File(workingDir + "/modules");
        if (modules.exists() && modules.isDirectory()) {
            for (File module : Objects.requireNonNull(modules.listFiles())) {
                if (!module.isDirectory()) {
                    try {
                        moduleLoader.loadModule(moduleLoader.readModule(module));
                    } catch (Exception e) {
                        getLogger().err("Couldn't load module " + module.getName() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Launches the application.
     *
     * @return True if the application was launched successfully, false otherwise.
     */
    public boolean launch() {
        if (!launched) {
            try {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                String url = onlineUI ? "https://zyneonstudios.github.io/nexus-app/src/main/html?app=true" : "http://localhost:" + Main.getPort() + "/index.html?app=true";
                applicationFrame = new AppFrame(webSetup, url, true);
                applicationFrame.setTitlebar(version, Color.black, Color.white);
                applicationFrame.setSize((int)(screenSize.getWidth()/1.5), (int)(screenSize.getHeight()/1.5));
                applicationFrame.setLocationRelativeTo(null);
                applicationFrame.setVisible(true);
                launched = true;
            } catch (Exception e) {
                getLogger().err("Couldn't launch application: " + e.getMessage());
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> CefApp.getInstance().dispose()));

        runner.start();
        moduleLoader = new ModuleLoader(this);
        loadModules();
        moduleLoader.activateModules();
        return launched;
    }

    /**
     * Stops the application.
     *
     * @param exitCode The exit code to use.
     * @param closeAsync Defines if the close action should run asynchronously.
     */
    public static void stop(int exitCode, boolean closeAsync) {
        if(closeAsync) {
            SwingUtilities.invokeLater(() -> {
                end(exitCode);
            });
        } else {
            end(exitCode);
        }
    }

    /**
     * Stops the application.
     *
     * @param exitCode The exit code to use.
     */
    public static void stop(int exitCode) {
        stop(exitCode, true);
    }


    private static void end(int exitCode) {
        getInstance().getWebSetup().getWebApp().dispose();
        try {
            if (getInstance().getWebSetup() != null && getInstance().getWebSetup().getWebApp() != null) {
                getInstance().getWebSetup().getWebApp().dispose();
            }
        } catch (Exception ignore) {
        }
        try {
            if (getInstance().getModuleLoader() != null) {
                getInstance().getModuleLoader().deactivateModules();
            }
        } catch (Exception ignore) {
        }
        System.gc();

        FileActions.deleteFolder(new File((NexusApplication.getInstance().getWorkingPath()+"/temp/").replace("\\","/").replace("//","/")));
        System.exit(exitCode);
    }


    /**
     * Restarts the application.
     */
    public static void restart() {
        try {
            getInstance().getWebSetup().getWebApp().dispose();
            String java = System.getProperty("java.home") + "/bin/java";
            File currentJar = new File(NexusApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (!currentJar.getName().endsWith(".jar")) {
                throw new RuntimeException("You need to run the .jar file to restart.");
            }
            ArrayList<String> command = new ArrayList<>();
            command.add(java);
            command.add("-jar");
            command.add(currentJar.getPath());
            Collections.addAll(command, Main.getArgs());
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
            System.exit(0);
        } catch (Exception e) {
            getLogger().err("Couldn't restart application: " + e.getMessage());
            if(getInstance()!=null) {
                if(getInstance().getApplicationFrame()!=null) {
                    getInstance().getApplicationFrame().setVisible(false);
                    getInstance().getApplicationFrame().executeJavaScript("location.href = 'index.html?app=true'");
                    getInstance().getApplicationFrame().setVisible(true);
                }
            }
        }
    }


    // --- Getter Methods ---

    /**
     * Gets the application's logger.
     *
     * @return The ApplicationLogger instance.
     */
    public static ApplicationLogger getLogger() {
        return Main.getLogger();
    }

    /**
     * Gets the working directory as a File object.
     *
     * @return The working directory.
     */
    public File getWorkingDir() {
        return new File(workingDir);
    }

    /**
     * Gets the path to the UI.
     *
     * @return The UI path.
     */
    public String getUiPath() {
        return uiPath;
    }

    /**
     * Gets the NexusApplication instance.
     *
     * @return The NexusApplication instance.
     */
    public static NexusApplication getInstance() {
        return instance;
    }

    /**
     * Gets the application's runner.
     *
     * @return The NexusRunner instance.
     */
    public NexusRunner getRunner() {
        return runner;
    }

    /**
     * Gets the working path as a String.
     *
     * @return The working path.
     */
    public String getWorkingPath() {
        return workingDir;
    }

    /**
     * Gets the application's main frame.
     *
     * @return The ApplicationFrame instance.
     */
    public AppFrame getApplicationFrame() {
        return applicationFrame;
    }

    /**
     * Gets the web setup.
     *
     * @return The NexusWebSetup instance.
     */
    public NexusWebSetup getWebSetup() {
        return webSetup;
    }

    /**
     * Checks if the application is launched.
     *
     * @return True if launched, false otherwise.
     */
    public boolean isLaunched() {
        return launched;
    }

    /**
     * Checks if the application is using the online UI.
     *
     * @return True if using online UI, false otherwise.
     */
    public boolean isOnlineUI() {
        return onlineUI;
    }

    /**
     * Gets the application's version.
     *
     * @return The version string.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the application's settings storage.
     *
     * @return The JsonStorage instance.
     */
    public JsonStorage getSettings() {
        return settings;
    }

    /**
     * Gets the module loader.
     *
     * @return The ModuleLoader instance.
     */
    public ModuleLoader getModuleLoader() {
        return moduleLoader;
    }

    /**
     * Gets the event handler.
     *
     * @return The NexusEventHandler instance.
     */
    public NexusEventHandler getEventHandler() {
        return eventHandler;
    }

    /**
     * Initializes and returns a Minecraft launcher with application auth infos
     *
     * @return A pre-initialized Vanilla launcher
     */
    public VanillaLauncher getVanillaLauncher() {
        return new VanillaLauncher(authInfos);
    }

    /**
     * Initializes and returns a Minecraft launcher with application auth infos
     *
     * @return A pre-initialized Fabric launcher
     */
    public FabricLauncher getFabricLauncher() {
        return new FabricLauncher(authInfos);
    }

    /**
     * Initializes and returns a Minecraft launcher with application auth infos
     *
     * @return A pre-initialized Forge launcher
     */
    public ForgeLauncher getForgeLauncher() {
        return new ForgeLauncher(authInfos);
    }

    /**
     * Initializes and returns a Minecraft launcher with application auth infos
     *
     * @return A pre-initialized NeoForge launcher
     */
    public NeoForgeLauncher getNeoForgeLauncher() {
        return new NeoForgeLauncher(authInfos);
    }

    /**
     * Initializes and returns a Minecraft launcher with application auth infos
     *
     * @return A pre-initialized Quilt launcher
     */
    public QuiltLauncher getQuiltLauncher() {
        return new QuiltLauncher(authInfos);
    }

    /**
     * Sets the authentication information for the used Microsoft account
     *
     * @param authInfos Map of authentication information
     */
    public static void setAuthInfos(AuthInfos authInfos) {
        NexusApplication.authInfos = authInfos;
    }

    /**
     * Gets the application's local settings.
     *
     * @return The ApplicationSettings instance.
     */
    public ApplicationSettings getLocalSettings() {
        return localSettings;
    }

    /**
     * Gets the application's data storage.
     *
     * @return The JsonStorage instance.
     */
    public JsonStorage getData() {
        return data;
    }

    /**
     * Gets NEX, the official Zyndex instance by Zyneon Nexus
     *
     * @return The ReadableZyndex object of our official modpack repository
     */
    public ReadableZyndex getNEX() {
        return NEX;
    }

    /**
     * Gets the download manager
     *
     * @return DownloadManager object
     */
    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    public LocalInstanceManager getInstanceManager() {
        return instanceManager;
    }

    public NexusConsoleHandler getConsoleHandler() {
        return consoleHandler;
    }
}