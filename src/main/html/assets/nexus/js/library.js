function addInstance(id,name,icon,group) {

    id = decodeURIComponent(id);
    name = decodeURIComponent(name);
    icon = decodeURIComponent(icon);
    group = decodeURIComponent(group);

    if(!document.getElementById(id)) {
        let list = document.getElementById("instance-list");
        if (group) {
            if (document.getElementById(group)) {
                list = document.getElementById(group);
            }
        }
        const template = list.querySelector(".instance-list-template");
        if (template) {
            const button = template.cloneNode(true);
            button.classList.remove("d-none");
            button.classList.remove("instance-list-template");

            if (id && name) {
                button.id = id;
                button.onclick = function () {
                    console.log("[CONNECTOR] library.showInstance."+id);
                }
                button.querySelector("span").innerText = name;

                if(icon) {
                    button.querySelector("img").src = icon;
                    button.querySelector("img").display = "";
                    button.querySelector("i").remove();
                } else {
                    button.querySelector("i").className = "bi bi-dice-"+(Math.floor(Math.random() * 6) + 1);
                    if(Math.random() < 0.5) {
                        button.querySelector("i").className = button.querySelector("i").className + "-fill";
                    }
                    button.querySelector("img").remove();
                }

                template.parentElement.insertBefore(button, template);
            }
        }
    }
}

function addInstanceGroup(id,name,colorName) {
    if(!document.getElementById(id)) {
        let list = document.getElementById("instance-list");
        const template = list.querySelector(".instance-group-template");
        if (template&&id&&name) {
            const group = template.cloneNode(true);
            group.id = id;
            group.classList.remove("d-none");
            group.classList.remove("instance-group-template");
            group.querySelector(".collapse").id = id+"-collapse";
            group.querySelector("a").id = id+"-collapse-button";
            group.querySelector("a").onclick = function () {
                toggleSubMenuGroup(id+"-collapse")
            };
            group.querySelector("h6").innerText = name;
            group.querySelector("h6").onclick = function () {
                toggleSubMenuGroup(id+"-collapse")
            };
            if(colorName) {
                group.classList.add(colorName);
            }
            template.parentElement.insertBefore(group, template);
        }
    }
}

function loadFolderButtonHoverEvent() {
    const button = document.querySelector(".title-menu").querySelector(".buttons").querySelector(".folder");
    const icon = button.querySelector("i");
    button.addEventListener("mouseover", () => {
        icon.className = "bi bi-folder2-open";
    });
    button.addEventListener("mouseout", () => {
        icon.className = "bi bi-folder2";
    });
}

function initLibrary() {
    console.log("[CONNECTOR] library.init");
}

function showInstance(id,name,version,summary,description,tagsString) {
    if(!document.getElementById("update-button").classList.contains("d-none")) {
        document.getElementById("update-button").classList.add("d-none");
    }
    document.getElementById("library-title").querySelector("span").classList.remove("icon");
    document.getElementById("library-title").querySelector("img").src = "";
    id = decodeURIComponent(id);
    name = decodeURIComponent(name);
    version = decodeURIComponent(version);
    summary = decodeURIComponent(summary);
    description = decodeURIComponent(description);

    if(activeInstance) {
        if(document.getElementById(activeInstance)) {
            document.getElementById(activeInstance).classList.remove("active");
        }
    }

    activeInstance = id;
    if(document.getElementById(activeInstance)) {
        document.getElementById(activeInstance).classList.add("active");
    }
    document.getElementById("library-title").querySelector("span").innerText = name;

        if(document.getElementById(id)&&document.getElementById(id).querySelector("img")&&document.getElementById(id).querySelector("img").src) {
            document.getElementById("library-title").querySelector("img").src = document.getElementById(id).querySelector("img").src;
            document.getElementById("library-title").querySelector("span").classList.add("icon");
        }

    document.getElementById("instance-view").style.display = "flex";
    document.getElementById("launch-button").style.display = "";
    document.getElementById("instance-name").innerText = name;
    document.getElementById("instance-version").innerText = version;
    document.getElementById("instance-summary").innerText = summary;
    document.getElementById("tab-about-content").innerHTML = marked.parse(description);
    openLinksInNewTab(document.getElementById("tab-about-content"));

    document.getElementById("launch-button").innerHTML = "<i class=\"bi bi-rocket-takeoff\"></i> LAUNCH";
    document.getElementById("launch-button").onclick = function () {
        console.log('[CONNECTOR] library.start.'+activeInstance);
        document.getElementById("launch-button").innerText = "UPDATING..."
    }

    document.getElementById("library-tags").innerHTML = "";
    const tags = tagsString.split(", ");
    for(let i = 0; i < tags.length; i++) {
        let tag = tags[i];
        if(tag.startsWith("minecraft-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-black'>Minecraft " + tag.replaceAll("minecraft-", "") + "</span>";
        } else if(tag.startsWith("fabric-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-info text-black'>Fabric " + tag.replaceAll("fabric-", "") + "</span>";
        } else if(tag.startsWith("forge-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-info text-black'>Forge " + tag.replaceAll("forge-", "") + "</span>";
        } else if(tag.startsWith("neoforge-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-info text-black'>NeoForge " + tag.replaceAll("neoforge-", "") + "</span>";
        } else if(tag.startsWith("quilt-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-info text-black'>Quilt " + tag.replaceAll("quilt-", "") + "</span>";
        } else if(!tag.startsWith("modloader-")&&!tag.startsWith("modloder-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-white text-black'>"+tag+"</span>";
        }
    }

    document.getElementById("update-button").onclick = function () {
        console.log('[CONNECTOR] library.update.'+activeInstance);
    }
    document.getElementById("folder-button").onclick = function () {
        console.log('[CONNECTOR] library.folder.'+activeInstance);
    }
    document.getElementById("library-settings-button").onclick = function () {
        console.log('[CONNECTOR] library.settings.'+activeInstance);
    }
}

function openLinksInNewTab(containerElement) {
    if (!containerElement) {
        return;
    }

    const links = containerElement.querySelectorAll('a');
    links.forEach(link => {
        link.target = '_blank';
        link.rel = 'noopener noreferrer';
    });
}

function requestInstanceCreation() {
    const crname = encodeURIComponent(document.getElementById("creator–instance-name").value.replaceAll(".","#DOT%")).replaceAll("\\","").replaceAll("/","");
    if(crname) {
        const crtype = encodeURIComponent(document.getElementById("creator-mc-type").value.replaceAll(".","#DOT%"));
        const crversion = encodeURIComponent(document.getElementById("creator-mc-versions").value.replaceAll(".","#DOT%"));
        const crmlversion = encodeURIComponent(document.getElementById("creator-ml-versions").value.replaceAll(".","#DOT%"));
        console.log("[CONNECTOR] library.creator.create."+crversion+"."+crtype+"."+crmlversion+"."+crname);
    } else {
        if(document.getElementById('creator-name-warning').classList.contains('d-none')) {document.getElementById('creator-name-warning').classList.remove('d-none');}
    }
}

function showSettingsPane(pageName) {
    document.getElementById("settings-pane").classList.add('show');
    let page = "general";
    pageName = pageName.toLowerCase();
    if(pageName === "game") {
        page = "game";
    } else if(pageName === "java") {
        page = "java";
    } else if(pageName === "hook") {
        page = "hook";
    } else if(pageName === "delete") {
        page = "delete";
    }
    const settingsPane = document.getElementById("settings-pane").querySelector(".instance-settings");
    const menuPane = settingsPane.querySelector(".settings-menu");
    const contentPane = settingsPane.querySelector(".settings-content");
    contentPane.querySelector(".general-settings").classList.remove('show');
    menuPane.querySelector(".general-button").classList.remove('show');
    contentPane.querySelector(".game-settings").classList.remove('show');
    menuPane.querySelector(".game-button").classList.remove('show');
    contentPane.querySelector(".java-settings").classList.remove('show');
    menuPane.querySelector(".java-button").classList.remove('show');
    contentPane.querySelector(".hook-settings").classList.remove('show');
    menuPane.querySelector(".hook-button").classList.remove('show');
    contentPane.querySelector(".delete-settings").classList.remove('show');
    menuPane.querySelector(".delete-button").classList.remove('show');

    menuPane.querySelector("."+page+"-button").classList.add('show');
    contentPane.querySelector("."+page+"-settings").classList.add('show');
}

document.getElementById("settings-pane").addEventListener("click", function (event) {
    const pane = document.getElementById("settings-pane").querySelector(".instance-settings");
    if (pane && !pane.contains(event.target)) {
        document.getElementById("settings-pane").classList.remove("show");
    }
})