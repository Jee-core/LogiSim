package org.yourcompany.yourproject.Frontend;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.yourcompany.yourproject.Backend.businessLayer.analysis.BooleanEquationGenerator;
import org.yourcompany.yourproject.Backend.businessLayer.components.GateComponent;
import org.yourcompany.yourproject.Backend.businessLayer.components.gates.LED;
import org.yourcompany.yourproject.Backend.businessLayer.components.gates.andd;
import org.yourcompany.yourproject.Backend.businessLayer.components.gates.nott;
import org.yourcompany.yourproject.Backend.businessLayer.components.gates.orr;


public class GatePallete extends JPanel {
    private JButton Band, Bor, Bnot, BLed, BbooleanExpression;
    private ComponentPaletteButton dragSource;
    private DesignArea circuitPanel;
    private GateComponent currentSelectedGate;
    
    // Modern dark theme colors
    private static final Color BACKGROUND_COLOR = new Color(45, 45, 55);
    private static final Color ACCENT_COLOR = new Color(70, 130, 180);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color PANEL_BACKGROUND = new Color(35, 35, 45);
    private static final Color BORDER_COLOR = new Color(100, 100, 150);
    private static final Color BUTTON_BACKGROUND = new Color(55, 55, 65);
    private static final Color BUTTON_HOVER = new Color(80, 80, 100);
    private static final Color BOOLEAN_BUTTON_COLOR = new Color(120, 80, 160); // Purple for boolean expression
    private static final Color BOOLEAN_BUTTON_DISABLED = new Color(60, 60, 70);
    
    // LED specific colors
    private static final Color LED_ON_COLOR = new Color(50, 200, 50);      // Bright green for LED
    private static final Color LED_OFF_COLOR = new Color(200, 50, 50);     // Bright red for LED
    private static final Color LED_BODY_COLOR = new Color(80, 80, 100);    // LED body color

    public GatePallete() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                "COMPONENT PALETTE",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 10),
                TEXT_COLOR));
        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(200, 150));

        // Create palette buttons with custom painted icons
        Band = createGateButton("AND Gate", "AND", BUTTON_BACKGROUND);
        Bor = createGateButton("OR Gate", "OR", BUTTON_BACKGROUND);
        Bnot = createGateButton("NOT Gate", "NOT", BUTTON_BACKGROUND);
        BLed = createGateButton("LED", "LED", BUTTON_BACKGROUND);
        BbooleanExpression = createBooleanExpressionButton();

        add(Band);
        add(Bor);
        add(Bnot);
        add(BLed);
        add(BbooleanExpression);

        // Add tooltips
        Band.setToolTipText("AND Gate: Output is HIGH when all inputs are HIGH (2 inputs, 1 output)");
        Bor.setToolTipText("OR Gate: Output is HIGH when any input is HIGH (2 inputs, 1 output)");
        Bnot.setToolTipText("NOT Gate: Output is the inverse of the input (1 input, 1 output)");
        BLed.setToolTipText("LED: Visual indicator - Lights up GREEN when input is HIGH, RED when LOW (1 input, 0 outputs)");
        BbooleanExpression.setToolTipText("Generate Boolean Expression for selected gate");

        // Initialize circuit panel reference (can be set later)
        this.circuitPanel = null;
        this.currentSelectedGate = null;
        
        // TEST: Enable the button for testing - REMOVE THIS LATER
        BbooleanExpression.setEnabled(true);
    }

    public void setCircuitPanel(DesignArea circuitPanel) {
        this.circuitPanel = circuitPanel;
        System.out.println("CircuitPanel set in GatePallete: " + (circuitPanel != null));
    }

    public void setSelectedGate(GateComponent gate) {
        this.currentSelectedGate = gate;
        System.out.println("Gate selected in GatePallete: " + (gate != null ? gate.getName() : "null"));
        updateBooleanButtonState();
    }

    private void updateBooleanButtonState() {
        boolean enabled = (currentSelectedGate != null);
        // For now, let's enable it if we have a selected gate, regardless of circuit panel
        // This makes it easier to test
        BbooleanExpression.setEnabled(enabled);
        System.out.println("Boolean button enabled: " + enabled);
        BbooleanExpression.repaint();
    }

    private JButton createGateButton(String text, String label, Color bgColor) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fill background
                if (getModel().isRollover()) {
                    g2d.setColor(BUTTON_HOVER);
                } else {
                    g2d.setColor(bgColor);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Draw border
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

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
                } else if (text.equals("LED")) {
                    drawLED(g2d, centerX, centerY, width, height);
                }

                // Draw label below the symbol
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 8));
                FontMetrics fm = g2d.getFontMetrics();
                int labelWidth = fm.stringWidth(label);
                g2d.drawString(label, centerX - labelWidth / 2, height - 6);

                g2d.dispose();
            }
        };

        // Reduced button size
        button.setPreferredSize(new Dimension(70, 50));
        button.setMinimumSize(new Dimension(70, 50));
        button.setMaximumSize(new Dimension(70, 50));
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFont(new Font("Segoe UI", Font.BOLD, 8));
        button.setForeground(TEXT_COLOR);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setEnabled(true);

        return button;
    }

    private JButton createBooleanExpressionButton() {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fill background with clear enabled/disabled states
                if (!isEnabled()) {
                    g2d.setColor(BOOLEAN_BUTTON_DISABLED);
                } else if (getModel().isPressed()) {
                    g2d.setColor(new Color(100, 60, 140)); // Pressed state
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(140, 100, 180)); // Hover color
                } else {
                    g2d.setColor(BOOLEAN_BUTTON_COLOR);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Draw border
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                int width = getWidth();
                int height = getHeight();
                int centerX = width / 2;
                int centerY = height / 2;

                // Draw Boolean expression symbol
                g2d.setColor(isEnabled() ? TEXT_COLOR : Color.GRAY);
                drawBooleanSymbol(g2d, centerX, centerY, width, height);

                // Draw label
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 7));
                FontMetrics fm = g2d.getFontMetrics();
                String label = "BOOLEAN";
                int labelWidth = fm.stringWidth(label);
                g2d.drawString(label, centerX - labelWidth / 2, height - 6);

                g2d.dispose();
            }
        };

        // Reduced button size
        button.setPreferredSize(new Dimension(70, 50));
        button.setMinimumSize(new Dimension(70, 50));
        button.setMaximumSize(new Dimension(70, 50));
        button.setBackground(BOOLEAN_BUTTON_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFont(new Font("Segoe UI", Font.BOLD, 7));
        button.setForeground(TEXT_COLOR);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setEnabled(true); // ENABLED BY DEFAULT FOR TESTING

        // Add action listener for boolean expression generation
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("=== BOOLEAN BUTTON CLICKED ===");
                System.out.println("Current selected gate: " + (currentSelectedGate != null ? currentSelectedGate.getName() : "null"));
                System.out.println("Circuit panel: " + (circuitPanel != null ? "set" : "null"));
                if (circuitPanel != null) {
                    System.out.println("Current circuit: " + (circuitPanel.getCurrentCircuit() != null ? "set" : "null"));
                }
                analyzeSelectedGate();
            }
        });

        return button;
    }

    private void drawBooleanSymbol(Graphics2D g2d, int centerX, int centerY, int width, int height) {
        g2d.setStroke(new BasicStroke(1.5f));
        
        // Draw f(x) symbol - simplified for smaller button
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        String symbol = "f(x)";
        int symbolWidth = fm.stringWidth(symbol);
        g2d.drawString(symbol, centerX - symbolWidth / 2, centerY + 4);
    }

    private void drawAndGate(Graphics2D g2d, int centerX, int centerY, int width, int height) {
        g2d.setStroke(new BasicStroke(1.5f));

        int gateWidth = 22;
        int gateHeight = 18;
        int x = centerX - gateWidth / 2;
        int y = centerY - gateHeight / 2 - 2;

        // Draw D-shaped AND gate
        Path2D.Double path = new Path2D.Double();
        path.moveTo(x, y);
        path.lineTo(x + gateWidth * 0.7, y);
        path.quadTo(x + gateWidth, y + gateHeight / 2, x + gateWidth * 0.7, y + gateHeight);
        path.lineTo(x, y + gateHeight);
        path.closePath();
        g2d.draw(path);

        // Draw input lines
        g2d.drawLine(x - 3, y + 6, x, y + 6);
        g2d.drawLine(x - 3, y + 12, x, y + 12);

        // Draw output line
        g2d.drawLine(
                (int) (x + gateWidth * 0.7),
                (int) (y + gateHeight / 2),
                (int) (x + gateWidth * 0.7 + 3),
                (int) (y + gateHeight / 2));
    }

    private void drawOrGate(Graphics2D g2d, int centerX, int centerY, int width, int height) {
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int gateWidth = 30;
        int gateHeight = 20;
        double x = centerX - gateWidth / 2.0;
        double y = centerY - gateHeight / 2.0 - 2;

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

        g2d.drawLine((int) (x - 8), (int) (y + gateHeight * 0.35), (int) (x - 2), (int) (y + gateHeight * 0.35));
        g2d.drawLine((int) (x - 8), (int) (y + gateHeight * 0.65), (int) (x - 2), (int) (y + gateHeight * 0.65));
        g2d.drawLine((int) (x + gateWidth), (int) (y + gateHeight / 2.0), (int) (x + gateWidth + 8),
                (int) (y + gateHeight / 2.0));
    }

    private void drawNotGate(Graphics2D g2d, int centerX, int centerY, int width, int height) {
        g2d.setStroke(new BasicStroke(1.5f));

        int gateWidth = 20;
        int gateHeight = 16;
        int x = centerX - gateWidth / 2;
        int y = centerY - gateHeight / 2 - 2;

        // Draw triangle (NOT gate)
        Path2D.Double path = new Path2D.Double();
        path.moveTo(x, y);
        path.lineTo(x + gateWidth, y + gateHeight / 2);
        path.lineTo(x, y + gateHeight);
        path.closePath();
        g2d.draw(path);

        // Draw input line
        g2d.drawLine(x - 3, y + gateHeight / 2, x, y + gateHeight / 2);

        // Draw output line with bubble
        int outputX = x + gateWidth;
        int outputY = y + gateHeight / 2;
        g2d.drawLine(outputX, outputY, outputX + 4, outputY);
        g2d.fillOval(outputX + 4, outputY - 2, 4, 4);
        g2d.drawOval(outputX + 4, outputY - 2, 4, 4);
    }

    private void drawLED(Graphics2D g2d, int centerX, int centerY, int width, int height) {
        g2d.setStroke(new BasicStroke(1.5f));

        int ledWidth = 18;
        int ledHeight = 15;
        int x = centerX - ledWidth / 2;
        int y = centerY - ledHeight / 2 - 2;

        // Draw LED body
        g2d.setColor(LED_BODY_COLOR);
        g2d.fillRoundRect(x, y, ledWidth, ledHeight, 4, 4);
        
        // Draw LED body outline
        g2d.setColor(TEXT_COLOR);
        g2d.drawRoundRect(x, y, ledWidth, ledHeight, 4, 4);

        // Draw LED light
        int lightSize = 9;
        int lightX = centerX - lightSize / 2;
        int lightY = centerY - lightSize / 2 - 2;
        
        g2d.setColor(LED_OFF_COLOR);
        g2d.fillOval(lightX, lightY, lightSize, lightSize);
        
        g2d.setColor(TEXT_COLOR);
        g2d.drawOval(lightX, lightY, lightSize, lightSize);

        // Draw input line
        g2d.drawLine(x - 3, centerY - 2, x, centerY - 2);
    }

    private void analyzeSelectedGate() {
        System.out.println("AnalyzeSelectedGate called");
        
        if (currentSelectedGate == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a gate in the circuit first.\n\n" +
                "Click on any gate in the design area to select it, then click the Boolean button.",
                "No Gate Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Try to analyze even without circuit panel for testing
        try {
            System.out.println("Generating boolean expression for: " + currentSelectedGate.getName());
            
            String expr;
            if (circuitPanel != null && circuitPanel.getCurrentCircuit() != null) {
                BooleanEquationGenerator gen = new BooleanEquationGenerator();
                expr = gen.getExpression(currentSelectedGate, circuitPanel.getCurrentCircuit());
            } else {
                // Fallback: generate basic expression based on gate type
                expr = generateBasicExpression(currentSelectedGate);
            }
            
            String analysisResult = "Boolean Expression Analysis:\n\n" +
                    "Gate: " + currentSelectedGate.getName() + "\n" +
                    "Expression: " + expr + "\n\n" +
                    "Inputs: " + currentSelectedGate.getInputs() + "\n" +
                    "Outputs: " + currentSelectedGate.getOutputs();
            
            JOptionPane.showMessageDialog(this, analysisResult, "Boolean Expression", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            System.out.println("Error in analyzeSelectedGate: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error analyzing gate: " + e.getMessage() + "\n\n" +
                "Make sure the gate is properly connected in a circuit.",
                "Analysis Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String generateBasicExpression(GateComponent gate) {
        // Basic fallback expression generation
        if (gate instanceof andd) {
            return "A ∧ B";
        } else if (gate instanceof orr) {
            return "A ∨ B";
        } else if (gate instanceof nott) {
            return "¬A";
        } else if (gate instanceof LED) {
            return "LED(A)";
        } else {
            return "Expression not available";
        }
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

    public JButton getLedButton() {
        return BLed;
    }

    public JButton getBooleanExpressionButton() {
        return BbooleanExpression;
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

        public GateComponent createComponent() {
            switch (componentType) {
                case "AND Gate":
                    return new andd();
                case "OR Gate":
                    return new orr();
                case "NOT Gate":
                    return new nott();
                case "LED":
                    return new LED();
                default:
                    return null;
            }
        }
    }
}