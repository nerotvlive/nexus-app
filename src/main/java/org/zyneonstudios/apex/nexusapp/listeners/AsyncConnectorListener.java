package org.zyneonstudios.apex.nexusapp.listeners;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.starxg.keytar.Keytar;
import com.zyneonstudios.nexus.desktop.events.AsyncWebFrameConnectorEvent;
import com.zyneonstudios.nexus.desktop.frame.web.WebFrame;
import com.zyneonstudios.nexus.instance.ReadableZynstance;
import com.zyneonstudios.nexus.instance.Zynstance;
import com.zyneonstudios.nexus.instance.ZynstanceBuilder;
import com.zyneonstudios.nexus.utilities.file.FileActions;
import com.zyneonstudios.verget.Verget;
import com.zyneonstudios.verget.minecraft.MinecraftVerget;
import jnafilechooser.api.JnaFileChooser;
import live.nerotv.aminecraftlauncher.launcher.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zyneonstudios.apex.nexusapp.events.PageLoadedEvent;
import org.zyneonstudios.apex.nexusapp.frame.AppFrame;
import org.zyneonstudios.apex.nexusapp.launchprocess.GameHooks;
import org.zyneonstudios.apex.nexusapp.main.NexusApplication;
import org.zyneonstudios.apex.nexusapp.search.CombinedSearch;
import org.zyneonstudios.apex.nexusapp.search.modrinth.ModrinthIntegration;
import org.zyneonstudios.apex.nexusapp.search.modrinth.ModrinthResource;
import org.zyneonstudios.apex.nexusapp.search.modrinth.search.facets.categories.ModrinthCategory;
import org.zyneonstudios.apex.nexusapp.search.zyndex.ZyndexIntegration;
import org.zyneonstudios.apex.nexusapp.search.zyndex.local.LocalInstance;
import org.zyneonstudios.apex.nexusapp.utilities.*;

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
                    case "curseforge" -> NexusApplication.getInstance().getLocalSettings().setDiscoverSearchCurseForge(e);
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
                if(!NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPath().equals(NexusApplication.getInstance().getWorkingPath()+"/instances/")) {
                    frame.executeJavaScript("document.querySelector('.instance-default-path').querySelector('.right').querySelector('.d-none').classList.remove('d-none');");
                }

                long maxMemoryInMegabytes = ((com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize() / (1024 * 1024);
                frame.executeJavaScript("document.querySelector('.instance-default-path-value').innerText = '" + NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPath() + "';");
                frame.executeJavaScript("document.querySelector('.instance-JavaMemoryDisplay').min = 1024; document.querySelector('.instance-JavaMemoryDisplay').max = " + maxMemoryInMegabytes + "; document.querySelector('.instance-JavaMemoryDisplay').value = " + NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftMemory() + ";");
                frame.executeJavaScript("document.querySelector('.instance-JavaMemory').min = 1024; document.querySelector('.instance-JavaMemory').max = " + maxMemoryInMegabytes + "; document.querySelector('.instance-JavaMemory').value = " + NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftMemory() + ";");
            } else if(s.startsWith("init.")) {
                s = s.replace("init.", "");
                if(s.equals("defaultInstanceSettings")) {
                    frame.executeJavaScript("initArrayBox(document.getElementById('pre-launch-hook-array-box'));","initArrayBox(document.getElementById('on-launch-hook-array-box'));","initArrayBox(document.getElementById('on-exit-hook-array-box'));","initArrayBox(document.getElementById('jvm-args-array-box'));","initArrayBox(document.getElementById('env-args-array-box'));","document.querySelector('.instance-useFullscreen').checked = "+NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftFullscreen()+";","document.querySelector('.instance-windowWidth').value = "+NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftWindowWidth()+";","document.querySelector('.instance-windowHeight').value = "+NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftWindowHeight()+";");

                    for(int i = 0; i < NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftJVMArgs().size(); i++) {
                        String arg = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftJVMArgs().get(i);
                        frame.executeJavaScript("addToArrayBox('jvm-args-array-box',\""+arg+"\",'settings.remove.jvm-args-array-box."+i+"');");
                    }

                    for(int i = 0; i < NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftEnvArgs().size(); i++) {
                        String arg = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftEnvArgs().get(i);
                        frame.executeJavaScript("addToArrayBox('env-args-array-box',\""+arg+"\",'settings.remove.env-args-array-box."+i+"');");
                    }

                    for(int i = 0; i < NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPreLaunchCommands().size(); i++) {
                        String arg = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPreLaunchCommands().get(i);
                        frame.executeJavaScript("addToArrayBox('pre-launch-hook-array-box',\""+arg+"\",'settings.remove.pre-launch-hook-array-box."+i+"');");
                    }

                    for(int i = 0; i < NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnLaunchCommands().size(); i++) {
                        String arg = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnLaunchCommands().get(i);
                        frame.executeJavaScript("addToArrayBox('on-launch-hook-array-box',\""+arg+"\",'settings.remove.on-launch-hook-array-box."+i+"');");
                    }

                    for(int i = 0; i < NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnExitCommands().size(); i++) {
                        String arg = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnExitCommands().get(i);
                        frame.executeJavaScript("addToArrayBox('on-exit-hook-array-box',\""+arg+"\",'settings.remove.on-exit-hook-array-box."+i+"');");
                    }

                } else if(s.equals("java")) {
                    String p21 = NexusApplication.getInstance().getLocalSettings().getJava21Path();
                    String p17 = NexusApplication.getInstance().getLocalSettings().getJava17Path();
                    String p8 = NexusApplication.getInstance().getLocalSettings().getJava8Path();
                    if(!p21.equals(NexusApplication.getInstance().getWorkingPath()+"/libs/jre-21")) {
                        frame.executeJavaScript("document.querySelector('.java-21-path-value').querySelector('.right').querySelector('.d-none').classList.remove('d-none');");
                    }
                    if(!p17.equals(NexusApplication.getInstance().getWorkingPath()+"/libs/jre-17")) {
                        frame.executeJavaScript("document.querySelector('.java-17-path-value').querySelector('.right').querySelector('.d-none').classList.remove('d-none');");
                    }
                    if(!p8.equals(NexusApplication.getInstance().getWorkingPath()+"/libs/jre-8")) {
                        frame.executeJavaScript("document.querySelector('.java-8-path-value').querySelector('.right').querySelector('.d-none').classList.remove('d-none');");
                    }
                    frame.executeJavaScript("document.querySelector('.jre-21-path-value').innerText = '" + p21.replace("\\","/") + "';","document.querySelector('.jre-17-path-value').innerText = '" + p17.replace("\\","/") + "';","document.querySelector('.jre-8-path-value').innerText = '" + p8.replace("\\","/") + "';");

                    if(JavaUtilities.getJavaVersion(p8)==null) {
                        frame.executeJavaScript("document.querySelector('.jre-8-installed').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-8-warning').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-8-notInstalled').classList.remove('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-8-install-button').innerText = 'Install';");
                        frame.executeJavaScript("document.querySelector('.jre-8-warning').innerText = '';");
                    } else if(Objects.equals(JavaUtilities.getJavaVersion(p8), "8")) {
                        frame.executeJavaScript("document.querySelector('.jre-8-installed').classList.remove('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-8-install-button').innerText = 'Reinstall';");
                        frame.executeJavaScript("document.querySelector('.jre-8-warning').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-8-notInstalled').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-8-warning').innerText = '';");
                    } else {
                        frame.executeJavaScript("document.querySelector('.jre-8-installed').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-8-notInstalled').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-8-warning').innerText = 'Wrong Java version installed! Java "+ JavaUtilities.getJavaVersion(p8)+" is installed.';");
                        frame.executeJavaScript("document.querySelector('.jre-8-warning').classList.remove('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-8-install-button').innerText = 'Fix';");
                    }

                    if(JavaUtilities.getJavaVersion(p17)==null) {
                        frame.executeJavaScript("document.querySelector('.jre-17-installed').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-17-warning').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-17-notInstalled').classList.remove('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-17-warning').innerText = '';");
                        frame.executeJavaScript("document.querySelector('.jre-17-install-button').innerText = 'Install';");
                    } else if(Objects.equals(JavaUtilities.getJavaVersion(p17), "17")) {
                        frame.executeJavaScript("document.querySelector('.jre-17-installed').classList.remove('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-17-install-button').innerText = 'Reinstall';");
                        frame.executeJavaScript("document.querySelector('.jre-17-warning').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-17-notInstalled').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-17-warning').innerText = '';");
                    } else {
                        frame.executeJavaScript("document.querySelector('.jre-17-installed').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-17-notInstalled').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-17-warning').innerText = 'Wrong Java version installed! Java "+ JavaUtilities.getJavaVersion(p17)+" is installed.';");
                        frame.executeJavaScript("document.querySelector('.jre-17-warning').classList.remove('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-17-install-button').innerText = 'Fix';");
                    }

                    if(JavaUtilities.getJavaVersion(p21)==null) {
                        frame.executeJavaScript("document.querySelector('.jre-21-installed').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-21-warning').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-21-notInstalled').classList.remove('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-21-warning').innerText = '';");
                        frame.executeJavaScript("document.querySelector('.jre-21-install-button').innerText = 'Install';");
                    } else if(Objects.equals(JavaUtilities.getJavaVersion(p21), "21")) {
                        frame.executeJavaScript("document.querySelector('.jre-21-installed').classList.remove('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-21-install-button').innerText = 'Reinstall';");
                        frame.executeJavaScript("document.querySelector('.jre-21-warning').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-21-notInstalled').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-21-warning').innerText = '';");
                    } else {
                        frame.executeJavaScript("document.querySelector('.jre-21-installed').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-21-notInstalled').classList.add('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-21-warning').innerText = 'Wrong Java version installed! Java "+ JavaUtilities.getJavaVersion(p21)+" is installed.';");
                        frame.executeJavaScript("document.querySelector('.jre-21-warning').classList.remove('d-none');");
                        frame.executeJavaScript("document.querySelector('.jre-21-install-button').innerText = 'Fix';");
                    }
                }
            } else if(s.startsWith("add.")) {
                s = s.replaceFirst("add.","");
                if(s.startsWith("jvm-args-array-box.")) {
                    s = s.replaceFirst("jvm-args-array-box.","");
                    frame.executeJavaScript("initArrayBox(document.getElementById('jvm-args-array-box'));");
                    NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftJVMArgs().add(s);
                    NexusApplication.getInstance().getLocalSettings().setDefaultMinecraftJVMArgs(NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftJVMArgs());
                    for(int i = 0; i < NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftJVMArgs().size(); i++) {
                        String arg = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftJVMArgs().get(i);
                        frame.executeJavaScript("addToArrayBox('jvm-args-array-box',\""+arg+"\",'settings.remove.jvm-args-array-box."+i+"');");
                    }
                } else if(s.startsWith("env-args-array-box.")) {
                    s = s.replaceFirst("env-args-array-box.","");
                    frame.executeJavaScript("initArrayBox(document.getElementById('env-args-array-box'));");
                    NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftEnvArgs().add(s);
                    NexusApplication.getInstance().getLocalSettings().setDefaultMinecraftEnvArgs(NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftEnvArgs());
                    for(int i = 0; i < NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftEnvArgs().size(); i++) {
                        String arg = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftEnvArgs().get(i);
                        frame.executeJavaScript("addToArrayBox('env-args-array-box',\""+arg+"\",'settings.remove.env-args-array-box."+i+"');");
                    }
                } else if(s.startsWith("pre-launch-hook-array-box.")) {
                    s = s.replaceFirst("pre-launch-hook-array-box.","");
                    frame.executeJavaScript("initArrayBox(document.getElementById('pre-launch-hook-array-box'));");
                    NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPreLaunchCommands().add(s);
                    NexusApplication.getInstance().getLocalSettings().setDefaultMinecraftPreLaunchCommands(NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPreLaunchCommands());
                    for(int i = 0; i < NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPreLaunchCommands().size(); i++) {
                        String arg = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPreLaunchCommands().get(i);
                        frame.executeJavaScript("addToArrayBox('pre-launch-hook-array-box',\""+arg+"\",'settings.remove.pre-launch-hook-array-box."+i+"');");
                    }
                } else if(s.startsWith("on-launch-hook-array-box.")) {
                    s = s.replaceFirst("on-launch-hook-array-box.","");
                    frame.executeJavaScript("initArrayBox(document.getElementById('on-launch-hook-array-box'));");
                    NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnLaunchCommands().add(s);
                    NexusApplication.getInstance().getLocalSettings().setDefaultMinecraftOnLaunchCommands(NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnLaunchCommands());
                    for(int i = 0; i < NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnLaunchCommands().size(); i++) {
                        String arg = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnLaunchCommands().get(i);
                        frame.executeJavaScript("addToArrayBox('on-launch-hook-array-box',\""+arg+"\",'settings.remove.on-launch-hook-array-box."+i+"');");
                    }
                } else if(s.startsWith("on-exit-hook-array-box.")) {
                    s = s.replaceFirst("on-exit-hook-array-box.","");
                    frame.executeJavaScript("initArrayBox(document.getElementById('on-exit-hook-array-box'));");
                    NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnExitCommands().add(s);
                    NexusApplication.getInstance().getLocalSettings().setDefaultMinecraftOnExitCommands(NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnExitCommands());
                    for(int i = 0; i < NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnExitCommands().size(); i++) {
                        String arg = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnExitCommands().get(i);
                        frame.executeJavaScript("addToArrayBox('on-exit-hook-array-box',\""+arg+"\",'settings.remove.on-exit-hook-array-box."+i+"');");
                    }
                }
            } else if(s.startsWith("remove.")) {
                s = s.replaceFirst("remove.","");
                if(s.startsWith("jvm-args-array-box.")) {
                    int index = Integer.parseInt(s.replaceFirst("jvm-args-array-box.",""));
                    frame.executeJavaScript("initArrayBox(document.getElementById('jvm-args-array-box'));");
                    NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftJVMArgs().remove(index);
                    NexusApplication.getInstance().getLocalSettings().setDefaultMinecraftJVMArgs(NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftJVMArgs());
                    for(int i = 0; i < NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftJVMArgs().size(); i++) {
                        String arg = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftJVMArgs().get(i);
                        frame.executeJavaScript("addToArrayBox('jvm-args-array-box',\""+arg+"\",'settings.remove.jvm-args-array-box."+i+"');");
                    }
                } else if(s.startsWith("env-args-array-box.")) {
                    int index = Integer.parseInt(s.replaceFirst("env-args-array-box.",""));
                    frame.executeJavaScript("initArrayBox(document.getElementById('env-args-array-box'));");
                    NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftEnvArgs().remove(index);
                    NexusApplication.getInstance().getLocalSettings().setDefaultMinecraftEnvArgs(NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftEnvArgs());
                    for(int i = 0; i < NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftEnvArgs().size(); i++) {
                        String arg = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftEnvArgs().get(i);
                        frame.executeJavaScript("addToArrayBox('env-args-array-box',\""+arg+"\",'settings.remove.env-args-array-box."+i+"');");
                    }
                } else if(s.startsWith("pre-launch-hook-array-box.")) {
                    int index = Integer.parseInt(s.replaceFirst("pre-launch-hook-array-box.",""));
                    frame.executeJavaScript("initArrayBox(document.getElementById('pre-launch-hook-array-box'));");
                    NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPreLaunchCommands().remove(index);
                    NexusApplication.getInstance().getLocalSettings().setDefaultMinecraftPreLaunchCommands(NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPreLaunchCommands());
                    for(int i = 0; i < NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPreLaunchCommands().size(); i++) {
                        String arg = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPreLaunchCommands().get(i);
                        frame.executeJavaScript("addToArrayBox('pre-launch-hook-array-box',\""+arg+"\",'settings.remove.pre-launch-hook-array-box."+i+"');");
                    }
                } else if(s.startsWith("on-launch-hook-array-box.")) {
                    int index = Integer.parseInt(s.replaceFirst("on-launch-hook-array-box.",""));
                    frame.executeJavaScript("initArrayBox(document.getElementById('on-launch-hook-array-box'));");
                    NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnLaunchCommands().remove(index);
                    NexusApplication.getInstance().getLocalSettings().setDefaultMinecraftOnLaunchCommands(NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnLaunchCommands());
                    for(int i = 0; i < NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnLaunchCommands().size(); i++) {
                        String arg = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnLaunchCommands().get(i);
                        frame.executeJavaScript("addToArrayBox('on-launch-hook-array-box',\""+arg+"\",'settings.remove.on-launch-hook-array-box."+i+"');");
                    }
                } else if(s.startsWith("on-exit-hook-array-box.")) {
                    int index = Integer.parseInt(s.replaceFirst("on-exit-hook-array-box.",""));
                    frame.executeJavaScript("initArrayBox(document.getElementById('on-exit-hook-array-box'));");
                    NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnExitCommands().remove(index);
                    NexusApplication.getInstance().getLocalSettings().setDefaultMinecraftOnExitCommands(NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnExitCommands());
                    for(int i = 0; i < NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnExitCommands().size(); i++) {
                        String arg = NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftOnExitCommands().get(i);
                        frame.executeJavaScript("addToArrayBox('on-exit-hook-array-box',\""+arg+"\",'settings.remove.on-exit-hook-array-box."+i+"');");
                    }
                }
            } else if(s.startsWith("installJava.")) {
                s = s.replaceFirst("installJava.","");
                switch (s) {
                    case "8" -> {if(new File(NexusApplication.getInstance().getLocalSettings().getJava8Path()).exists()) { FileActions.deleteFolder(new File(NexusApplication.getInstance().getLocalSettings().getJava8Path())); }}
                    case "17" -> {if(new File(NexusApplication.getInstance().getLocalSettings().getJava17Path()).exists()) { FileActions.deleteFolder(new File(NexusApplication.getInstance().getLocalSettings().getJava17Path())); }}
                    case "21" -> {if(new File(NexusApplication.getInstance().getLocalSettings().getJava21Path()).exists()) { FileActions.deleteFolder(new File(NexusApplication.getInstance().getLocalSettings().getJava21Path())); }}
                }
                JavaUtilities.installJava(s,"default");
            } else if(s.startsWith("reset.")) {
                s = s.replace("reset.", "");
                if(s.startsWith("javaPath.")) {
                    s = s.replaceFirst("javaPath.","");
                    switch (s) {
                        case "21" -> NexusApplication.getInstance().getLocalSettings().setJre21path(NexusApplication.getInstance().getWorkingPath()+"/libs/jre-21");
                        case "17" -> NexusApplication.getInstance().getLocalSettings().setJre17path(NexusApplication.getInstance().getWorkingPath()+"/libs/jre-17");
                        case "8" -> NexusApplication.getInstance().getLocalSettings().setJre8path(NexusApplication.getInstance().getWorkingPath()+"/libs/jre-8");
                    }
                    resolveMessage("settings.init.java");
                } else if(s.equals("instancePath")) {
                    String path = NexusApplication.getInstance().getWorkingPath()+"/instances/";
                    NexusApplication.getInstance().getLocalSettings().setDefaultMinecraftPath(path);
                    frame.executeJavaScript("document.querySelector('.instance-default-path-value').innerText = '" + path + "';");
                }
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
                            if(!pathString.equals(NexusApplication.getInstance().getWorkingPath()+"/instances/")) {
                                frame.executeJavaScript("document.querySelector('.instance-default-path').querySelector('.right').querySelector('.d-none').classList.remove('d-none');");
                            }
                        } else {
                            NexusApplication.getLogger().err("[NEXUS] The selected path is not a directory: " + path.getAbsolutePath());
                        }
                    }
                } else if(s.startsWith("javaPath.")) {
                    s = s.replaceFirst("javaPath.","");
                    JnaFileChooser fc = new JnaFileChooser();
                    fc.setMode(JnaFileChooser.Mode.Directories);
                    if (fc.showOpenDialog(frame)) {
                        File path = fc.getSelectedFile();
                        if(path.isDirectory()) {
                            String pathString = path.getAbsolutePath().replace("\\","/");
                            if(!pathString.endsWith("/")) {
                                pathString += "/";
                            }
                            switch (s) {
                                case "21" -> NexusApplication.getInstance().getLocalSettings().setJre21path(pathString);
                                case "17" -> NexusApplication.getInstance().getLocalSettings().setJre17path(pathString);
                                case "8" -> NexusApplication.getInstance().getLocalSettings().setJre8path(pathString);
                            }
                            resolveMessage("settings.init.java");
                        } else {
                            NexusApplication.getLogger().err("[NEXUS] The selected path is not a directory: " + path.getAbsolutePath());
                        }
                    }
                }
            } else if(s.startsWith("open.")) {
                s = s.replace("open.", "");
                if (s.startsWith("javaPath.")) {
                    s = s.replaceFirst("javaPath.", "");
                    try {
                        if (Desktop.isDesktopSupported()) {
                            String path = null;
                            switch (s) {
                                case "21" -> path = NexusApplication.getInstance().getLocalSettings().getJava21Path();
                                case "17" -> path = NexusApplication.getInstance().getLocalSettings().getJava17Path();
                                case "8" -> path = NexusApplication.getInstance().getLocalSettings().getJava8Path();
                            }
                            if (path != null) {
                                new File(path).mkdirs();
                                Desktop.getDesktop().open(new File(path));
                            }
                        }
                    } catch (Exception e) {
                        NexusApplication.getLogger().err(e.getMessage());
                    }
                } else if (s.equals("instancePath")) {
                    try {
                        if (Desktop.isDesktopSupported()) {
                            if(Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().open(new File(NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftPath()));
                            }
                        }
                    } catch (Exception e) {
                        NexusApplication.getLogger().err(e.getMessage());
                    }
                }

            } else if(s.startsWith("set.")) {
                s = s.replace("set.","");
                if(s.startsWith("defaultMemory.")) {
                    int memory = Integer.parseInt(s.replace("defaultMemory.", ""));
                    NexusApplication.getInstance().getLocalSettings().setDefaultMinecraftMemory(memory);
                } else if(s.startsWith("windowWidth.")) {
                    int width = Integer.parseInt(s.replace("windowWidth.", ""));
                    NexusApplication.getInstance().getLocalSettings().setDefaultMinecraftWindowWidth(width);
                } else if(s.startsWith("windowHeight.")) {
                    int height = Integer.parseInt(s.replace("windowHeight.", ""));
                    NexusApplication.getInstance().getLocalSettings().setDefaultMinecraftWindowHeight(height);
                } else if(s.startsWith("fullscreen.")) {
                    boolean bool = s.replace("fullscreen.", "").equals("true");
                    NexusApplication.getInstance().getLocalSettings().setDefaultMinecraftFullscreen(bool);
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
                    ModrinthResource project = new ModrinthResource(id);
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
                NexusApplication.getInstance().getLocalSettings().setLastInstanceId(showId);
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
                LocalInstance lI = NexusApplication.getInstance().getInstanceManager().getInstance(id);
                ReadableZynstance instance = NexusApplication.getInstance().getInstanceManager().getInstance(id).getInstance();
                if(instance != null) {
                    String mc = instance.getMinecraftVersion();
                    String modloaderVersion;
                    if(instance.getModloader().equalsIgnoreCase("fabric")) {
                        modloaderVersion = instance.getFabricVersion();
                        MinecraftVersion.Type type = MinecraftVersion.getType(instance.getMinecraftVersion());
                        if(type!=null) {
                            JavaUtilities.setJava(type);
                        }
                        FabricLauncher launcher = NexusApplication.getInstance().getFabricLauncher();
                        launcher.setPreLaunchHook(GameHooks.getPreLaunchHook(launcher,lI));
                        launcher.setPostLaunchHook(GameHooks.getPostLaunchHook(launcher,lI));
                        launcher.setGameCloseHook(GameHooks.getGameCloseHook(launcher,lI));

                        if(lI.isFullscreen()) {
                            launcher.addAdditionalEnvironmentalArgs("--fullscreen");
                        }
                        launcher.addAdditionalEnvironmentalArgs("--width",lI.getWidth()+"","--height",lI.getHeight()+"");

                        launcher.launch(mc, modloaderVersion, NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftMemory(), Path.of(NexusApplication.getInstance().getInstanceManager().getInstance(id).getPath()), instance.getId());
                        NexusApplication.getInstance().getInstanceManager().addRunningInstance(launcher.getGameProcess(), id);
                    } else if(instance.getModloader().equalsIgnoreCase("forge")) {
                        modloaderVersion = instance.getForgeVersion();
                        MinecraftVersion.Type type = MinecraftVersion.getType(instance.getMinecraftVersion());
                        if(type!=null) {
                            JavaUtilities.setJava(type);
                        }
                        ForgeLauncher launcher = NexusApplication.getInstance().getForgeLauncher();
                        launcher.setPreLaunchHook(GameHooks.getPreLaunchHook(launcher,lI));
                        launcher.setPostLaunchHook(GameHooks.getPostLaunchHook(launcher,lI));
                        launcher.setGameCloseHook(GameHooks.getGameCloseHook(launcher,lI));

                        if(lI.isFullscreen()) {
                            launcher.addAdditionalEnvironmentalArgs("--fullscreen");
                        }
                        launcher.addAdditionalEnvironmentalArgs("--width",lI.getWidth()+"","--height",lI.getHeight()+"");

                        launcher.launch(mc,modloaderVersion,NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftMemory(), Path.of(NexusApplication.getInstance().getInstanceManager().getInstance(id).getPath()),instance.getId());
                        NexusApplication.getInstance().getInstanceManager().addRunningInstance(launcher.getGameProcess(), id);
                    } else if(instance.getModloader().equalsIgnoreCase("neoforge")) {
                        modloaderVersion = instance.getNeoForgeVersion();
                        MinecraftVersion.Type type = MinecraftVersion.getType(instance.getMinecraftVersion());
                        if(type!=null) {
                            JavaUtilities.setJava(type);
                        }
                        NeoForgeLauncher launcher = NexusApplication.getInstance().getNeoForgeLauncher();
                        launcher.setPreLaunchHook(GameHooks.getPreLaunchHook(launcher,lI));
                        launcher.setPostLaunchHook(GameHooks.getPostLaunchHook(launcher,lI));
                        launcher.setGameCloseHook(GameHooks.getGameCloseHook(launcher,lI));

                        if(lI.isFullscreen()) {
                            launcher.addAdditionalEnvironmentalArgs("--fullscreen");
                        }
                        launcher.addAdditionalEnvironmentalArgs("--width",lI.getWidth()+"","--height",lI.getHeight()+"");

                        launcher.launch(mc, modloaderVersion, NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftMemory(), Path.of(NexusApplication.getInstance().getInstanceManager().getInstance(id).getPath()), instance.getId());
                        NexusApplication.getInstance().getInstanceManager().addRunningInstance(launcher.getGameProcess(), id);
                    } else if(instance.getModloader().equalsIgnoreCase("quilt")) {
                        modloaderVersion = instance.getQuiltVersion();
                        MinecraftVersion.Type type = MinecraftVersion.getType(instance.getMinecraftVersion());
                        if(type!=null) {
                            JavaUtilities.setJava(type);
                        }
                        QuiltLauncher launcher = NexusApplication.getInstance().getQuiltLauncher();
                        launcher.setPreLaunchHook(GameHooks.getPreLaunchHook(launcher,lI));
                        launcher.setPostLaunchHook(GameHooks.getPostLaunchHook(launcher,lI));
                        launcher.setGameCloseHook(GameHooks.getGameCloseHook(launcher,lI));

                        if(lI.isFullscreen()) {
                            launcher.addAdditionalEnvironmentalArgs("--fullscreen");
                        }
                        launcher.addAdditionalEnvironmentalArgs("--width",lI.getWidth()+"","--height",lI.getHeight()+"");

                        launcher.launch(mc, modloaderVersion, NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftMemory(), Path.of(NexusApplication.getInstance().getInstanceManager().getInstance(id).getPath()), instance.getId());
                        NexusApplication.getInstance().getInstanceManager().addRunningInstance(launcher.getGameProcess(), id);
                    } else {
                        VanillaLauncher launcher = NexusApplication.getInstance().getVanillaLauncher();
                        MinecraftVersion.Type type = MinecraftVersion.getType(instance.getMinecraftVersion());
                        if(type!=null) {
                            JavaUtilities.setJava(type);
                        }
                        launcher.setPreLaunchHook(GameHooks.getPreLaunchHook(launcher,lI));
                        launcher.setPostLaunchHook(GameHooks.getPostLaunchHook(launcher,lI));
                        launcher.setGameCloseHook(GameHooks.getGameCloseHook(launcher,lI));

                        if(lI.isFullscreen()) {
                            launcher.addAdditionalEnvironmentalArgs("--fullscreen");
                        }
                        launcher.addAdditionalEnvironmentalArgs("--width",lI.getWidth()+"","--height",lI.getHeight()+"");

                        launcher.launch(mc, NexusApplication.getInstance().getLocalSettings().getDefaultMinecraftMemory(), Path.of(NexusApplication.getInstance().getInstanceManager().getInstance(id).getPath()), instance.getId());
                        NexusApplication.getInstance().getInstanceManager().addRunningInstance(launcher.getGameProcess(), id);
                    }
                }
            } else if(s.startsWith("delete.")) {
                frame.executeJavaScript("document.body.querySelector('.settings-deletion-delete').innerHTML = 'Deleting instance...';");
                String id = s.replace("delete.", "");
                NexusApplication.getInstance().getInstanceManager().removeInstance(id);
                String absolutePath = new File(id).getAbsolutePath();
                System.gc();
                try {
                    if(!FileUtilities.deleteDirectory(new File(absolutePath))) {
                        Thread.sleep(1000);
                        if(!FileUtilities.deleteDirectory(new File(absolutePath))) {
                            throw new RuntimeException("Couldn't delete instance folder: "+absolutePath);
                        }
                    }
                    frame.getBrowser().reload();
                } catch (Exception e) {
                    NexusApplication.getLogger().err(e.getMessage());
                }
            } else if(s.startsWith("settings.")) {
                String id = s.replace("settings.", "");
                LocalInstance instance = NexusApplication.getInstance().getInstanceManager().getInstance(id);
                String path = new File(instance.getPath()).getAbsolutePath().replace("\\","\\\\");
                frame.executeJavaScript("document.body.querySelector('.settings-deletion-delete').onclick = function () { console.log('[CONNECTOR] library.delete."+id+"'); }","document.body.querySelector('.settings-deletion-folder').onclick = function () { console.log('[CONNECTOR] library.folder."+id+"'); }","document.body.querySelector('.settings-deletion-id').innerText = \""+instance.getInstance().getId()+"\";","document.body.querySelector('.settings-deletion-name').innerText = \""+instance.getInstance().getName()+"\";","document.body.querySelector('.settings-deletion-version').innerText = \""+instance.getInstance().getVersion()+"\";","document.body.querySelector('.settings-deletion-path').innerText = \""+path+"\";","document.body.querySelector('.settings-instance-title').innerText = \""+instance.getInstance().getName()+"\";","showSettingsPane('General');");
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
        }
    }
}

