package com.zyneonstudios.nexus.application.frame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

/**
 * The {@code ZyneonSplash} class represents the splash screen displayed during the application's startup.
 * It shows a logo image while the application is initializing.
 */
public class ZyneonSplash extends JWindow {

    /**
     * Constructor for the ZyneonSplash.
     * Initializes the splash screen window with the logo image.
     */
    public ZyneonSplash() {
        super();
        try {
            // Set the background to transparent.
            setBackground(new Color(0, 0, 0, 0));

            // Set the size of the splash screen window.
            setSize(180, 180);

            // Center the splash screen on the screen.
            setLocationRelativeTo(null);

            // Load and scale the logo image.
            JLabel image;
            Image logo = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png")));
            Image scaledLogo = logo.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
            image = new JLabel(new ImageIcon(scaledLogo));

            // Add the image to the content pane.
            getContentPane().add(image);
        } catch (IOException e) {
            // Handle any IO exceptions that occur during image loading.
            throw new RuntimeException("Failed to load logo image: " + e.getMessage(), e);
        }
    }
}