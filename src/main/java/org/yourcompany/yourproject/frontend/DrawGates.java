package org.yourcompany.yourproject.frontend;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.yourcompany.yourproject.backend.businessLayer.components.SubcircuitComponent;
import org.yourcompany.yourproject.backend.businessLayer.components.GateComponent;

import org.yourcompany.yourproject.backend.businessLayer.components.gates.orr;
import org.yourcompany.yourproject.backend.businessLayer.components.gates.LED;
import org.yourcompany.yourproject.backend.businessLayer.components.gates.andd;
import org.yourcompany.yourproject.backend.businessLayer.components.gates.nott;

public class DrawGates extends JPanel {
    private GateComponent gate;
    // SMALLER dimensions for more compact gates
    private static final int GATE_WIDTH = 100;
    private static final int GATE_HEIGHT = 70;
    private static final int PORT_RADIUS = 4;
    private static final int LINE_LENGTH = 25; // Shorter connection lines
    private static final Color PORT_FILL = Color.WHITE;
    private static final Color PORT_OUTLINE = new Color(80, 80, 100);
    private static final Color TEXT_COLOR = new Color(40, 40, 60);
    private static final Color SHADOW_COLOR = new Color(200, 200, 210, 80);
    private static final Color INPUT_COLOR = new Color(70, 130, 180);  // Steel blue for inputs
    private static final Color OUTPUT_COLOR = new Color(50, 168, 82);  // Green for outputs
    private static final Color GATE_OUTLINE = new Color(60, 60, 80);
    private static final Color GATE_FILL = new Color(245, 248, 255);   // Light background for gates
    private static final Color FILL_COLOR = new Color(249, 252, 255);
    private static final Color OUTLINE_COLOR = new Color(46, 95, 139);
    
    // LED specific colors
    private static final Color LED_ON_COLOR = new Color(50, 200, 50);      // Bright green when ON
    private static final Color LED_OFF_COLOR = new Color(200, 50, 50);     // Bright red when OFF
    private static final Color LED_BODY_COLOR = new Color(60, 60, 80);     // Dark gray for LED body
    private static final Color LED_GLOW_COLOR = new Color(50, 255, 50, 80); // Glow effect when ON
    
    public DrawGates(GateComponent gate) {
        this.gate = gate;
        setSize(GATE_WIDTH + LINE_LENGTH * 2, GATE_HEIGHT);
        setBackground(new Color(245, 248, 255, 0)); // Transparent background
        setBorder(BorderFactory.createEmptyBorder());
        setOpaque(false);
        setLayout(null);

        // Update gate position when component is moved
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent e) {
                if (gate != null) {
                    Point location = getLocation();
                    gate.setPosition(location.x, location.y);
                    // Notify parent to update wire connections
                    repaintParent();
                }
            }
        });
    }

    private void repaintParent() {
        Component parent = getParent();
        if (parent != null) {
            parent.repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGateBody(g2d);

        if (gate != null) {
            int inputs = gate.getInputs();
            int outputs = gate.getOutputs();

            // Draw input ports
            if (inputs > 0) {
                int spacing = GATE_HEIGHT / (inputs + 1);
                for (int i = 0; i < inputs; i++) {
                    int y = spacing * (i + 1);
                    drawInputPort(g2d, i, y);
                }
            }

            // Draw output ports (LED has no outputs)
            if (outputs > 0) {
                int spacing = GATE_HEIGHT / (outputs + 1);
                for (int i = 0; i < outputs; i++) {
                    int y = spacing * (i + 1);
                    drawOutputPort(g2d, i, y);
                }
            }
        }
        g2d.dispose();
    }
/**
 * Update the gate's visual state based on current inputs/outputs
 */
public void updateGateState() {
    // Force the gate to recompute its output
    if (gate != null) {
        gate.computeOutput();
    }
    
    // Repaint to show updated state
    repaint();
    
    // If this is an LED, make sure it shows the correct color
    if (gate instanceof LED) {
        // LED visual update is handled in paintComponent
        repaint();
    }
}
    private void drawGateBody(Graphics2D g2d) {
        if (gate == null) {
            drawDefaultBody(g2d);
            return;
        }

        int gateX = LINE_LENGTH;
        
        if (gate instanceof orr) {
            drawOrShape(g2d, gateX);
        } else if (gate instanceof andd) {
            drawAndShape(g2d, gateX);
        } else if (gate instanceof nott) {
            drawNotShape(g2d, gateX);
        } else if (gate instanceof SubcircuitComponent) {
            drawCircuitComponentShape(g2d, gateX);
        } else if (gate instanceof LED) {
            drawLEDShape(g2d, gateX);
        } else {
            drawDefaultBody(g2d);
        }
    }

    private void drawDefaultBody(Graphics2D g2d) {
        g2d.setColor(FILL_COLOR);
        g2d.fillRoundRect(5, 5, GATE_WIDTH - 10, GATE_HEIGHT - 10, 20, 20);
        g2d.setColor(OUTLINE_COLOR);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(5, 5, GATE_WIDTH - 10, GATE_HEIGHT - 10, 20, 20);
    }

    private void drawInputPort(Graphics2D g2d, int portIndex, int y) {
        int startX = 0;
        int endX = LINE_LENGTH;
        int portX = endX;

        // Draw horizontal wire line with input color
        g2d.setColor(INPUT_COLOR);
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(startX, y, portX, y);

        // Draw port circle
        g2d.setColor(PORT_FILL);
        g2d.fillOval(portX - PORT_RADIUS, y - PORT_RADIUS, PORT_RADIUS * 2, PORT_RADIUS * 2);
        
        g2d.setColor(INPUT_COLOR);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(portX - PORT_RADIUS, y - PORT_RADIUS, PORT_RADIUS * 2, PORT_RADIUS * 2);

        // Draw current value with colored background
        Boolean value = gate.getInputVal(portIndex);
        String valueText = value != null && value ? "1" : "0";
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 10)); // Smaller font
        Color valueColor = value != null && value ? new Color(0, 120, 0) : new Color(180, 0, 0);
        
        int valueWidth = g2d.getFontMetrics().stringWidth(valueText);
        
        // Value background with input color theme
        g2d.setColor(new Color(INPUT_COLOR.getRed(), INPUT_COLOR.getGreen(), INPUT_COLOR.getBlue(), 40));
        g2d.fillRoundRect(startX - valueWidth - 8, y - 18, valueWidth + 4, 14, 4, 4);
        g2d.setColor(INPUT_COLOR);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(startX - valueWidth - 8, y - 18, valueWidth + 4, 14, 4, 4);
        
        // Value text
        g2d.setColor(valueColor);
        g2d.drawString(valueText, startX - valueWidth - 6, y - 8);
    }

    private void drawOutputPort(Graphics2D g2d, int portIndex, int y) {
        int startX = GATE_WIDTH + LINE_LENGTH;
        int endX = GATE_WIDTH;
        int portX = endX;

        // Draw horizontal wire line with output color
        g2d.setColor(OUTPUT_COLOR);
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(startX, y, portX, y);

        // Draw port circle
        g2d.setColor(PORT_FILL);
        g2d.fillOval(portX - PORT_RADIUS, y - PORT_RADIUS, PORT_RADIUS * 2, PORT_RADIUS * 2);
        
        g2d.setColor(OUTPUT_COLOR);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(portX - PORT_RADIUS, y - PORT_RADIUS, PORT_RADIUS * 2, PORT_RADIUS * 2);

        // Draw current value with colored background
        Boolean value = gate.getOutputVal(portIndex);
        String valueText = value != null && value ? "1" : "0";
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 10)); // Smaller font
        Color valueColor = value != null && value ? new Color(0, 120, 0) : new Color(180, 0, 0);
        
        int valueWidth = g2d.getFontMetrics().stringWidth(valueText);
        
        // Value background with output color theme
        g2d.setColor(new Color(OUTPUT_COLOR.getRed(), OUTPUT_COLOR.getGreen(), OUTPUT_COLOR.getBlue(), 40));
        g2d.fillRoundRect(startX + 2, y - 18, valueWidth + 4, 14, 4, 4);
        g2d.setColor(OUTPUT_COLOR);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(startX + 2, y - 18, valueWidth + 4, 14, 4, 4);
        
        // Value text
        g2d.setColor(valueColor);
        g2d.drawString(valueText, startX + 4, y - 8);
    }

    private void drawAndShape(Graphics2D g2d, int xOffset) {
        int inset = 8; // Smaller inset
        int width = GATE_WIDTH - inset * 2;
        int height = GATE_HEIGHT - inset * 2;
        int x = xOffset + inset;
        int y = inset;

        // Main shape
        GeneralPath path = new GeneralPath();
        path.moveTo(x, y);
        path.lineTo(x + width * 0.7, y);
        path.quadTo(x + width, y + height / 2.0, x + width * 0.7, y + height);
        path.lineTo(x, y + height);
        path.closePath();

        // Fill with light color
        g2d.setColor(GATE_FILL);
        g2d.fill(path);
        
        // Draw outline
        g2d.setColor(GATE_OUTLINE);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(path);
    }

    private void drawOrShape(Graphics2D g2d, int xOffset) {
        int inset = 10; // Smaller inset
        double width = GATE_WIDTH - inset * 2;
        double height = GATE_HEIGHT - inset * 2;
        double x = xOffset + inset;
        double y = inset;

        double leftStartX = x - width * 0.3;

        GeneralPath outer = new GeneralPath();
        outer.moveTo(leftStartX, y);
        outer.quadTo(x - width * 0.05, y + height / 2.0, leftStartX, y + height);
        outer.curveTo(x + width * 0.3, y + height, x + width * 0.9, y + height * 0.75, x + width, y + height / 2.0);
        outer.curveTo(x + width * 0.9, y + height * 0.25, x + width * 0.3, y, leftStartX, y);
        outer.closePath();

        // Fill with light color
        g2d.setColor(GATE_FILL);
        g2d.fill(outer);
        
        // Draw outline
        g2d.setColor(GATE_OUTLINE);
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(outer);

        // Draw inner curve
        GeneralPath inner = new GeneralPath();
        inner.moveTo(x, y);
        inner.quadTo(x + width * 0.45, y + height / 2.0, x, y + height);
        g2d.draw(inner);
    }

    private void drawNotShape(Graphics2D g2d, int xOffset) {
        int inset = 10; // Smaller inset
        int width = GATE_WIDTH - inset * 2;
        int height = GATE_HEIGHT - inset * 2;
        int x = xOffset + inset;
        int y = inset;

        // Main triangle
        GeneralPath triangle = new GeneralPath();
        triangle.moveTo(x, y);
        triangle.lineTo(x + width - 6, y + height / 2.0);
        triangle.lineTo(x, y + height);
        triangle.closePath();

        // Fill with light color
        g2d.setColor(GATE_FILL);
        g2d.fill(triangle);
        
        // Draw outline
        g2d.setColor(GATE_OUTLINE);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(triangle);

        // Draw NOT bubble (smaller)
        int bubbleDiameter = 10;
        int bubbleX = x + width - 6;
        int bubbleY = (int)(y + height / 2.0 - bubbleDiameter / 2);
        
        g2d.setColor(PORT_FILL);
        g2d.fillOval(bubbleX, bubbleY, bubbleDiameter, bubbleDiameter);
        g2d.setColor(GATE_OUTLINE);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(bubbleX, bubbleY, bubbleDiameter, bubbleDiameter);
    }

    private void drawCircuitComponentShape(Graphics2D g2d, int xOffset) {
        int inset = 8;
        int width = GATE_WIDTH - inset * 2;
        int height = GATE_HEIGHT - inset * 2;
        int x = xOffset + inset;
        int y = inset;

        // Draw a rectangle with rounded corners for circuit component
        RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, 15, 15);

        // Fill with a different color to distinguish from basic gates
        Color circuitFill = new Color(255, 248, 225); // Light orange color
        g2d.setColor(circuitFill);
        g2d.fill(rect);
        
        // Draw outline
        g2d.setColor(GATE_OUTLINE);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(rect);

        // Draw circuit name
        SubcircuitComponent circuitComp = (SubcircuitComponent) gate;
        String displayName = circuitComp.getCircuitName();
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2d.setColor(TEXT_COLOR);
        
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(displayName);
        int textX = x + (width - textWidth) / 2;
        int textY = y + height / 2 + fm.getAscent() / 2 - 2;
        
        g2d.drawString(displayName, textX, textY);

        // Draw "CIRCUIT" label below the name
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g2d.setColor(new Color(100, 100, 120));
        String circuitLabel = "CIRCUIT";
        int labelWidth = g2d.getFontMetrics().stringWidth(circuitLabel);
        int labelX = x + (width - labelWidth) / 2;
        int labelY = textY + 12;
        g2d.drawString(circuitLabel, labelX, labelY);
    }

    private void drawLEDShape(Graphics2D g2d, int xOffset) {
        int inset = 8;
        int width = GATE_WIDTH - inset * 2;
        int height = GATE_HEIGHT - inset * 2;
        int x = xOffset + inset;
        int y = inset;

        // Draw LED body (rectangle)
        RoundRectangle2D body = new RoundRectangle2D.Double(x, y + 10, width, height - 20, 10, 10);
        g2d.setColor(LED_BODY_COLOR);
        g2d.fill(body);
        g2d.setColor(GATE_OUTLINE);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(body);

        // Determine LED state
        LED led = (LED) gate;
        boolean isLit = led.isLit();
        Color ledColor = isLit ? LED_ON_COLOR : LED_OFF_COLOR;

        // Draw LED light (circle in the center)
        int ledSize = 20;
        int ledX = x + (width - ledSize) / 2;
        int ledY = y + (height - ledSize) / 2;
        
        // Draw glow effect when lit
        if (isLit) {
            g2d.setColor(LED_GLOW_COLOR);
            g2d.fillOval(ledX - 5, ledY - 5, ledSize + 10, ledSize + 10);
        }
        
        // Draw main LED light
        g2d.setColor(ledColor);
        g2d.fillOval(ledX, ledY, ledSize, ledSize);
        
        // Draw LED outline
        g2d.setColor(GATE_OUTLINE);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(ledX, ledY, ledSize, ledSize);

        // Draw LED label
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2d.setColor(Color.WHITE);
        
        FontMetrics fm = g2d.getFontMetrics();
        String ledText = "LED";
        int textWidth = fm.stringWidth(ledText);
        int textX = x + (width - textWidth) / 2;
        int textY = y + height - 8;
        
        g2d.drawString(ledText, textX, textY);

        // Draw status indicator
        String statusText = isLit ? "ON" : "OFF";
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 9));
        g2d.setColor(isLit ? Color.GREEN : Color.RED);
        
        int statusWidth = g2d.getFontMetrics().stringWidth(statusText);
        int statusX = x + (width - statusWidth) / 2;
        int statusY = y + 15;
        
        g2d.drawString(statusText, statusX, statusY);
    }

    // Check if a point is on an input port
    public int getInputPortAt(Point p) {
        if (gate == null) return -1;
        int inputs = gate.getInputs();
        if (inputs == 0) return -1;

        int spacing = GATE_HEIGHT / (inputs + 1);
        for (int i = 0; i < inputs; i++) {
            int y = spacing * (i + 1);
            int portX = LINE_LENGTH;
            double distance = Math.sqrt(Math.pow(p.x - portX, 2) + Math.pow(p.y - y, 2));
            if (distance <= PORT_RADIUS + 3) {
                return i;
            }
        }
        return -1;
    }

    // Check if a point is on an output port
    public int getOutputPortAt(Point p) {
        if (gate == null) return -1;
        int outputs = gate.getOutputs();
        if (outputs == 0) return -1;

        int spacing = GATE_HEIGHT / (outputs + 1);
        for (int i = 0; i < outputs; i++) {
            int y = spacing * (i + 1);
            int portX = GATE_WIDTH + LINE_LENGTH;
            double distance = Math.sqrt(Math.pow(p.x - portX, 2) + Math.pow(p.y - y, 2));
            if (distance <= PORT_RADIUS + 3) {
                return i;
            }
        }
        return -1;
    }

    public GateComponent getGate() {
        return gate;
    }

    public void setGate(GateComponent gate) {
        this.gate = gate;
        if (gate != null) {
            Point location = getLocation();
            gate.setPosition(location.x, location.y);
        }
        repaint();
    }

    // Get absolute position of an input port (connect to the actual port position)
    public Point getInputPortAbsolutePosition(int portIndex) {
        Point location = getLocation();
        if (gate == null) return location;

        int inputs = gate.getInputs();
        if (inputs == 0 || portIndex < 0 || portIndex >= inputs) return location;

        int spacing = GATE_HEIGHT / (inputs + 1);
        int y = spacing * (portIndex + 1);
        
        // Connect to the actual INPUT PORT position (blue circle)
        int portX = location.x + LINE_LENGTH;
        int portY = location.y + y;
        
        return new Point(portX, portY);
    }

    // Get absolute position of an output port (connect to the actual port position)
    public Point getOutputPortAbsolutePosition(int portIndex) {
        Point location = getLocation();
        if (gate == null) return location;

        int outputs = gate.getOutputs();
        if (outputs == 0 || portIndex < 0 || portIndex >= outputs) return location;

        int spacing = GATE_HEIGHT / (outputs + 1);
        int y = spacing * (portIndex + 1);
        
        // Connect to the actual OUTPUT PORT position (green circle)
        int portX = location.x + GATE_WIDTH + LINE_LENGTH;
        int portY = location.y + y;
        
        return new Point(portX, portY);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(GATE_WIDTH + LINE_LENGTH * 2, GATE_HEIGHT);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(GATE_WIDTH + LINE_LENGTH * 2, GATE_HEIGHT);
    }
}