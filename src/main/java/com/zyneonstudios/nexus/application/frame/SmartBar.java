package com.zyneonstudios.nexus.application.frame;

import com.zyneonstudios.nexus.application.main.NexusApplication;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.CompletableFuture;

public class SmartBar extends JPanel {

    private final JPanel root = new JPanel(new BorderLayout());
    private final JPanel marginTop = new JPanel();
    private final JPanel marginRight = new JPanel();
    private final JPanel marginBottom = new JPanel();
    private final JPanel marginLeft = new JPanel();

    private Color backgroundColor = Color.decode("#1f1f1f");
    private Color spaceColor = null;
    private Color color = Color.lightGray;
    private Color borderColor = Color.decode("#292929");
    private Color errorColor = Color.decode("#e63c30");
    private Color successColor = Color.decode("#34bf49");
    private Color feedbackColor = Color.decode("#96e8ff");
    private Color placeholderColor = Color.darkGray;
    private String placeholder = "▶ Input command...";

    private final JPanel bar = new JPanel(new BorderLayout());
    private JPanel smartBarLeft = new JPanel();
    private JPanel smartBarRight = new JPanel();
    private JTextField smartBarInput = new JTextField();

    private JPanel spacerTop = new JPanel();
    private JPanel spacerBottom = new JPanel();

    public SmartBar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(247, 30));

        marginTop.setBackground(null);
        root.add(marginTop, BorderLayout.NORTH);
        marginRight.setBackground(null);
        root.add(marginRight, BorderLayout.EAST);
        marginBottom.setBackground(null);
        root.add(marginBottom, BorderLayout.SOUTH);
        marginLeft.setBackground(null);
        root.add(marginLeft, BorderLayout.WEST);

        spacerTop.setPreferredSize(new Dimension(247, 3));
        root.add(spacerTop, BorderLayout.NORTH);

        spacerBottom.setPreferredSize(new Dimension(247, 3));
        root.add(spacerBottom, BorderLayout.SOUTH);


        smartBarLeft.setPreferredSize(new Dimension(3,30));
        bar.add(smartBarLeft, BorderLayout.WEST);

        smartBarRight.setPreferredSize(new Dimension(3,30));
        bar.add(smartBarRight, BorderLayout.EAST);

        smartBarInput.setBorder(null);
        smartBarInput.setText(placeholder);
        smartBarInput.setForeground(placeholderColor);
        smartBarInput.setPreferredSize(new Dimension(241, 24));
        smartBarInput.addActionListener(e -> {
            if(NexusApplication.getInstance().getConsoleHandler().runCommand(smartBarInput.getText()+" ")) {
                smartBarInput.setForeground(successColor);
            } else {
                smartBarInput.setForeground(errorColor);
            }
        });
        smartBarInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if(e.getKeyCode() != KeyEvent.VK_ENTER) {
                    smartBarInput.setForeground(color);
                    validateValue();
                }
            }
        });
        smartBarInput.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SwingUtilities.invokeLater(() -> {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                    smartBarInput.requestFocusInWindow();
                });
            }
        });
        smartBarInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if(smartBarInput.getText().equals(placeholder)) {
                    smartBarInput.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(smartBarInput.getText().isEmpty()||smartBarInput.getText().isBlank()) {
                    smartBarInput.setText(placeholder);
                    smartBarInput.setForeground(placeholderColor);
                }
            }
        });
        bar.add(smartBarInput, BorderLayout.CENTER);
        bar.setBorder(new RoundedBorder(2,borderColor));
        root.add(bar, BorderLayout.CENTER);

        add(root, BorderLayout.CENTER);

        setColor(color);
        setBackgroundColor(backgroundColor);
    }

    public Color getPlaceholderColor() {
        return placeholderColor;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public Color getErrorColor() {
        return errorColor;
    }

    public Color getFeedbackColor() {
        return feedbackColor;
    }

    public Color getSuccessColor() {
        return successColor;
    }

    public void setErrorColor(Color errorColor) {
        if(smartBarInput.getForeground().equals(this.errorColor)) {
            smartBarInput.setForeground(errorColor);
        }
        this.errorColor = errorColor;
    }

    public void setFeedbackColor(Color feedbackColor) {
        if(smartBarInput.getForeground().equals(this.feedbackColor)) {
            smartBarInput.setForeground(feedbackColor);
        }
        this.feedbackColor = feedbackColor;
    }

    public void setSuccessColor(Color successColor) {
        if(smartBarInput.getForeground().equals(this.successColor)) {
            smartBarInput.setForeground(successColor);
        }
        this.successColor = successColor;
    }

    public void setPlaceholderColor(Color placeholderColor) {
        this.placeholderColor = placeholderColor;
        if(smartBarInput.getText().equals(placeholder)) {
            smartBarInput.setForeground(this.placeholderColor);
        }
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        super.setBackground(spaceColor);
        smartBarInput.setBackground(null);
        spacerTop.setBackground(spaceColor);
        smartBarRight.setBackground(null);
        spacerBottom.setBackground(spaceColor);
        smartBarLeft.setBackground(null);
        bar.setBackground(this.backgroundColor);
        root.setBackground(spaceColor);
    }

    public void setSpaceColor(Color spaceColor) {
        this.spaceColor = spaceColor;
        setBackgroundColor(backgroundColor);
    }

    public Color getSpaceColor() {
        return spaceColor;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        this.smartBarInput.setForeground(this.color);
    }

    /**
     * DEPRECATED: Use getColor() instead!
     *
     * @return The foreground color of the Smartbar.
     */
    @Override @Deprecated
    public Color getForeground() {
        return color;
    }

    /**
     * DEPRECATED: Use setColor() instead!
     * This method tries to utilize the new setColor() method and falls back automatically if that fails.
     *
     * @param foregroundColor The foreground color to set.
     **/
    @Override @Deprecated
    public void setForeground(Color foregroundColor) {
        try {
            setColor(foregroundColor);
        } catch (Exception e) {
            super.setForeground(foregroundColor);
        }
    }

    /**
     * DEPRECATED: Use setBackgroundColor() instead!
     * This method tries to utilize the new setBackgroundColor() method and falls back automatically if that fails.
     *
     * @param backgroundColor The background color to set.
     **/
    @Override @Deprecated
    public void setBackground(Color backgroundColor) {
        try {
            setBackgroundColor(backgroundColor);
        } catch (Exception e) {
            super.setBackground(backgroundColor);
        }
    }

    /**
     * DEPRECATED: Use getBackgroundColor() instead!
     *
     * @return The background color of the Smartbar.
     */
    @Override @Deprecated
    public Color getBackground() {
        return backgroundColor;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        if(smartBarInput.getText().equals(placeholder)) {
            smartBarInput.setText("");
        }
        this.placeholder = placeholder;
        if(smartBarInput.getText().isEmpty()) {
            smartBarInput.setText(this.placeholder);
        }
    }

    public JPanel getBar() {
        return bar;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public JPanel getSpacerBottom() {
        return spacerBottom;
    }

    public JPanel getSmartBarLeft() {
        return smartBarLeft;
    }

    public JPanel getSmartBarRight() {
        return smartBarRight;
    }

    public JPanel getSpacerTop() {
        return spacerTop;
    }

    public JTextField getSmartBarInput() {
        return smartBarInput;
    }

    public String getValue() {
        return smartBarInput.getText();
    }

    @Deprecated
    public String getText() {
        return getValue();
    }

    public void setValue(String value) {
        smartBarInput.setText(value);
    }

    @Deprecated
    public void setText(String text) {
        setValue(text);
    }

    public void setSmartBarInput(JTextField smartBarInput) {
        bar.remove(this.smartBarInput);
        this.smartBarInput = smartBarInput;
        bar.add(this.smartBarInput, BorderLayout.CENTER);
    }

    public void setSmartBarLeft(JPanel smartBarLeft) {
        bar.remove(this.smartBarLeft);
        this.smartBarLeft = smartBarLeft;
        bar.add(this.smartBarLeft, BorderLayout.WEST);
    }

    public void setSmartBarRight(JPanel smartBarRight) {
        bar.remove(this.smartBarRight);
        this.smartBarRight = smartBarRight;
        bar.add(this.smartBarRight, BorderLayout.EAST);
    }

    public void setSpacerTop(JPanel spacerTop) {
        root.remove(this.spacerTop);
        this.spacerTop = spacerTop;
        root.add(this.spacerTop, BorderLayout.NORTH);
    }

    public void setSpacerBottom(JPanel spacerBottom) {
        root.remove(this.spacerBottom);
        this.spacerBottom = spacerBottom;
        root.add(this.spacerBottom, BorderLayout.SOUTH);
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        bar.setBorder(new RoundedBorder(2,this.borderColor));
    }

    private void validateValue() {
        CompletableFuture.runAsync(()->{
            try {
                Thread.sleep(10);
                if (!smartBarInput.getText().isEmpty()) {
                    if (NexusApplication.getInstance().getConsoleHandler().hasCommand(smartBarInput.getText().split(" ", 2)[0])) {
                        smartBarInput.setForeground(feedbackColor);
                    } else if(smartBarInput.getText().contains(" ")) {
                        smartBarInput.setForeground(errorColor);
                    }
                }
            } catch (Exception ignore) {}
        });
    }

    public void setMargin(int margin) {
        marginTop.setPreferredSize(new Dimension(marginTop.getWidth(), margin));
        marginRight.setPreferredSize(new Dimension(margin, marginRight.getHeight()));
        marginBottom.setPreferredSize(new Dimension(marginBottom.getWidth(), margin));
        marginLeft.setPreferredSize(new Dimension(margin, marginLeft.getHeight()));
    }

    public void setMargin(int top, int right, int bottom, int left) {
        marginTop.setPreferredSize(new Dimension(marginTop.getWidth(), top));
        marginRight.setPreferredSize(new Dimension(right, marginRight.getHeight()));
        marginBottom.setPreferredSize(new Dimension(marginBottom.getWidth(), bottom));
        marginLeft.setPreferredSize(new Dimension(left, marginLeft.getHeight()));
    }

    private static class RoundedBorder implements Border {

        private final int radius;
        private final Color borderColor;

        public RoundedBorder(int radius,Color borderColor) {
            this.radius = radius;
            this.borderColor = borderColor;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius + 1, this.radius + 1, this.radius + 2, this.radius);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(borderColor);
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }
}