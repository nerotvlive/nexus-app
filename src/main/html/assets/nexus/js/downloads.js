function setRunningDownload(id,name,timeElapsed,timeRemaining,sizeDownload,sizeFile,progress,speed,url,path) {
    if(document.getElementById("running-download")) {
        document.getElementById("running-download").remove();
        document.getElementById("downloads-loader-card").classList.remove("d-none");
    }

    if(id && name && timeElapsed && timeRemaining && sizeDownload && sizeFile && progress && speed && url && path) {
        if(document.getElementById(id)) {
            document.getElementById(id).remove();
        }
        const template = document.getElementById("running-download-template");
        const download = template.cloneNode(true);
        download.id = "running-download";
        download.classList.remove("d-none");
        download.classList.add(id);
        download.querySelector(".download-name").innerHTML = "<strong>"+name+"</strong>";
        download.querySelector(".downloadId").innerText = id;
        download.querySelector(".downloadUrl").innerText = url;
        download.querySelector(".downloadPath").innerText = path;
        download.querySelector(".timeElapsed").innerText = timeElapsed;
        download.querySelector(".timeRemaining").innerText = timeRemaining;
        download.querySelector(".downloadSize").innerText = sizeDownload;
        download.querySelector(".fileSize").innerText = sizeFile;
        download.querySelector(".progressInfo").innerText = progress+"%";
        download.querySelector(".downloadSpeed").innerText = speed;
        download.querySelector(".progress-bar").style.width = progress+"%";

        if(getStorageItem("devtools")) {
            if(getStorageItem("devtools") === "true") {
                download.querySelector(".debug-info").classList.remove("d-none");
            }
        }

        template.parentNode.insertBefore(download, template);
        document.getElementById("downloads-loader-card").classList.add("d-none");
    }
}

function updateRunningDownload(id,name,timeElapsed,timeRemaining,sizeDownload,sizeFile,progress,speed,url,path) {
    if(id && name && timeElapsed && timeRemaining && sizeDownload && sizeFile && progress && speed && url && path) {
        if (document.getElementById("running-download")) {
            const download = document.getElementById("running-download");
            if (download.classList.contains(id)) {
                download.querySelector(".timeElapsed").innerText = timeElapsed;
                download.querySelector(".timeRemaining").innerText = timeRemaining;
                download.querySelector(".fileSize").innerText = sizeFile;
                download.querySelector(".progressInfo").innerText = progress+"%";
                download.querySelector(".progress-bar").style.width = progress+"%";
                download.querySelector(".downloadSpeed").innerText = speed;

                if(getStorageItem("devtools")) {
                    if(getStorageItem("devtools") === "true") {
                        download.querySelector(".debug-info").classList.remove("d-none");
                    }
                }
                return;
            }
        }
        setRunningDownload(id, name, timeElapsed, timeRemaining, sizeDownload, sizeFile, progress, speed, url, path);
    }
}

function addHistoryDownload(id,name,success) {
    if(id && name) {
        if(document.getElementById("running-download")) {
            if(document.getElementById("running-download").classList.contains(id.replace("-history", ""))) {
                document.getElementById("running-download").remove();
                document.getElementById("downloads-loader-card").classList.remove("d-none");
            }
        }
        if (!document.getElementById(id)) {
            const template = document.getElementById("history-download-template");
            const download = template.cloneNode(true);
            download.id = id;
            download.classList.remove("d-none");
            download.querySelector(".downloadName").innerText = name;
            if (success === true) {
                download.classList.add("finished");
                download.querySelector(".downloadSuccessIcon").classList.add("bi-check-lg");
            } else if (success === false) {
                download.classList.add("failed");
                download.querySelector(".downloadSuccessIcon").classList.add("bi-x-lg");
            }
            template.parentNode.insertBefore(download, template);
        }
    }
}

function addWaitingDownload(id,name,url,path,index) {
    if(id && name && url && path && index) {
        if (!document.getElementById(id)) {
            if (document.getElementById("running-download")) {
                if (document.getElementById("running-download").classList.contains(id)) {
                    document.getElementById("running-download").remove();
                    document.getElementById("downloads-loader-card").classList.remove("d-none");
                }
            }
            const template = document.getElementById("waiting-download-template");
            const download = template.cloneNode(true);
            download.id = id;
            download.classList.remove("d-none");
            download.querySelector(".download-name").innerText = name;
            download.querySelector(".downloadId").innerText = id;
            download.querySelector(".downloadUrl").innerText = url;
            download.querySelector(".downloadPath").innerText = path;
            download.querySelector(".downloadIndex").innerText = index;
            if (getStorageItem("devtools")) {
                if (getStorageItem("devtools") === "true") {
                    download.querySelector(".debug-info").classList.remove("d-none");
                }
            }
            template.parentNode.insertBefore(download, template);
        }
    }
}

function initDownloads() {
    console.log("[CONNECTOR] downloads.init");
    initLoader();
}
initDownloads();

async function initLoader() {
    setTimeout(function (){
        if(document.getElementById("downloads-loader")) {
            document.getElementById("downloads-loader").innerHTML = "<strong>No running download!</strong>";
        }
    }, 2000);
}