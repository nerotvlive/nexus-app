function setRunningSetting(id,name,timeElapsed,timeRemaining,sizeSetting,sizeFile,progress,speed,url,path) {
    if(document.getElementById("running-setting")) {
        document.getElementById("running-setting").remove();
        document.getElementById("settings-loader-card").classList.remove("d-none");
    }

    if(id && name && timeElapsed && timeRemaining && sizeSetting && sizeFile && progress && speed && url && path) {
        if(document.getElementById(id)) {
            document.getElementById(id).remove();
        }
        const template = document.getElementById("running-setting-template");
        const setting = template.cloneNode(true);
        setting.id = "running-setting";
        setting.classList.remove("d-none");
        setting.classList.add(id);
        setting.querySelector(".setting-name").innerHTML = "<strong>"+name+"</strong>";
        setting.querySelector(".settingId").innerText = id;
        setting.querySelector(".settingUrl").innerText = url;
        setting.querySelector(".settingPath").innerText = path;
        setting.querySelector(".timeElapsed").innerText = timeElapsed;
        setting.querySelector(".timeRemaining").innerText = timeRemaining;
        setting.querySelector(".settingSize").innerText = sizeSetting;
        setting.querySelector(".fileSize").innerText = sizeFile;
        setting.querySelector(".progressInfo").innerText = progress+"%";
        setting.querySelector(".settingSpeed").innerText = speed;
        setting.querySelector(".progress-bar").style.width = progress+"%";

        if(getStorageItem("devtools")) {
            if(getStorageItem("devtools") === "true") {
                setting.querySelector(".debug-info").classList.remove("d-none");
            }
        }

        template.parentNode.insertBefore(setting, template);
        document.getElementById("settings-loader-card").classList.add("d-none");
    }
}

function updateRunningSetting(id,name,timeElapsed,timeRemaining,sizeSetting,sizeFile,progress,speed,url,path) {
    if(id && name && timeElapsed && timeRemaining && sizeSetting && sizeFile && progress && speed && url && path) {
        if (document.getElementById("running-setting")) {
            const setting = document.getElementById("running-setting");
            if (setting.classList.contains(id)) {
                setting.querySelector(".timeElapsed").innerText = timeElapsed;
                setting.querySelector(".timeRemaining").innerText = timeRemaining;
                setting.querySelector(".fileSize").innerText = sizeFile;
                setting.querySelector(".progressInfo").innerText = progress+"%";
                setting.querySelector(".progress-bar").style.width = progress+"%";
                setting.querySelector(".settingSpeed").innerText = speed;

                if(getStorageItem("devtools")) {
                    if(getStorageItem("devtools") === "true") {
                        setting.querySelector(".debug-info").classList.remove("d-none");
                    }
                }
                return;
            }
        }
        setRunningSetting(id, name, timeElapsed, timeRemaining, sizeSetting, sizeFile, progress, speed, url, path);
    }
}

function addHistorySetting(id,name,success) {
    if(id && name) {
        if(document.getElementById("running-setting")) {
            if(document.getElementById("running-setting").classList.contains(id.replace("-history", ""))) {
                document.getElementById("running-setting").remove();
                document.getElementById("settings-loader-card").classList.remove("d-none");
            }
        }
        if (!document.getElementById(id)) {
            const template = document.getElementById("history-setting-template");
            const setting = template.cloneNode(true);
            setting.id = id;
            setting.classList.remove("d-none");
            setting.querySelector(".settingName").innerText = name;
            if (success === true) {
                setting.classList.add("finished");
                setting.querySelector(".settingSuccessIcon").classList.add("bi-check-lg");
            } else if (success === false) {
                setting.classList.add("failed");
                setting.querySelector(".settingSuccessIcon").classList.add("bi-x-lg");
            }
            template.parentNode.insertBefore(setting, template);
        }
    }
}

function addWaitingSetting(id,name,url,path,index) {
    if(id && name && url && path && index) {
        if (!document.getElementById(id)) {
            if (document.getElementById("running-setting")) {
                if (document.getElementById("running-setting").classList.contains(id)) {
                    document.getElementById("running-setting").remove();
                    document.getElementById("settings-loader-card").classList.remove("d-none");
                }
            }
            const template = document.getElementById("waiting-setting-template");
            const setting = template.cloneNode(true);
            setting.id = id;
            setting.classList.remove("d-none");
            setting.querySelector(".setting-name").innerText = name;
            setting.querySelector(".settingId").innerText = id;
            setting.querySelector(".settingUrl").innerText = url;
            setting.querySelector(".settingPath").innerText = path;
            setting.querySelector(".settingIndex").innerText = index;
            if (getStorageItem("devtools")) {
                if (getStorageItem("devtools") === "true") {
                    setting.querySelector(".debug-info").classList.remove("d-none");
                }
            }
            template.parentNode.insertBefore(setting, template);
        }
    }
}

function initSettings() {
    console.log("[CONNECTOR] settings.init");
    initLoader();
}
initSettings();

async function initLoader() {
    setTimeout(function (){
        if(document.getElementById("settings-loader")) {
            document.getElementById("settings-loader").innerHTML = "<strong>Not implemented!</strong>";
        }
    }, 2000);
}