document.querySelector(".menu-panel").querySelector(".card-body").innerHTML = "<span class='text nexus-version'>"+version+"</span><i onclick='console.log(`[CONNECTOR] exit`)' class='bi bi-door-open'></i>";

/**
 * Initializes the appearance settings values based on stored preferences.
 */
function initAppearanceValues() {
    // Set the landing page dropdown value
    document.querySelector(".appearance-landing").value = landing;

    // Set the theme dropdown value
    document.querySelector(".appearance-theme").value = theme;

    // Set the animations checkbox state
    document.querySelector(".appearance-animations").checked = animations;

    // Set the render effects checkbox state
    document.querySelector(".appearance-renderEffects").checked = renderEffects;

    // Set the custom accent color checkbox state
    document.querySelector(".appearance-customAccentColor").checked = enableCustomAccentColor;

    // Set the accent color input value
    document.querySelector(".appearance-accentColor").value = accentColor;

    // Set the border radius display and range values
    document.querySelector(".appearance-borderRadiusDisplay").value = borderRadius;
    document.querySelector(".appearance-borderRadius").value = borderRadius;

    // Set the panel floating checkbox state
    document.querySelector(".appearance-panelFloating").checked = panelFloating;

    // Set the panel inlined checkbox state
    document.querySelector(".appearance-panelInlined").checked = panelInlined;

    // Set the checkbox states for button positions
    document.querySelector(".appearance-downloadsToPanel").checked = !downloadsInMenu;
    document.querySelector(".appearance-settingsToPanel").checked = !settingsInMenu;

    // Show/hide custom color input and predefined color buttons based on custom accent color setting
    const colorInput = document.querySelector(".color-input");
    const colors = document.querySelector(".colors");
    if (enableCustomAccentColor) {
        colorInput.style.display = "flex";
        colors.style.display = "none";
    } else {
        colorInput.style.display = "none";
        colors.style.display = "flex";
    }

    // Show/hide the reset accent color button based on the current accent color
    const resetAccentColorButton = colorInput.querySelector("i");
    if (accentColor.toLowerCase() !== "#8732ec") {
        resetAccentColorButton.style.display = "flex";
    } else {
        resetAccentColorButton.style.display = "none";
    }
    console.log("[CONNECTOR] initAppearanceValues");
} initAppearanceValues();

function initSettings() {
    showSettingsCategory("appearance-settings")
    const urlParams = new URLSearchParams(window.location.search);
    if(urlParams.has("st")) {
        document.getElementById(urlParams.get("st")+"-button").click();
    }

    console.log("[CONNECTOR] settings.init");
}

function showSettingsCategory(category) {
    const settings = document.getElementById(category);
    const button = document.getElementById(category+"-button");
    if(settings && button) {
        if(settingsCategory) {
            const oldSettings = document.getElementById(settingsCategory);
            const oldButton = document.getElementById(settingsCategory+"-button");
            if(oldSettings.classList.contains("active")) {
                oldSettings.classList.remove("active");
            }
            if(oldButton.classList.contains("active")) {
                oldButton.classList.remove("active");
            }
        }
        if(!settings.classList.contains("active")) {
            settings.classList.add("active");
        }
        if(!button.classList.contains("active")) {
            button.classList.add("active");
        }
        settingsCategory = category;
    }
}

/**
 * Selects the default landing page for the application.
 * @param {string} landingPage - The file path of the default landing page.
 */
function setLanding(landingPage) {
    landing = landingPage;
    setStorageItem("settings.appearance.landingPage", landing);
}

/**
 * Toggles animations on/off and updates the stored preference.
 * @param {boolean} bool - True to enable animations, false to disable.
 */
function setAnimations(bool) {
    animations = bool;
    setStorageItem("settings.appearance.animations", bool);
    document.querySelector(".appearance-animations").checked = animations;

    // Add or remove the CSS rule to disable transitions
    const transitionDisableStyle = "<style>* { transition: all 0s !important; animation: none !important; }</style>";
    if (!animations) {
        if (!document.head.innerHTML.includes(transitionDisableStyle)) {
            document.head.innerHTML += transitionDisableStyle;
        }
    } else {
        document.head.innerHTML = document.head.innerHTML.replaceAll(transitionDisableStyle, "");
    }
}

/**
 * Toggles render effects (glass effect, shadows) on/off and updates the stored preference.
 * @param {boolean} bool - True to enable render effects, false to disable.
 */
function setRenderEffects(bool) {
    renderEffects = bool;
    setStorageItem("settings.appearance.renderEffects", bool);
    document.querySelector(".appearance-renderEffects").checked = renderEffects;

    const panel = document.querySelector('.menu-panel');
    const renderEffectsDisableStyle = "<style>:root,[data-bs-theme='light'],[data-bs-theme='dark'] * { box-shadow: none !important; text-shadow: none !important; --bs-box-shadow: none !important; }</style>";

    if (!renderEffects) {
        // Remove glass effect and add CSS to disable shadows
        if (panel.classList.contains("glass")) {
            panel.classList.remove("glass");
        }
        if (!document.head.innerHTML.includes(renderEffectsDisableStyle)) {
            document.head.innerHTML += renderEffectsDisableStyle;
        }
    } else {
        // Add glass effect and remove CSS to disable shadows
        if (!panel.classList.contains("glass")) {
            panel.classList.add("glass");
        }
        document.head.innerHTML = document.head.innerHTML.replaceAll(renderEffectsDisableStyle, "");
    }
}

/**
 * Toggles custom accent color on/off and updates the stored preference.
 * @param {boolean} bool - True to enable custom accent color, false to use predefined colors.
 */
function setCustomAccentColor(bool) {
    enableCustomAccentColor = bool;
    setStorageItem("settings.appearance.customAccentColor", bool);
    document.querySelector(".appearance-customAccentColor").checked = enableCustomAccentColor;

    const colorInput = document.querySelector(".color-input");
    const colors = document.querySelector(".colors");
    if (bool) {
        // Show custom color input, hide predefined colors, and load stored custom color
        colorInput.style.display = "flex";
        colors.style.display = "none";
        if (getStorageItem("settings.appearance.accentColor")) {
            accentColor = getStorageItem("settings.appearance.accentColor");
        }
        setAccentColor(accentColor);
    } else {
        // Hide custom color input, show predefined colors, and load stored predefined color or default
        colorInput.style.display = "none";
        colors.style.display = "flex";
        if (getStorageItem("settings.appearance.color")) {
            accentColor = getStorageItem("settings.appearance.color");
        } else {
            accentColor = root.style.getPropertyValue("--nex-primary");
        }
        setAccentColor(accentColor);
    }
    document.querySelector(".appearance-accentColor").value = accentColor;
}

/**
 * Sets the accent color and updates the stored preference.
 * @param {string} color - The new accent color (hex or CSS variable).
 */
function setAccentColor(color) {
    accentColor = color;
    if (enableCustomAccentColor) {
        setStorageItem("settings.appearance.accentColor", color);
    } else {
        setStorageItem("settings.appearance.color", color);
    }
    document.querySelector(".appearance-accentColor").value = accentColor;
    setAccentColor_dev(accentColor);

    // Show/hide the reset accent color button based on the current accent color
    if (enableCustomAccentColor) {
        const resetAccentColorButton = document.querySelector(".color-input").querySelector("i");
        if (accentColor.toLowerCase() !== "#8732ec") {
            resetAccentColorButton.style.display = "flex";
        } else {
            resetAccentColorButton.style.display = "none";
        }
    }
}

/**
 * Sets the border radius and updates the stored preference.
 * @param {number} number - The new border radius value.
 */
function setBorderRadius(number) {
    // Clamp the border radius value between 0 and 2
    number = Math.max(0, Math.min(2, number));

    borderRadius = number;
    setStorageItem("settings.appearance.borderRadius", number);
    document.querySelector(".appearance-borderRadiusDisplay").value = borderRadius;
    document.querySelector(".appearance-borderRadius").value = borderRadius;
    setBorderRadius_dev(number);
}

/**
 * Toggles the panel floating state and updates the stored preference.
 * @param {boolean} bool - True to enable panel floating, false to disable.
 */
function setPanelFloating(bool) {
    panelFloating = bool;
    setStorageItem("settings.appearance.panelFloating", bool);
    document.querySelector(".appearance-panelFloating").checked = panelFloating;

    const panel = document.querySelector(".menu-panel");
    if (panelFloating) {
        // Add floating class
        if (!panel.classList.contains("floating")) {
            panel.classList.add("floating");
        }
    } else {
        // Remove floating class if not inlined
        if (!panelInlined) {
            if (panel.classList.contains("floating")) {
                panel.classList.remove("floating");
            }
        }
    }
}

/**
 * Toggles the panel inlined state and updates the stored preference.
 * @param {boolean} bool - True to enable panel inlined, false to disable.
 */
function setPanelInlined(bool) {
    panelInlined = bool;
    setStorageItem("settings.appearance.panelInlined", bool);
    document.querySelector(".appearance-panelInlined").checked = panelInlined;

    const panel = document.querySelector(".menu-panel");
    if (panelInlined) {
        // Set panel mode to hover, add inlined and floating classes, enable menu and panel
        panelMode = "hover";
        changePanelMode();
        if (!panel.classList.contains("inlined")) {
            panel.classList.add("inlined");
        }
        if (!panel.classList.contains("floating")) {
            panel.classList.add("floating");
        }
        enableMenu(true);
        enablePanel(true);
    } else {
        // Remove inlined class, re-initialize panel, and remove floating class if not floating
        if (panel.classList.contains("inlined")) {
            panel.classList.remove("inlined");
            initPanel();
        }
        if (panel.classList.contains("floating")) {
            if (!panelFloating) {
                panel.classList.remove("floating");
            }
        }
    }
}

/**
 * Toggles if the downloads button will be displayed in the side-menu or the panel.
 * @param {boolean} bool - True to move the button to the panel.
 */
function setDownloadsToPanel(bool) {
    downloadsInMenu = !bool;
    setStorageItem("settings.appearance.downloadsInPanel", downloadsInMenu);
    updateButtonVisibility();
}

/**
 * Toggles if the settings button will be displayed in the side-menu or the panel.
 * @param {boolean} bool - True to move the button to the panel.
 */
function setSettingsToPanel(bool) {
    settingsInMenu = !bool;
    setStorageItem("settings.appearance.settingsInPanel", settingsInMenu);
    updateButtonVisibility();
}

function addAccountToAccountList(name, uuid) {
    if(!document.getElementById(uuid)) {
        const template = document.querySelector(".account-template");
        const account = template.cloneNode(true);
        account.id = uuid;
        account.classList.remove("account-template");
        account.style.display = "";
        account.querySelector(".account-templateName").innerText = name;
        template.parentNode.insertBefore(account, template);
    }
}

function copyUUID(element) {
    navigator.clipboard.writeText(element.innerText);
    const popover = bootstrap.Popover.getOrCreateInstance(element);
    popover.show();
    setTimeout(() => {
        popover.hide();
    }, 2000);
}

function setDefaultMemory(mem) {
    document.body.querySelector('.instance-JavaMemoryDisplay').value = mem;
    document.body.querySelector('.instance-JavaMemory').value = mem;
    console.log("[CONNECTOR] settings.set.defaultMemory." + mem);
}

try {
    const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    const popoverList = [...popoverTriggerList].map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl));
} catch (ignore) {}