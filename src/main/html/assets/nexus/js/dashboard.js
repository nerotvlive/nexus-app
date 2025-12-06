function initDashboard() {
    document.getElementById('nappv').innerText = version;
    highlight(document.getElementById("discover-button"));
    document.querySelector(".menu-panel").querySelector(".card-body").innerHTML = "<i onclick='window.open(`https://discord.gg/hbHDrqUjJ8`,`_blank`);' class='bi bi-discord'></i><i onclick='window.open(`https://github.com/zyneonstudios/nexus-app`,`_blank`);' class='bi bi-github'></i><i onclick='window.open(`https://nexus.zyneonstudios.org/app`,`_blank`);' class='bi bi-globe'></i><i onclick='console.log(`[CONNECTOR] exit`)' class='bi bi-door-open'></i>";
    const urlParams = new URLSearchParams(window.location.search);
    if(urlParams.has("q")) {
        loadPage("search.html",false,"?q="+urlParams.get("q"));
    } else if(urlParams.has("dt")) {
        if(urlParams.get("dt")==="search") {
            loadPage("search.html",false);
        }
    }

    if (getStorageItem("devtools") === "true") {
        document.getElementById("dev-mode-warning").classList.remove("d-none");
    }
}