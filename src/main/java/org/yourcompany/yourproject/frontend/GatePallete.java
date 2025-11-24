package org.yourcompany.yourproject.GUI.ui;

import javax.swing.*;

import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.gates.AndGate;
import org.yourcompany.yourproject.businessLayer.components.gates.NotGate;
import org.yourcompany.yourproject.businessLayer.components.gates.OrGate;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

public class GatePallete extends JPanel {
    private JButton Band, Bor, Bnot;
    private ComponentPaletteButton dragSource;
    
    // Modern dark theme colors
    private static final Color BACKGROUND_COLOR = new Color(45, 45, 55);
    private static final Color ACCENT_COLOR = new Color(70, 130, 180);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color PANEL_BACKGROUND = new Color(35, 35, 45);
    private static final Color BORDER_COLOR = new Color(100, 100, 150);
    private static final Color BUTTON_BACKGROUND = new Color(55, 55, 65);
    private static final Color BUTTON_HOVER = new Color(80, 80, 100);

    public GatePallete() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 5)); // Increased horizontal gap
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                "COMPONENT PALETTE",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 10),
                TEXT_COLOR));
        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(250, 130)); // Increased width and height

        // Create palette buttons with custom painted icons
        Band = createGateButton("AND Gate", "AND", BUTTON_BACKGROUND);
        Bor = createGateButton("OR Gate", "OR", BUTTON_BACKGROUND);
        Bnot = createGateButton("NOT Gate", "NOT", BUTTON_BACKGROUND);

        add(Band);
        add(Bor);
        add(Bnot);

        // Add tooltips
        Band.setToolTipText("AND Gate: Output is HIGH when all inputs are HIGH (2 inputs, 1 output)");
        Bor.setToolTipText("OR Gate: Output is HIGH when any input is HIGH (2 inputs, 1 output)");
        Bnot.setToolTipText("NOT Gate: Output is the inverse of the input (1 input, 1 output)");
    }

    private JButton createGateButton(String text, String label, Color bgColor) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                // Draw custom background
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fill background
                if (getModel().isRollover()) {
                    g2d.setColor(BUTTON_HOVER);
                } else {
                    g2d.setColor(bgColor);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); // Slightly larger radius
                
                // Draw border
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1.5f)); // Thicker border
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                int width = getWidth();
                int height = getHeight();
                int centerX = width / 2;
                int centerY = height / 2;

                // Draw gate symbol
                g2d.setColor(TEXT_COLOR);
                if (text.equals("AND Gate")) {
                    drawAndGate(g2d, centerX, centerY, width, height);
                } else if (text.equals("OR Gate")) {
                    drawOrGate(g2d, centerX, centerY, width, height);
                } else if (text.equals("NOT Gate")) {
                    drawNotGate(g2d, centerX, centerY, width, height);
                }

                // Draw label below the symbol
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 9)); // Slightly larger font
                FontMetrics fm = g2d.getFontMetrics();
                int labelWidth = fm.stringWidth(label);
                g2d.drawString(label, centerX - labelWidth / 2, height - 8); // Adjusted position

                g2d.dispose();
            }
        };

        button.setPreferredSize(new Dimension(90, 60)); // Increased size: 90x60 instead of 80x50
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFont(new Font("Segoe UI", Font.BOLD, 9));
        button.setForeground(TEXT_COLOR);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);

        // Hover effect is handled in paintComponent
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });

        return button;
    }

    private void drawAndGate(Graphics2D g2d, int centerX, int centerY, int width, int height) {
        g2d.setStroke(new BasicStroke(2.0f)); // Thicker lines

        int gateWidth = 30; // Increased from 25
        int gateHeight = 24; // Increased from 20
        int x = centerX - gateWidth / 2;
        int y = centerY - gateHeight / 2 - 3;

        // Draw D-shaped AND gate
        Path2D.Double path = new Path2D.Double();
        path.moveTo(x, y);
        path.lineTo(x + gateWidth * 0.7, y);
        path.quadTo(x + gateWidth, y + gateHeight / 2, x + gateWidth * 0.7, y + gateHeight);
        path.lineTo(x, y + gateHeight);
        path.closePath();
        g2d.draw(path);

        // Draw input lines
        g2d.drawLine(x - 4, y + 8, x, y + 8); // Longer lines
        g2d.drawLine(x - 4, y + 16, x, y + 16); // Longer lines

        // Draw output line
        g2d.drawLine(
                (int) (x + gateWidth * 0.7),
                (int) (y + gateHeight / 2),
                (int) (x + gateWidth * 0.7 + 4), // Longer output line
                (int) (y + gateHeight / 2));
    }

    private void drawOrGate(Graphics2D g2d, int centerX, int centerY, int width, int height) {
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); // Thicker lines

        int gateWidth = 40; // Increased from 35
        int gateHeight = 26; // Increased from 22
        double x = centerX - gateWidth / 2.0;
        double y = centerY - gateHeight / 2.0 - 3;

        double leftStartX = x - gateWidth * 0.35;

        Path2D.Double outer = new Path2D.Double();
        outer.moveTo(leftStartX, y);
        outer.quadTo(x - gateWidth * 0.05, y + gateHeight / 2.0, leftStartX, y + gateHeight);
        outer.curveTo(x + gateWidth * 0.35, y + gateHeight, x + gateWidth * 0.92, y + gateHeight * 0.75,
                x + gateWidth, y + gateHeight / 2.0);
        outer.curveTo(x + gateWidth * 0.92, y + gateHeight * 0.25, x + gateWidth * 0.35, y, leftStartX, y);
        outer.closePath();
        g2d.draw(outer);

        Path2D.Double inner = new Path2D.Double();
        inner.moveTo(x, y);
        inner.quadTo(x + gateWidth * 0.45, y + gateHeight / 2.0, x, y + gateHeight);
        g2d.draw(inner);

        g2d.drawLine((int) (x - 10), (int) (y + gateHeight * 0.35), (int) (x - 2), (int) (y + gateHeight * 0.35)); // Longer lines
        g2d.drawLine((int) (x - 10), (int) (y + gateHeight * 0.65), (int) (x - 2), (int) (y + gateHeight * 0.65)); // Longer lines
        g2d.drawLine((int) (x + gateWidth), (int) (y + gateHeight / 2.0), (int) (x + gateWidth + 10), // Longer output line
                (int) (y + gateHeight / 2.0));
    }

    private void drawNotGate(Graphics2D g2d, int centerX, int centerY, int width, int height) {
        g2d.setStroke(new BasicStroke(2.0f)); // Thicker lines

        int gateWidth = 26; // Increased from 22
        int gateHeight = 22; // Increased from 18
        int x = centerX - gateWidth / 2;
        int y = centerY - gateHeight / 2 - 3;

        // Draw triangle (NOT gate)
        Path2D.Double path = new Path2D.Double();
        path.moveTo(x, y);
        path.lineTo(x + gateWidth, y + gateHeight / 2);
        path.lineTo(x, y + gateHeight);
        path.closePath();
        g2d.draw(path);

        // Draw input line
        g2d.drawLine(x - 4, y + gateHeight / 2, x, y + gateHeight / 2); // Longer input line

        // Draw output line with bubble (circle)
        int outputX = x + gateWidth;
        int outputY = y + gateHeight / 2;
        g2d.drawLine(outputX, outputY, outputX + 6, outputY); // Longer output line
        g2d.fillOval(outputX + 6, outputY - 3, 5, 5); // Larger bubble
        g2d.drawOval(outputX + 6, outputY - 3, 5, 5); // Larger bubble
    }

    public JButton getAndButton() {
        return Band;
    }

    public JButton getOrButton() {
        return Bor;
    }

    public JButton getNotButton() {
        return Bnot;
    }

    public ComponentPaletteButton getDragSource() {
        return dragSource;
    }

    public void clearDragSource() {
        dragSource = null;
    }

    /**
     * Helper class to track what component is being dragged from the palette
     */
    public static class ComponentPaletteButton {
        private JButton button;
        private String componentType;

        public ComponentPaletteButton(JButton button, String componentType) {
            this.button = button;
            this.componentType = componentType;
        }

        public JButton getButton() {
            return button;
        }

        public String getComponentType() {
            return componentType;
        }

        public ComponentBase createComponent() {
            switch (componentType) {
                case "AND Gate":
                    return new AndGate();
                case "OR Gate":
                    return new OrGate();
                case "NOT Gate":
                    return new NotGate();
                default:
                    return null;
            }
        }
    }
}