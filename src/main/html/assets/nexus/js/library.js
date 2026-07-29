function addInstance(id,name,icon,group) {
    id = decodeURIComponent(id);
    name = decodeURIComponent(name);
    icon = decodeURIComponent(icon);
    group = decodeURIComponent(group);

    if(instanceGrouping === false) {
        group = null;
    }

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
                addInstanceToOverview(id,name,icon,group)
            }
        }
    }
}

function addInstanceToOverview(id,name,icon,group) {
    if(instanceGrouping === false) {
        group = null;
    }

    const elementId = id+"-ov"
    if(!document.getElementById(elementId)) {
        const template = document.getElementById("overview-template-row");
        if(template&&template.parentElement) {
            let list = template.parentElement;
            const row = template.cloneNode(true);
            const button = row.querySelector('.instance-show');
            button.onclick = function () {
                console.log("[CONNECTOR] library.showInstance."+id);
            }
            const button2 = row.querySelector('.instance-launch');
            button2.onclick = function () {
                console.log("[CONNECTOR] library.start."+id);
            }
            row.id = elementId;
            row.classList.remove("d-none");
            if(icon) {
                row.querySelector(".instance-icon").src = icon;
                row.querySelector(".instance-icon").classList.remove("d-none");
            }
            row.querySelector(".instance-name").innerText = name;
            list.insertBefore(row, template);
        }
    }
}

function addInstanceGroup(id,name,colorName) {
    if (!document.getElementById(id)) {
        let list = document.getElementById("instance-list");
        const template = list.querySelector(".instance-group-template");
        if (template && id && name) {
            const group = template.cloneNode(true);
            group.id = id;
            if (instanceGrouping === true) {
                group.classList.remove("d-none");
            } else {
                group.style.display = "none";
            }
            group.classList.remove("instance-group-template");
            group.querySelector(".collapse").id = id + "-collapse";
            group.querySelector("a").id = id + "-collapse-button";
            group.querySelector("a").onclick = function () {
                toggleSubMenuGroup(id + "-collapse")
            };
            group.querySelector("span.group-title").innerText = name;
            group.querySelector("span.group-title").onclick = function () {
                toggleSubMenuGroup(id + "-collapse")
            };
            if (colorName) {
                group.classList.add(colorName);
            }
            template.parentElement.insertBefore(group, template);
        }
        const id_ = id + "-collapse";
        if (localStorage.getItem("submenu-group_" + id_)) {
            if (localStorage.getItem("submenu-group_" + id_) === "enable") {
                enableSubMenuGroup(id_);
            }
        }
    } else {
        document.getElementById(id).classList.remove("d-none");
    }
}

function initLibrary() {
    console.log("[CONNECTOR] library.init");
    initArrayBoxes();

    const toggle = document.querySelector(".instanceGroupingToggle");
    if(instanceGrouping === true) {
        toggle.classList.remove("bi-list-task");
        toggle.classList.add("bi-view-list");
    } else {
        toggle.classList.remove("bi-view-list");
        toggle.classList.add("bi-list-task");
    }

    sleep(10).then(() => {
        document.getElementById("library").classList.add("active");
    })

}

function showOverview() {
    document.getElementById("instance-view").style.display = "none";
    document.getElementById("overview").style.display = "flex";
    if(activeInstance) {
        if(document.getElementById(activeInstance)) {
            document.getElementById(activeInstance).classList.remove("active");
        }
    }
    activeInstance = null;
    document.getElementById("overview-button").classList.add("active");
    document.getElementById("library-title").querySelector("span").innerText = "Library Overview";
    document.getElementById("instance-icon").src = "";
    document.getElementById("folder-button").classList.add("d-none");
    document.getElementById("library-settings-button").classList.add("d-none");
}

function showInstance(id,name,version,summary,description,tagsString,bg) {
    if(bg) {
        document.documentElement.style.setProperty('--instance-background', "url('"+bg+"');");
    }
    document.getElementById("instance-icon").classList.add("d-none");
    document.getElementById("folder-button").classList.add("d-none");
    document.getElementById("library-settings-button").classList.add("d-none");
    document.getElementById("update-button").classList.add("d-none");

    document.getElementById("library-title").querySelector("span").classList.remove("icon");
    document.getElementById("instance-icon").src = "";
    id = decodeURIComponent(id);
    name = decodeURIComponent(name);
    activeInstanceName = name;
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
    document.getElementById("overview-button").classList.remove("active");
    document.getElementById("library-title").querySelector("span").innerText = name;

        if(document.getElementById(id)&&document.getElementById(id).querySelector("img")&&document.getElementById(id).querySelector("img").src) {
            document.getElementById("instance-icon").src = document.getElementById(id).querySelector("img").src;
            document.getElementById("instance-icon").classList.remove("d-none");
            document.getElementById("library-title").querySelector("span").classList.add("icon");
        }

        document.getElementById("overview").style.display = "none";
    document.getElementById("instance-view").style.display = "flex";
    document.getElementById("launch-button").style.display = "";
    document.getElementById("instance-name").innerText = name;
    document.getElementById("instance-version").innerText = version;
    document.getElementById("instance-summary").innerText = summary;
    document.getElementById("instance-about").innerHTML = marked.parse(description);
    openLinksInNewTab(document.getElementById("instance-about"));

    document.getElementById("launch-button").innerHTML = "<i class=\"bi bi-play-fill\"></i> RUN";
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
        } else if(tag.startsWith("modrinth")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-modrinth'>Modrinth</span>";
        } else if(tag.startsWith("curseforge")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-curseforge'>CurseForge</span>";
        } else if(tag.startsWith("essential+")||tag.startsWith("essentialplus")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-white text-black'>Essential+</span>";
        } else if(tag.startsWith("fabric-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-info text-black'>Fabric " + tag.replaceAll("fabric-", "") + "</span>";
        } else if(tag.startsWith("forge-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-info text-black'>Forge " + tag.replaceAll("forge-", "") + "</span>";
        } else if(tag.startsWith("neoforge-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-info text-black'>NeoForge " + tag.replaceAll("neoforge-", "") + "</span>";
        } else if(tag.startsWith("quilt-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-info text-black'>Quilt " + tag.replaceAll("quilt-", "") + "</span>";
        } else if(!tag.startsWith("modloader-")&&!tag.startsWith("modloder-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-white text-black'>"+capitalizeFirstLetter(tag)+"</span>";
        }
    }

    document.getElementById("update-button").onclick = function () {
        console.log('[CONNECTOR] library.update.'+activeInstance);
    }
    document.getElementById("folder-button").classList.remove("d-none");
    document.getElementById("folder-button").onclick = function () {
        console.log('[CONNECTOR] library.folder.'+activeInstance);
    }
    document.getElementById("library-settings-button").classList.remove("d-none");
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
    const settingsPane = document.getElementById("settings-pane").querySelector(".instance-settings");
    const menuPane = settingsPane.querySelector(".settings-menu");
    const contentPane = settingsPane.querySelector(".settings-content");

    contentPane.querySelector(".settings-title").innerText = pageName;
    contentPane.querySelector(".general-settings").classList.remove('show');
    menuPane.querySelector(".general-button").classList.remove('show');
    contentPane.querySelector(".game-settings").classList.remove('show');
    menuPane.querySelector(".game-button").classList.remove('show');
    contentPane.querySelector(".java-settings").classList.remove('show');
    menuPane.querySelector(".java-button").classList.remove('show');
    contentPane.querySelector(".hook-settings").classList.remove('show');
    menuPane.querySelector(".hook-button").classList.remove('show');
    contentPane.querySelector(".deletion-settings").classList.remove('show');
    menuPane.querySelector(".deletion-button").classList.remove('show');

    menuPane.querySelector("."+pageName.toLowerCase()+"-button").classList.add('show');
    contentPane.querySelector("."+pageName.toLowerCase()+"-settings").classList.add('show');
}

document.getElementById("settings-pane").addEventListener("click", function (event) {
    const pane = document.getElementById("settings-pane").querySelector(".instance-settings");
    if (pane && !pane.contains(event.target)) {
        document.getElementById("settings-pane").classList.remove("show");
    }
});

function toggleInstanceGrouping() {
    if(instanceGrouping === true) {
        enableInstanceGrouping(false);
    } else {
        enableInstanceGrouping(true);
    }
}

function enableInstanceGrouping(bool) {
    if(bool === true||bool === false) {
        instanceGrouping = bool;
        setStorageItem("settings.instanceGrouping", bool);
        location.reload();
        const toggle = document.querySelector(".instanceGroupingToggle");
        if(bool === true) {
            toggle.classList.remove("bi-list-task");
            toggle.classList.add("bi-view-list");
        } else {
            toggle.classList.remove("bi-view-list");
            toggle.classList.add("bi-list-task");
        }
    }
}

function filterTable(tableId) {
    const table = document.getElementById(tableId);
    const input = table.querySelector("input.search");
    const filter = input.value.toUpperCase();
    const tr = table.getElementsByTagName("tr");

    for (let i = 1; i < tr.length; i++) {
        let visible = false;
        const td = tr[i].getElementsByTagName("td");

        for (let j = 1; j < td.length; j++) {
            if (td[j] && td[j].innerText.toUpperCase().indexOf(filter) > -1) {
                visible = true;
                break;
            }
        }

        tr[i].style.display = visible ? "" : "none";
    }
}