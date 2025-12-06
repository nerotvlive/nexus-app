package com.zyneonstudios.nexus.application.listeners;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.starxg.keytar.Keytar;
import com.zyneonstudios.nexus.application.Main;
import com.zyneonstudios.nexus.application.events.PageLoadedEvent;
import com.zyneonstudios.nexus.application.frame.AppFrame;
import com.zyneonstudios.nexus.application.launchprocess.GameHooks;
import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.application.search.CombinedSearch;
import com.zyneonstudios.nexus.application.search.modrinth.ModrinthIntegration;
import com.zyneonstudios.nexus.application.search.modrinth.resource.ModrinthProject;
import com.zyneonstudios.nexus.application.search.modrinth.search.facets.categories.ModrinthCategory;
import com.zyneonstudios.nexus.application.search.zyndex.ZyndexIntegration;
import com.zyneonstudios.nexus.application.search.zyndex.local.LocalInstance;
import com.zyneonstudios.nexus.application.utilities.DiscordRichPresence;
import com.zyneonstudios.nexus.application.utilities.MicrosoftAuthenticator;
import com.zyneonstudios.nexus.application.utilities.StringUtility;
import com.zyneonstudios.nexus.desktop.events.AsyncWebFrameConnectorEvent;
import com.zyneonstudios.nexus.desktop.frame.web.WebFrame;
import com.zyneonstudios.nexus.index.Zyndex;
import com.zyneonstudios.nexus.instance.ReadableZynstance;
import com.zyneonstudios.nexus.instance.Zynstance;
import com.zyneonstudios.nexus.instance.ZynstanceBuilder;
import com.zyneonstudios.nexus.utilities.logger.NexusLogger;
import com.zyneonstudios.verget.Verget;
import com.zyneonstudios.verget.fabric.FabricVerget;
import com.zyneonstudios.verget.minecraft.MinecraftVerget;
import jnafilechooser.api.JnaFileChooser;
import live.nerotv.aminecraftlauncher.launcher.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;

public class AsyncConnectorListener extends AsyncWebFrameConnectorEvent {

    private static final Logger log = LoggerFactory.getLogger(AsyncConnectorListener.class);
    private final AppFrame frame;

    public AsyncConnectorListener(WebFrame frame, String message) {
        super(frame, message);
        this.frame = (AppFrame)frame;
    }

    @Override
    protected void resolveMessage(String s) {
        if (s.startsWith("event.theme.changed.")) {
            if (s.endsWith("dark")) {
                frame.setTitleBackground(Color.black);
                frame.setTitleForeground(Color.white);
                frame.getSmartBar().getBar().setBackground(Color.decode("#1f1f1f"));
                frame.getSmartBar().setBorderColor(Color.decode("#292929"));
                frame.getSmartBar().setColor(Color.lightGray);
                frame.getSmartBar().setFeedbackColor(Color.decode("#96e8ff"));
                frame.getSmartBar().setSuccessColor(Color.decode("#34bf49"));
                frame.getSmartBar().setErrorColor(Color.decode("#e63c30"));
                frame.getSmartBar().setPlaceholderColor(Color.darkGray);
                try {
                    frame.setIconImage(ImageIO.read(Objects.requireNonNull(getClass().getResource("/icon.png"))).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
                } catch (Exception ignore) {
                    throw new RuntimeException(ignore);
                }
            } else {
                frame.setTitleBackground(Color.white);
                frame.setTitleForeground(Color.black);
                frame.getSmartBar().setBackgroundColor(Color.decode("#f0f0f0"));
                frame.getSmartBar().setBorderColor(Color.lightGray);
                frame.getSmartBar().setColor(Color.decode("#292929"));
                frame.getSmartBar().setFeedbackColor(Color.decode("#0a54ff"));
                frame.getSmartBar().setSuccessColor(Color.decode("#009e18"));
                frame.getSmartBar().setErrorColor(Color.decode("#e63c30"));
                frame.getSmartBar().setPlaceholderColor(Color.lightGray);
                try {
                    frame.setIconImage(ImageIO.read(Objects.requireNonNull(getClass().getResource("/icon-inverted.png"))).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
                } catch (Exception ignore) {
                    throw new RuntimeException(ignore);
                }
            }

            // Handle page loaded events.
        } else if (s.startsWith("event.page.loaded")) {
            for (PageLoadedEvent event : NexusApplication.getInstance().getEventHandler().getPageLoadedEvents()) {
                event.setUrl(frame.getBrowser().getURL());
                event.execute();
            }

        } else if(s.startsWith("discover.search.")) {
            s = s.replace("discover.search.", "");

            if(s.equals("init")) {
                frame.executeJavaScript("document.getElementById('search-source').querySelector('.nex').querySelector('input').checked = "+NexusApplication.getInstance().getLocalSettings().isDiscoverSearchNEX()+";");
                frame.executeJavaScript("document.getElementById('search-source').querySelector('.modrinth').querySelector('input').checked = "+NexusApplication.getInstance().getLocalSettings().isDiscoverSearchModrinth()+";");
                frame.executeJavaScript("document.getElementById('search-source').querySelector('.curseforge').querySelector('input').checked = "+NexusApplication.getInstance().getLocalSettings().isDiscoverSearchCurseForge()+";");
            } else if(s.startsWith("enable.")) {
                String[] cmd = s.replace("enable.", "").split("\\.", 2);
                String p = cmd[0].toLowerCase();
                boolean e = cmd[1].equals("true");
                switch (p) {
                    case "nex" -> NexusApplication.getInstance().getLocalSettings().setDiscoverSearchNEX(e);
                    case "modrinth" -> NexusApplication.getInstance().getLocalSettings().setDiscoverSearchModrinth(e);
                    case "curseforge" ->
                            NexusApplication.getInstance().getLocalSettings().setDiscoverSearchCurseForge(e);
                }
                frame.executeJavaScript("startSearch(0);");
            }
        } else if(s.startsWith("search.")) {
            String[] query = (s.replace("search.","")).split("\\.",3);
            String serachId = query[0];
            int offset = Integer.parseInt(query[1]);
            String search = query[2];

            CombinedSearch CS = new CombinedSearch(new int[0],new ModrinthCategory[0]);
            CS.setLimit(15);
            CS.setOffset(CS.getOffset()+offset);

            for(JsonElement e:CS.search(search)) {
                JsonObject result = e.getAsJsonObject();

                String id = result.get("id").getAsString();
                String iconUrl = result.get("iconUrl").getAsString();
                String name = result.get("name").getAsString();
                String downloads = result.get("downloads").getAsString();
                String followers = result.get("followers").getAsString();
                String authors = result.getAsJsonArray("authors").get(0).getAsString();
                String summary = result.get("summary").getAsString();
                String url = result.get("url").getAsString();
                String source = result.get("source").getAsString();
                String connector = result.get("connector").getAsString();

                String cmd = "addSearchResult(\""+serachId+"\",\""+id.replace("\"","''")+"\",\""+iconUrl.replace("\"","''")+"\",\""+name.replace("\"","''")+"\",\""+downloads+"\",\""+followers+"\",\""+ authors.replace("\"","''") +"\",\""+summary.replace("\"","''")+"\",\""+url.replace("\"","''")+"\",\""+source.replace("\"","''")+"\",\""+connector.replace("\"","''")+"\");";
                frame.executeJavaScript(cmd);
            }
        } else if (s.equals("exit")) {
            NexusApplication.stop(0);
        } else if (s.equals("restart")) {
            NexusApplication.restart();
        } else if(s.equals("logout")) {
            MicrosoftAuthenticator.logout();
        } else if(s.equals("login")) {
            MicrosoftAuthenticator.startLogin(true);
        } else if(s.startsWith("logout.")) {
            String uuid = s.replace("logout.","");
            MicrosoftAuthenticator.logout(uuid);
        } else if(s.startsWith("login.")) {
            if (s.replace("login.", "").equals("new")) {
                resolveMessage("login");
                return;
            }
            try {
                MicrosoftAuthenticator.refresh(new String(Base64.getDecoder().decode(Keytar.getInstance().getPassword("ZNA||01||00", Base64.getEncoder().encodeToString(s.replace("login.", "").getBytes()) + "_0"))), true);
            } catch (Exception e) {
                NexusApplication.getLogger().printErr("NEXUS", "AUTHENTICATION", "Couldn't refresh the Microsoft token.", e.getMessage(), e.getStackTrace());
            }
        } else if(s.startsWith("settings.")) {
            s = s.replace("settings.", "");
            if(s.equals("init")) {
                long maxMemoryInMegabytes = ((com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize() / (1024 * 1024);
                frame.executeJavaScript("document.querySelector('.instance-default-path-value').innerText = '" + NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPath() + "';");
                frame.executeJavaScript("document.querySelector('.instance-JavaMemoryDisplay').min = 1024; document.querySelector('.instance-JavaMemoryDisplay').max = "+maxMemoryInMegabytes+"; document.querySelector('.instance-JavaMemoryDisplay').value = "+NexusApplication.getInstance().getLocalSettings().getDefaultMemory()+";");
                frame.executeJavaScript("document.querySelector('.instance-JavaMemory').min = 1024; document.querySelector('.instance-JavaMemory').max = "+maxMemoryInMegabytes+"; document.querySelector('.instance-JavaMemory').value = "+NexusApplication.getInstance().getLocalSettings().getDefaultMemory()+";");
            } else if(s.startsWith("select.")) {
                s = s.replace("select.", "");
                if(s.equals("instancePath")) {
                    JnaFileChooser fc = new JnaFileChooser();
                    fc.setMode(JnaFileChooser.Mode.Directories);
                    if (fc.showOpenDialog(frame)) {
                        File path = fc.getSelectedFile();
                        if(path.isDirectory()) {
                            String pathString = path.getAbsolutePath().replace("\\","/");
                            if(!pathString.endsWith("/")) {
                                pathString += "/";
                            }
                            NexusApplication.getInstance().getLocalSettings().setDefaultMinecraftPath(pathString);
                            frame.executeJavaScript("document.querySelector('.instance-default-path-value').innerText = '" + pathString + "';");
                        } else {
                            NexusApplication.getLogger().err("[NEXUS] The selected path is not a directory: " + path.getAbsolutePath());
                        }
                    }
                }
            } else if(s.startsWith("set.")) {
                s = s.replace("set.","");
                if(s.startsWith("defaultMemory.")) {
                    int memory = Integer.parseInt(s.replace("defaultMemory.", ""));
                    NexusApplication.getInstance().getLocalSettings().setDefaultMemory(memory);
                }
            }
        } else if(s.equals("initAccountSettings")) {
            if(MicrosoftAuthenticator.isLoggedIn()) {
                frame.executeJavaScript("document.querySelector('.account-activeSkin').src = 'https://cravatar.eu/helmhead/"+MicrosoftAuthenticator.getUUID()+"/128.png'; document.querySelector('.account-activeName').innerText = '"+MicrosoftAuthenticator.getUsername()+"'; document.querySelector('.account-activeUUID').innerText = '"+MicrosoftAuthenticator.getUUID()+"'; document.querySelector('.account-activeProfileCard').style.display = 'flex';");
                frame.executeJavaScript("document.querySelector('.account-activeRow').style.display = ''; document.querySelector('.account-activeRow').id = '"+MicrosoftAuthenticator.getUUID()+"'; document.querySelector('.account-activeRowName').innerText = '"+MicrosoftAuthenticator.getUsername()+"';");
                for(String uuid : MicrosoftAuthenticator.getDecryptedAuthenticatedUUIDs()) {
                    frame.executeJavaScript("addAccountToAccountList('"+MicrosoftAuthenticator.getDecryptedAuthenticatedUsername(uuid)+"','"+uuid+"');");
                }
            }
        } else if(s.equals("initAppearanceValues")) {
            frame.executeJavaScript("document.querySelector('.appearance-nativeWindow').checked = "+NexusApplication.getInstance().getLocalSettings().useNativeWindow()+";","document.querySelector('.appearance-hideApp').checked = "+NexusApplication.getInstance().getLocalSettings().minimizeApp()+";");
        } else if(s.equals("initDiscordRPC")) {
            boolean rpc = true;
            if(NexusApplication.getInstance().getSettings().has("settings.discord.rpc")) {
                try {
                    rpc = NexusApplication.getInstance().getSettings().getBool("settings.discord.rpc");
                } catch (Exception ignore) {}
            }
            frame.executeJavaScript("document.querySelector('.privacy-enableDiscordRPC').checked = "+rpc+";");
        } else if(s.startsWith("discordrpc.")) {
            if (s.replace("discordrpc.", "").equals("true")) {
                DiscordRichPresence.startRPC();
                NexusApplication.getInstance().getSettings().set("settings.discord.rpc", true);
            } else {
                DiscordRichPresence.stopRPC();
                NexusApplication.getInstance().getSettings().set("settings.discord.rpc", false);
            }
        } else if(s.startsWith("hideApp.")) {
            boolean bool = s.replace("hideApp.", "").equals("true");
            NexusApplication.getInstance().getSettings().set("settings.window.minimizeOnStart", bool);
            NexusApplication.getInstance().getLocalSettings().setMinimizeApp(bool);
        } else if(s.startsWith("install.minecraft.")) {
            String[] cmd = s.replace("install.minecraft.", "").split("\\.",2);
            String source = cmd[0].toLowerCase();
            String id = cmd[1];
            frame.executeJavaScript("document.getElementById(\""+id+"\").querySelector(\".result-install\").innerText = \"Installing...\";");
            switch (source) {
                case "nex" ->
                        ZyndexIntegration.install(NexusApplication.getInstance().getNEX().getInstancesById().get(id), NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPath());
                case "modrinth" -> {
                    ModrinthProject project = new ModrinthProject(id);
                    String version = project.getVersions()[project.getVersions().length - 1];
                    ModrinthIntegration.installModpack(new File(NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPath()), project.getId(), version);
                }
                case "curseforge" -> {
                }
            }

        } else if(s.startsWith("nativeWindow.")) {
            boolean bool = s.replace("nativeWindow.", "").equals("true");
            NexusApplication.getInstance().getSettings().set("settings.window.nativeDecorations", bool);
            NexusApplication.getInstance().getLocalSettings().setUseNativeWindow(bool);
            NexusApplication.restart();
        } else if(s.startsWith("library.")) {
            s = s.replace("library.", "");
            if(s.equals("init")) {
                NexusApplication.getInstance().getInstanceManager().reload();
                String showId = null;
                for(LocalInstance lI:NexusApplication.getInstance().getInstanceManager().getInstances().values()) {
                    Zynstance i = lI.getInstance();
                    String iconUrl = "";
                    if(i.getIconUrl()!=null) {
                        iconUrl = i.getIconUrl();
                    }
                    frame.executeJavaScript("addInstance(\""+StringUtility.encodeData(lI.getPath())+"\",\""+StringUtility.encodeData(i.getName())+"\",\""+StringUtility.encodeData(iconUrl)+"\",\"\");");
                    if(showId == null) {
                        showId = lI.getPath();
                    }
                }

                if(showId != null) {
                    if (!NexusApplication.getInstance().getLocalSettings().getLastInstanceId().isEmpty()) {
                        showId = NexusApplication.getInstance().getLocalSettings().getLastInstanceId();
                    }
                    resolveMessage("library.showInstance." + showId);
                }
                resolveMessage("library.creator.init.vanilla");
            } else if(s.startsWith("creator.")) {
                frame.executeJavaScript("if(!document.getElementById('creator-ml-versions').classList.contains('d-none')) { document.getElementById('creator-ml-versions').classList.add('d-none'); }","if(!document.getElementById('creator-name-warning').classList.contains('d-none')) {document.getElementById('creator-name-warning').classList.add('d-none');}");
                s = s.replaceFirst("creator.", "");
                if(s.startsWith("init.version.")) {
                    s = s.replace("init.version.","");
                    String[] query = s.split("\\.",2);
                    String type = query[0];
                    String ver = query[1];
                    ArrayList<String> versions = new ArrayList<>();
                    switch (type) {
                        case "fabric" -> versions.addAll(Verget.getFabricVersions(true, ver));
                        case "forge" -> versions.addAll(Verget.getForgeVersions(ver));
                        case "neoforge" -> versions.addAll(Verget.getNeoForgeVersions(ver));
                        case "quilt" -> versions.addAll(Verget.getQuiltVersions(ver));
                    }
                    String option = "<option value='%ver%'>%ver%</option>";
                    for(String v:versions) {
                        frame.executeJavaScript("document.getElementById('creator-ml-versions').innerHTML += \""+option.replace("%ver%",v)+"\";","if(document.getElementById('creator-ml-versions').classList.contains('d-none')) { document.getElementById('creator-ml-versions').classList.remove('d-none'); }");
                    }
                } else if(s.startsWith("init.")) {
                    s = s.replace("init.","");
                    ArrayList<String> versions = new ArrayList<>();
                    switch (s) {
                        case "experimental" -> versions.addAll(Verget.getMinecraftVersions(MinecraftVerget.Filter.EXPERIMENTAL));
                        case "fabric" -> versions.addAll(Verget.getFabricGameVersions(true));
                        case "forge" -> versions.addAll(Verget.getForgeGameVersions());
                        case "neoforge" -> versions.addAll(Verget.getNeoForgeGameVersions());
                        case "quilt" -> versions.addAll(Verget.getQuiltGameVersions(true));
                        default -> versions.addAll(Verget.getMinecraftVersions(MinecraftVerget.Filter.RELEASES));
                    }
                    String option = "<option value='%ver%'>%ver%</option>";
                    for(String v:versions) {
                        frame.executeJavaScript("document.getElementById('creator-mc-versions').innerHTML += \""+option.replace("%ver%",v)+"\";");
                    }
                    resolveMessage("library.creator.init.version."+s+"."+versions.getFirst());
                } else if(s.startsWith("create.")) {
                    String[] args = s.replace("create.","").split("\\.",4);
                    String crversion = URLDecoder.decode(args[0], StandardCharsets.UTF_8).replace("#DOT%",".");
                    String crtype = URLDecoder.decode(args[1], StandardCharsets.UTF_8).replace("#DOT%",".");
                    String crmlversion = URLDecoder.decode(args[2], StandardCharsets.UTF_8).replace("#DOT%",".");
                    String crname = URLDecoder.decode(args[3], StandardCharsets.UTF_8).replace("#DOT%",".");
                    String crid = crname.toLowerCase().replaceAll("[^0-9a-z\\-_]","");

                    File installDir = ZyndexIntegration.getInstallDir(new File(NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPath()),crid);
                    ZynstanceBuilder builder = new ZynstanceBuilder(installDir+"/zyneonInstance.json");
                    builder.setMinecraftVersion(crversion);
                    builder.setSummary("This is an instance created by you!");
                    builder.setId(crid);
                    builder.setName(crname);
                    String authorName = "Anonymous";
                    if(MicrosoftAuthenticator.isLoggedIn()) {
                        ArrayList<String> author = new ArrayList<>();
                        author.add(MicrosoftAuthenticator.getUsername());
                        builder.setAuthors(author);
                        authorName = MicrosoftAuthenticator.getUsername();
                    }
                    builder.setDescription("\n ## Welcome to your new instance!\n\nYou created this instance as **%author%** under the name **%name%**! Now you are free to **customize** your instance! **Manage mods, resource packs, shaders, worlds, and much more** - write your description and, if you like, **share it with your friends**! \n\n\n ## Manage resources\n\nUse the **tabs above** to **browse** installed resources and find and install new resources compatible with your instance, such as **mods, maps, resource packs, or shaders**. The resource manager also helps you to **update, deactivate, or delete** already installed resources. \n \n ## Settings \n \n In the top right, you will find the button with the gear icon. There you can change your instance settings. Things you can adjust there include: the Java version, the Java runtime and environment variables, the amount of RAM that should be available for your instance, and your personalization options such as the instance name, the description, summary, version, modloader, Minecraft version, images, and much more. You can also delete your instance in the settings. \n\n ## That's it! \n\n Now, we'll let you discover the rest yourself. Feel free to click around the program and get into the groove of creating and managing your own instances with the NEXUS App—or discover instances from other people via Discover!".replace("%author%",authorName).replace("%name%",crname));
                    switch (crtype) {
                        case "fabric" -> builder.setFabricVersion(crmlversion);
                        case "forge" -> builder.setForgeVersion(crmlversion);
                        case "neoforge" -> builder.setNeoForgeVersion(crmlversion);
                        case "quilt" -> builder.setQuiltVersion(crmlversion);
                    }
                    builder.create();
                    frame.getBrowser().reload();
                }
            } else if(s.startsWith("showInstance.")) {
                String showId = s.replace("showInstance.", "");
                LocalInstance lI = NexusApplication.getInstance().getInstanceManager().getInstance(showId);
                ReadableZynstance show = lI.getInstance();
                String tags = show.getTagString();
                if(show.getFabricVersion()!=null&&!tags.contains("fabric-"+show.getFabricVersion())) {
                    tags = "fabric-"+show.getFabricVersion() + ", " + tags;
                } else if(show.getForgeVersion()!=null&&!tags.contains("forge-"+show.getForgeVersion())) {
                    tags = "forge-"+show.getForgeVersion() + ", " + tags;
                } else if(show.getNeoForgeVersion()!=null&&!tags.contains("neoforge-"+show.getNeoForgeVersion())) {
                    tags = "neoforge-"+show.getNeoForgeVersion() + ", " + tags;
                } else if(show.getQuiltVersion()!=null&&!tags.contains("quilt-"+show.getQuiltVersion())) {
                    tags = "quilt-"+show.getQuiltVersion() + ", " + tags;
                }
                if(!tags.contains("minecraft-"+show.getMinecraftVersion())) {
                    tags = "minecraft-"+show.getMinecraftVersion() + ", " + tags;
                }
                String cmd = "showInstance(\""+ StringUtility.encodeData(lI.getPath())+"\",\""+StringUtility.encodeData(show.getName())+"\",\""+StringUtility.encodeData(show.getVersion())+"\",\""+StringUtility.encodeData(show.getSummary())+"\",\""+ StringUtility.encodeData(show.getDescription()) +"\",\""+tags+"\");";
                String button = "";
                if(NexusApplication.getInstance().getInstanceManager().hasRunningInstance(showId)) {
                    button = "document.getElementById(\"launch-button\").innerHTML = \"<i class='bi bi-check-lg'></i> RUNNING\";";
                }
                frame.executeJavaScript(cmd,button);
                NexusApplication.getInstance().getLocalSettings().setLastInstanceId(showId);
            } else if(s.startsWith("start.")) {
                String id = s.replace("start.", "");
                ReadableZynstance instance = NexusApplication.getInstance().getInstanceManager().getInstance(id).getInstance();
                if(instance != null) {
                    String mc = instance.getMinecraftVersion();
                    String modloaderVersion;
                    if(instance.getModloader().equalsIgnoreCase("fabric")) {
                        modloaderVersion = instance.getFabricVersion();
                        MinecraftVersion version = new MinecraftVersion(NexusApplication.getInstance().getWorkingPath()+"/libs/");
                        MinecraftVersion.Type type = version.getType(instance.getMinecraftVersion());
                        if(type!=null) {
                            version.setJava(type);
                        }
                        FabricLauncher launcher = NexusApplication.getInstance().getFabricLauncher();
                        launcher.setPreLaunchHook(GameHooks.getPreLaunchHook(launcher));
                        launcher.setPostLaunchHook(GameHooks.getPostLaunchHook(launcher));
                        launcher.setGameCloseHook(GameHooks.getGameCloseHook(launcher));
                        launcher.launch(mc, modloaderVersion, NexusApplication.getInstance().getLocalSettings().getDefaultMemory(), Path.of(NexusApplication.getInstance().getInstanceManager().getInstance(id).getPath()), instance.getId());
                        NexusApplication.getInstance().getInstanceManager().addRunningInstance(launcher.getGameProcess(), id);
                    } else if(instance.getModloader().equalsIgnoreCase("forge")) {
                        modloaderVersion = instance.getForgeVersion();
                        MinecraftVersion version = new MinecraftVersion(NexusApplication.getInstance().getWorkingPath()+"/libs/");
                        MinecraftVersion.Type type = version.getType(instance.getMinecraftVersion());
                        if(type!=null) {
                            version.setJava(type);
                        }
                        ForgeLauncher launcher = NexusApplication.getInstance().getForgeLauncher();
                        launcher.setPreLaunchHook(GameHooks.getPreLaunchHook(launcher));
                        launcher.setPostLaunchHook(GameHooks.getPostLaunchHook(launcher));
                        launcher.setGameCloseHook(GameHooks.getGameCloseHook(launcher));
                        launcher.launch(mc,modloaderVersion,NexusApplication.getInstance().getLocalSettings().getDefaultMemory(), Path.of(NexusApplication.getInstance().getInstanceManager().getInstance(id).getPath()),instance.getId());
                        NexusApplication.getInstance().getInstanceManager().addRunningInstance(launcher.getGameProcess(), id);
                    } else if(instance.getModloader().equalsIgnoreCase("neoforge")) {
                        modloaderVersion = instance.getNeoForgeVersion();
                        MinecraftVersion version = new MinecraftVersion(NexusApplication.getInstance().getWorkingPath()+"/libs/");
                        MinecraftVersion.Type type = version.getType(instance.getMinecraftVersion());
                        if(type!=null) {
                            version.setJava(type);
                        }
                        NeoForgeLauncher launcher = NexusApplication.getInstance().getNeoForgeLauncher();
                        launcher.setPreLaunchHook(GameHooks.getPreLaunchHook(launcher));
                        launcher.setPostLaunchHook(GameHooks.getPostLaunchHook(launcher));
                        launcher.setGameCloseHook(GameHooks.getGameCloseHook(launcher));
                        launcher.launch(mc, modloaderVersion, NexusApplication.getInstance().getLocalSettings().getDefaultMemory(), Path.of(NexusApplication.getInstance().getInstanceManager().getInstance(id).getPath()), instance.getId());
                        NexusApplication.getInstance().getInstanceManager().addRunningInstance(launcher.getGameProcess(), id);
                    } else if(instance.getModloader().equalsIgnoreCase("quilt")) {
                        modloaderVersion = instance.getQuiltVersion();
                        MinecraftVersion version = new MinecraftVersion(NexusApplication.getInstance().getWorkingPath()+"/libs/");
                        MinecraftVersion.Type type = version.getType(instance.getMinecraftVersion());
                        if(type!=null) {
                            version.setJava(type);
                        }
                        QuiltLauncher launcher = NexusApplication.getInstance().getQuiltLauncher();
                        launcher.setPreLaunchHook(GameHooks.getPreLaunchHook(launcher));
                        launcher.setPostLaunchHook(GameHooks.getPostLaunchHook(launcher));
                        launcher.setGameCloseHook(GameHooks.getGameCloseHook(launcher));
                        launcher.launch(mc, modloaderVersion, NexusApplication.getInstance().getLocalSettings().getDefaultMemory(), Path.of(NexusApplication.getInstance().getInstanceManager().getInstance(id).getPath()), instance.getId());
                        NexusApplication.getInstance().getInstanceManager().addRunningInstance(launcher.getGameProcess(), id);
                    } else {
                        VanillaLauncher launcher = NexusApplication.getInstance().getVanillaLauncher();
                        MinecraftVersion version = new MinecraftVersion(NexusApplication.getInstance().getWorkingPath()+"/libs/");
                        MinecraftVersion.Type type = version.getType(instance.getMinecraftVersion());
                        if(type!=null) {
                            version.setJava(type);
                        }
                        launcher.setPreLaunchHook(GameHooks.getPreLaunchHook(launcher));
                        launcher.setPostLaunchHook(GameHooks.getPostLaunchHook(launcher));
                        launcher.setGameCloseHook(GameHooks.getGameCloseHook(launcher));
                        launcher.launch(mc, NexusApplication.getInstance().getLocalSettings().getDefaultMemory(), Path.of(NexusApplication.getInstance().getInstanceManager().getInstance(id).getPath()), instance.getId());
                        NexusApplication.getInstance().getInstanceManager().addRunningInstance(launcher.getGameProcess(), id);
                    }
                }
            } else if(s.startsWith("settings.")) {
                String id = s.replace("settings.", "");
                frame.executeJavaScript("showSettingsPane('general');");
            } else if(s.startsWith("folder.")) {
                String id = s.replace("folder.", "");
                LocalInstance instance = NexusApplication.getInstance().getInstanceManager().getInstance(id);
                if(instance != null) {
                    try {
                        Desktop.getDesktop().open(new File(instance.getPath()));
                    } catch (IOException e) {
                        NexusApplication.getLogger().err(e.getMessage());
                    }
                }
            }
        } else if(s.equals("run.test")) {

            FabricLauncher launcher = NexusApplication.getInstance().getFabricLauncher();
            launcher.setPreLaunchHook(GameHooks.getPreLaunchHook(launcher));
            launcher.setPostLaunchHook(GameHooks.getPostLaunchHook(launcher));
            launcher.setGameCloseHook(GameHooks.getGameCloseHook(launcher));
            String version = FabricVerget.getSupportedMinecraftVersions(false).getFirst();
            launcher.launch(version, FabricVerget.getVersions(true).getFirst(),NexusApplication.getInstance().getLocalSettings().getDefaultMemory(), Path.of("target/run/game/"+version+"/"),"test");

        }
    }
}

