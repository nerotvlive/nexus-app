package com.zyneonstudios.nexus.application.frame;

import com.zyneonstudios.nexus.application.listeners.AsyncConnectorListener;
import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.desktop.frame.nexus.NexusWebFrame;
import com.zyneonstudios.nexus.desktop.frame.web.NexusWebSetup;
import com.zyneonstudios.nexus.desktop.frame.web.WebFrame;
import com.zyneonstudios.nexus.utilities.strings.StringGenerator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The {@code AppFrame} class represents the main application window for the Nexus application.
 * It extends {@link NexusWebFrame} to provide a web browser-based user interface and implements
 * {@link ComponentListener} to handle window resize events and {@link WebFrame} for web-related functionalities.
 * This frame is responsible for initializing the UI, handling window events, managing menus,
 * and facilitating communication between the Java application and the web content.
 */
@SuppressWarnings("unused")
public class AppFrame extends NexusWebFrame implements ComponentListener, WebFrame {

    /** The minimum size of the application window. */
    private final Dimension minSize = new Dimension(1024, 640);

    /** A unique identifier for this window instance, used for distinguishing between multiple windows. */
    private final String windowId = StringGenerator.generateAlphanumericString(12);

    /** The SmartBar component, which may display context-sensitive information or controls. */
    private SmartBar smartBar;

    /** The "Actions" menu in the menu bar. */
    private final JMenu actions = new JMenu("Actions");

    /** The "Browser" menu in the menu bar. */
    private final JMenu browser = new JMenu("Browser");

    /** The developer menu bar containing debugging and advanced options. */
    private final JMenuBar menuBar = new JMenuBar();

    /** A flag indicating whether a custom frame (title bar) is being used instead of the native one. */
    private final boolean customFrame;

    /**
     * Constructs the main application frame.
     *
     * @param setup     The {@link NexusWebSetup} instance for configuring the web client.
     * @param url       The initial URL to load in the web browser component.
     * @param decorated A boolean indicating whether the window should have a native title bar and borders.
     */
    public AppFrame(NexusWebSetup setup, String url, boolean decorated) {
        super(setup.getWebClient(), url, decorated, NexusApplication.getInstance().getLocalSettings().useNativeWindow());
        this.customFrame = !NexusApplication.getInstance().getLocalSettings().useNativeWindow();

        initializeFrame(url, setup, decorated);
        setupMenus(url, setup, decorated);
        setupBrowserComponent();
    }

    /**
     * Initializes the frame's basic properties, such as the icon, border, and window listeners.
     *
     * @param url       The initial URL being loaded.
     * @param setup     The web setup configuration.
     * @param decorated Whether the frame uses native decoration.
     */
    private void initializeFrame(String url, NexusWebSetup setup, boolean decorated) {
        try {
            // Set the application icon, scaled to 32x32 pixels.
            setIconImage(ImageIO.read(Objects.requireNonNull(getClass().getResource("/icon.png"))).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            NexusApplication.getLogger().deb("Failed to load application icon: " + e.getMessage());
        }

        // Apply a custom border if not using the native window frame.
        if (customFrame) {
            getRootPane().setBorder(BorderFactory.createLineBorder(Color.decode("#454545"), 1, true));
        }
        
        // Add a window listener to handle closing events.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                handleWindowClosing();
            }
        });

        getMaximizeButton().setVisible(false);
        setMinimumSize(minSize);
        addComponentListener(this);
        setAsyncWebFrameConnectorEvent(new AsyncConnectorListener(this, null));
    }

    /**
     * Sets up the menu bar, including the "Actions" and "Browser" menus, and the developer bar.
     *
     * @param url       The initial URL.
     * @param setup     The web setup configuration.
     * @param decorated Whether the frame is decorated.
     */
    private void setupMenus(String url, NexusWebSetup setup, boolean decorated) {
        JPanel spacer = new JPanel();
        spacer.setBackground(null);
        menuBar.setBackground(Color.black);

        if (!NexusApplication.getInstance().getLocalSettings().useNativeWindow()) {
            try {
                Image myPicture = ImageIO.read(Objects.requireNonNull(getClass().getResource("/icon.png"))).getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                JLabel picLabel = new JLabel(new ImageIcon(myPicture));

                menuBar.add(spacer);
                menuBar.add(picLabel);
            } catch (Exception e) {
                NexusApplication.getLogger().deb("Failed to load application icon: " + e.getMessage());
            }
        }

        actions.getPopupMenu().setBackground(Color.black);
        browser.getPopupMenu().setBackground(Color.black);

        // "Actions" menu items
        JMenuItem refresh = new JMenuItem("Open start page");
        refresh.addActionListener(e -> getBrowser().loadURL(url));
        actions.add(refresh);

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> NexusApplication.stop(0));
        actions.add(exit);

        // "Browser" menu items
        JMenuItem goForward = new JMenuItem("Go forward");
        goForward.addActionListener(e -> getBrowser().goForward());
        browser.add(goForward);

        JMenuItem goBack = new JMenuItem("Go back");
        goBack.addActionListener(e -> getBrowser().goBack());
        browser.add(goBack);
        
        JMenuItem reload = new JMenuItem("Reload");
        reload.addActionListener(e -> getBrowser().reload());
        browser.add(reload);

        // Enable/disable navigation buttons based on browser state.
        browser.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                goBack.setEnabled(getBrowser().canGoBack());
                goForward.setEnabled(getBrowser().canGoForward());
            }

            @Override
            public void menuDeselected(MenuEvent e) {
                // Not needed.
            }

            @Override
            public void menuCanceled(MenuEvent e) {
                // Not needed.
            }
        });

        // Add debug menu items if debugging is enabled.
        if (NexusApplication.getLogger().isDebugging()) {
            setupDebugMenuItems(setup, decorated);
        }

        menuBar.add(browser);
        menuBar.add(actions);
        menuBar.setBorder(null);
        
        // Initialize and add the SmartBar.
        smartBar = new SmartBar();
        smartBar.setMargin(0, 3, 0, 6);
        smartBar.setMaximumSize(new Dimension(250, getSize().height));
        menuBar.add(smartBar);
        menuBar.setOpaque(true);

        // Attach the menu bar to the native frame or custom title bar.
        if (NexusApplication.getInstance().getLocalSettings().useNativeWindow()) {
            menuBar.setBorderPainted(false);
            setJMenuBar(menuBar);
        } else {
            JLabel title = getLabel();
            title.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            JButton minimize = getMinimizeButton();
            JButton close = getCloseButton();
            JPanel right = new JPanel(new BorderLayout());
            right.setBackground(null);
            right.setPreferredSize(menuBar.getPreferredSize());
            JPanel buttons = new JPanel(new BorderLayout());
            buttons.add(minimize, BorderLayout.WEST);
            buttons.add(close, BorderLayout.EAST);
            right.add(buttons, BorderLayout.EAST);
            getTitlebar().removeAll();
            getTitlebar().setLayout(new BorderLayout());
            getTitlebar().add(menuBar, BorderLayout.WEST);
            getTitlebar().add(title, BorderLayout.CENTER);
            getTitlebar().add(right, BorderLayout.EAST);
        }
    }

    /**
     * Sets up menu items that are only available in debug mode.
     *
     * @param setup     The web setup configuration.
     * @param decorated Whether the frame is decorated.
     */
    private void setupDebugMenuItems(NexusWebSetup setup, boolean decorated) {
        JMenuItem inputUrl = new JMenuItem("Input URL");
        inputUrl.addActionListener(e -> showUrlInputDialog());
        browser.add(inputUrl);

        JMenuItem openInBrowser = new JMenuItem("Open in default browser");
        openInBrowser.addActionListener(e -> openInDefaultBrowser());
        actions.add(openInBrowser);

        JMenuItem devTools = new JMenuItem("Show DevTools");
        devTools.addActionListener(e -> getBrowser().openDevTools());
        actions.add(devTools);

        JMenu frameMenu = new JMenu("Window actions");
        frameMenu.getPopupMenu().setBackground(Color.black);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JMenuItem resetWindowSize = new JMenuItem("Reset window size");
        resetWindowSize.addActionListener(e -> setSize((int)(screenSize.getWidth()/1.5), (int)(screenSize.getHeight()/1.5)));
        frameMenu.add(resetWindowSize);

        JMenuItem resetWindowLocation = new JMenuItem("Reset window location");
        resetWindowLocation.addActionListener(e -> setLocationRelativeTo(null));
        frameMenu.add(resetWindowLocation);

        AtomicInteger clones = new AtomicInteger(1);
        JMenuItem cloneWindow = new JMenuItem("Clone window");
        cloneWindow.addActionListener(e -> cloneWindow(setup, decorated, clones));
        frameMenu.add(cloneWindow);

        actions.add(frameMenu);

        JMenuItem disableDevtools = new JMenuItem("Disable dev mode");
        disableDevtools.addActionListener(e -> {
            NexusApplication.getLogger().disableDebug();
            setVisible(false);
            browser.remove(inputUrl);
            actions.remove(disableDevtools);
            actions.remove(cloneWindow);
            actions.remove(devTools);
            actions.remove(openInBrowser);
            setVisible(true);
            executeJavaScript("enableDevTools(false);");
        });
        actions.add(disableDevtools);
    }
    
    /**
     * Sets up a mouse listener for the browser's UI component to ensure it gains focus on click.
     */
    private void setupBrowserComponent() {
        getBrowser().getUIComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                getBrowser().getUIComponent().requestFocusInWindow();
            }
        });
    }

    /**
     * Handles the window closing event. The application will only exit if the main window is closed,
     * not a cloned window.
     */
    private void handleWindowClosing() {
        if (!getTitle().contains("-clone ")) {
            NexusApplication.stop(0);
        }
    }
    
    /**
     * Shows a dialog that allows the user to input a URL to be loaded in the browser.
     */
    private void showUrlInputDialog() {
        JDialog inputWindow = new JDialog(AppFrame.this, "Input url (" + windowId + ", " + getTitle() + ")", true);
        inputWindow.setLocationRelativeTo(this);
        inputWindow.setResizable(false);
        inputWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        inputWindow.setSize(500, 75);
        JTextField urlField = new JTextField(getBrowser().getURL());
        urlField.addActionListener(e1 -> {
            getBrowser().loadURL(urlField.getText());
            inputWindow.dispose();
        });
        inputWindow.add(urlField);
        inputWindow.setVisible(true);
    }

    /**
     * Opens the current URL of the browser component in the user's default system browser.
     */
    private void openInDefaultBrowser() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(getBrowser().getURL()));
            }
        } catch (Exception e) {
            NexusApplication.getLogger().deb("Could not open URL in default browser: " + e.getMessage());
        }
    }

    /**
     * Creates a new {@code AppFrame} instance that is a clone of the current window.
     *
     * @param setup     The web setup configuration for the new window.
     * @param decorated Whether the cloned window should be decorated.
     * @param clones    An atomic counter to number the cloned windows.
     */
    private void cloneWindow(NexusWebSetup setup, boolean decorated, AtomicInteger clones) {
        AppFrame clone = new AppFrame(setup, getBrowser().getURL(), decorated);
        clone.setTitleColors(Color.decode("#333399"), Color.decode("#ffffff"));
        clone.setVisible(true);
        clone.setTitlebar(windowId + "-clone " + clones.getAndIncrement(), Color.decode("#333399"), Color.white);
        clone.setSize(getSize());
        clone.setLocationRelativeTo(null);
        clone.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    /**
     * Returns the minimum size of the application window.
     *
     * @return The minimum {@link Dimension} of the window.
     */
    public Dimension getMinSize() {
        return minSize;
    }

    /**
     * Sets the title text and colors for the custom title bar.
     *
     * @param title      The title text to display.
     * @param background The background color of the title bar.
     * @param foreground The foreground color of the title bar text.
     */
    public void setTitlebar(String title, Color background, Color foreground) {
        setTitle("NEXUS App (" + title + ")");
        setTitleBackground(background);
        setTitleForeground(foreground);
    }

    /**
     * Sets the colors for the custom title bar.
     *
     * @param background The background color of the title bar.
     * @param foreground The foreground color of the title bar text.
     */
    public void setTitleColors(Color background, Color foreground) {
        setTitleBackground(background);
        setTitleForeground(foreground);
    }

    /**
     * Sets the background color of the title bar and related components.
     *
     * @param color The background color to apply.
     */
    public void setTitleBackground(Color color) {
        getRootPane().putClientProperty("JRootPane.titleBarBackground", color);
        if (customFrame) {
            setCustomTitleBackground(color);
        }
        setBackground(color);
        menuBar.setBackground(color);
        actions.getPopupMenu().setBackground(color);
        browser.getPopupMenu().setBackground(color);
        smartBar.setSpaceColor(color);
    }

    /**
     * Sets the background color for the components of the custom title bar.
     *
     * @param color The color to set as the background.
     */
    private void setCustomTitleBackground(Color color) {
        try {
            getTitlebar().setBackground(color);
            getCloseButton().setBackground(color);
            getMinimizeButton().setBackground(color);
            getMinimizeButton().getParent().setBackground(color);
            getMaximizeButton().setBackground(color);
            getLabel().setBackground(color);
        } catch (Exception e) {
            NexusApplication.getLogger().err(e.getMessage());
        }
    }

    /**
     * Sets the foreground color of the title bar text and related components.
     *
     * @param color The foreground color to apply.
     */
    public void setTitleForeground(Color color) {
        getRootPane().putClientProperty("JRootPane.titleBarForeground", color);
        if (customFrame) {
            setCustomTitleForeground(color);
        }
        menuBar.setForeground(color);
        actions.setForeground(color);
        browser.setForeground(color);
        updateMenuColors(actions, color);
        updateMenuColors(browser, color);
    }

    /**
     * Recursively updates the foreground color of a menu and all its subcomponents.
     *
     * @param menu  The {@link JMenu} to update.
     * @param color The color to apply to the foreground.
     */
    private void updateMenuColors(JMenu menu, Color color) {
        menu.getPopupMenu().setForeground(color);
        for (Component c : menu.getPopupMenu().getComponents()) {
            c.setForeground(color);
        }
    }

    /**
     * Sets the foreground color for the components of the custom title bar.
     *
     * @param color The color to set as the foreground.
     */
    private void setCustomTitleForeground(Color color) {
        try {
            getTitlebar().setForeground(color);
            getCloseButton().setForeground(color);
            getMinimizeButton().setForeground(color);
            getMaximizeButton().setForeground(color);
            getLabel().setForeground(color);
        } catch (Exception e) {
            NexusApplication.getLogger().err(e.getMessage());
        }
    }

    /**
     * Executes one or more JavaScript snippets in the context of the current web page.
     *
     * @param script The JavaScript code to execute.
     */
    @Override
    public void executeJavaScript(String... script) {
        super.executeJavaScript(script);
    }

    /**
     * Returns this frame instance cast as a {@link JFrame}.
     *
     * @return This object as a {@code JFrame}.
     */
    @Override
    public JFrame getAsJFrame() {
        return this;
    }

    /**
     * Invoked when the component's size changes.
     * This method is intentionally not implemented as no action is needed on resize.
     * @param e The {@code ComponentEvent}.
     */
    @Override
    public void componentResized(ComponentEvent e) {
        // Not implemented.
    }

    /**
     * Invoked when the component's position changes.
     * This method is intentionally not implemented as no action is needed on move.
     * @param e The {@code ComponentEvent}.
     */
    @Override
    public void componentMoved(ComponentEvent e) {
        // Not implemented.
    }

    /**
     * Invoked when the component has been made visible.
     * This method is intentionally not implemented as no action is needed on show.
     * @param e The {@code ComponentEvent}.
     */
    @Override
    public void componentShown(ComponentEvent e) {
        // Not implemented.
    }

    /**
     * Invoked when the component has been made invisible.
     * This method is intentionally not implemented as no action is needed on hide.
     * @param e The {@code ComponentEvent}.
     */
    @Override
    public void componentHidden(ComponentEvent e) {
        // Not implemented.
    }

    /**
     * Returns the {@link SmartBar} associated with this frame.
     *
     * @return The {@code SmartBar} instance.
     */
    public SmartBar getSmartBar() {
        return smartBar;
    }

    /**
     * Sets the {@link SmartBar} for this frame.
     *
     * @param smartBar The {@code SmartBar} to associate with this frame.
     */
    public void setSmartBar(SmartBar smartBar) {
        this.smartBar = smartBar;
    }
}
