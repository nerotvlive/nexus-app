package com.zyneonstudios.nexus.application.listeners;

import com.zyneonstudios.nexus.application.utilities.DiscordRichPresence;
import com.zyneonstudios.nexus.application.utilities.MicrosoftAuthenticator;
import com.zyneonstudios.nexus.application.events.PageLoadedEvent;
import com.zyneonstudios.nexus.application.main.NexusApplication;

public class PageLoadListener extends PageLoadedEvent {

    public PageLoadListener() {
        super(null);
    }

    @Override
    public boolean onLoad() {
        NexusApplication.getInstance().getApplicationFrame().executeJavaScript("enableDevTools("+NexusApplication.getLogger().isDebugging()+");","app = true;","localStorage.setItem('enabled','true');","version = 'Desktop v"+NexusApplication.getInstance().getVersion()+"';");
        if(getUrl().toLowerCase().contains("page=library")) {
            if(MicrosoftAuthenticator.isLoggedIn()) {
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.querySelector('.menu-panel').querySelector('.card-body').innerHTML = \"<div style='margin-left: 0.5rem;'><img src='https://cravatar.eu/helmhead/"+MicrosoftAuthenticator.getUUID()+"/128.png'></div><div class='w-100 h-100 p-2 d-flex flex-column'><p>Account: <label><select id='authenticatedAccounts' onchange=\\\"console.log('[CONNECTOR] login.'+this.value); document.getElementById('login-overlay').innerText = 'Please wait...';\\\"><option value='"+MicrosoftAuthenticator.getUUID()+"'>"+MicrosoftAuthenticator.getUsername()+"</option></select></label><br><a onclick=\\\"loadPage('settings.html',menu,'&st=account-settings&app=true');\\\">Manage account(s)</a></p></div>\";");
                for(String u:MicrosoftAuthenticator.getDecryptedAuthenticatedUUIDs()) {
                    if (!u.equals(MicrosoftAuthenticator.getUUID())) {
                        String n = MicrosoftAuthenticator.getDecryptedAuthenticatedUsername(u);
                        NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.getElementById('authenticatedAccounts').innerHTML += \"<option value='" + u + "'>" + n + "</option>\"");
                    }
                }
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.getElementById('authenticatedAccounts').innerHTML += \"<option value='new'>Add account</option>\"");
            } else {
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("loadPage('login.html');");
            }
            DiscordRichPresence.setDetails("Looking at their library...");
        }
        if(getUrl().toLowerCase().contains("page=login")) {
            if(MicrosoftAuthenticator.isLoggedIn()) {
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("loadPage('library.html');");
            }
            DiscordRichPresence.setDetails("Looking at their library...");
        }
        if(getUrl().toLowerCase().contains("page=discover")) {
            DiscordRichPresence.setDetails("Exploring resources...");
        }
        if(getUrl().toLowerCase().contains("page=settings")) {
            DiscordRichPresence.setDetails("Customizing their settings...");
        }
        if(getUrl().toLowerCase().contains("page=downloads")) {
            DiscordRichPresence.setDetails("Looking at downloads...");
        }
        return true;
    }
}
