package org.yourcompany.yourproject.Frontend;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.yourcompany.yourproject.Backend.businessLayer.components.Circuit;
import org.yourcompany.yourproject.Backend.businessLayer.components.SubcircuitComponent;
import org.yourcompany.yourproject.Backend.businessLayer.components.gates.LED;
import org.yourcompany.yourproject.Backend.businessLayer.components.Connector;
import org.yourcompany.yourproject.Backend.businessLayer.components.GateComponent;

public class DesignArea extends JPanel {

    private JLabel label;
    private List<DrawGates> gateUIs = new ArrayList<>();
    private List<DrawWire> wireUIs = new ArrayList<>();
    private static DesignArea instance;
    private Circuit currentCircuit;
    
    // Controller and View Service
    private Board controller;
    private BoardHelper viewService;
    
    // Connection mode variables (kept for backward compatibility)
    private DrawGates sourceGate;
    private int sourcePort;
    private boolean isConnecting = false;
    private Point tempWireEnd; // For drawing temporary wire during connection

    // Modern theme properties
    private final Color BACKGROUND_COLOR = new Color(28, 32, 43); // Dark blue-gray background
    private final Color PANEL_COLOR = new Color(36, 41, 54); // Slightly lighter panel color
    private final Color ACCENT_COLOR = new Color(100, 100, 150); // Same purple accent
    private final Color TEXT_COLOR = new Color(220, 220, 230); // Light gray text
    private final Color BORDER_COLOR = new Color(45,45,55); // Subtle border color
    
    // Grid properties - UPDATED TO LIGHT GRID LINES
    private final Color GRID_LINE_COLOR = new Color(50, 55, 70, 80); // Subtle grid lines
    private final Color GRID_MAJOR_COLOR = new Color(70, 75, 95, 60); // Slightly more visible major lines
    private final int GRID_SPACING = 15; // Smaller spacing for finer grid
    private final int MAJOR_GRID_SPACING = 60; // Every 4th line is major

    public DesignArea() {
        setLayout(null);
        setBackground(BACKGROUND_COLOR);
        
        // Modern border with subtle shadow effect
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(20, 25, 35), 1),
            BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 8, 8, 8),
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    "DESIGN CANVAS",
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 11),
                    TEXT_COLOR
                )
            )
        ));

        // Wire selection functionality
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Deselect all wires first
                for (Component comp : getComponents()) {
                    if (comp instanceof DrawWire) {
                        ((DrawWire) comp).setSelected(false);
                    }
                }
                
                // Check if clicked on a wire
                for (Component comp : getComponents()) {
                    if (comp instanceof DrawWire && comp.contains(e.getPoint())) {
                        ((DrawWire) comp).setSelected(true);
                        break;
                    }
                }
                
                repaint();
            }
        });

        // Modern label with gradient effect simulation
        label = new JLabel("CIRCUIT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create gradient background for label - MATCHING THEME COLORS
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(70, 75, 95, 150), // Darker blue-gray
                    0, getHeight(), new Color(50, 55, 75, 100) // Even darker
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Set text color to match theme
                setForeground(TEXT_COLOR);
                super.paintComponent(g);
            }
        };
        label.setBounds(25, 25, 180, 28);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_COLOR);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setOpaque(false);
        add(label);
        instance = this;
        
        
        // Initialize controller and view service
        viewService = new BoardHelper(this);
        controller = new Board(this, viewService);
        
        // Add mouse listener for connection handling (backward compatibility)
        addMouseListener(new ConnectionMouseAdapter());
        addMouseMotionListener(new ConnectionMouseAdapter());
    }

    public void showCircuit(String name) {
        label.setText("CIRCUIT: " + name.toUpperCase());
        repaint();
    }

    /**
     * Set the current circuit being edited
     */
    public void setCurrentCircuit(Circuit circuit) {
        this.currentCircuit = circuit;
        if (controller != null) {
            controller.setCurrentCircuit(circuit);
        }
    }
    
    public void refreshCircuitLabel() {
        if (currentCircuit != null) {
            showCircuit(currentCircuit.getName());
        } else {
            showCircuit("NO CIRCUIT SELECTED");
        }
    }
    
    public Circuit getCurrentCircuit() {
        return currentCircuit;
    }
    
    /**
     * Get the canvas controller
     */
    public Board getController() {
        return controller;
    }

    /**
     * Register a listener for gate selection changes
     */
    public void setGateSelectionListener(BoardHelper.GateSelectionListener listener) {
        if (viewService != null) {
            viewService.setGateSelectionListener(listener);
        }
    }

    /**
     * Add actual gates and connectors of a circuit to the design area
     * If called with a new circuit, it replaces the current one
     */
    public void addCircuitComponents(Circuit circuit) {
        this.currentCircuit = circuit;
        
        // Use controller to load circuit (this ensures proper listener setup)
        if (controller != null) {
            controller.setCurrentCircuit(circuit);
            // Update local lists for backward compatibility
            gateUIs = viewService.getGateViews();
            updateWireConnections();
            showCircuit(circuit.getName());
            revalidate();
            repaint();
            return;
        }
        
        // Fallback to old method if controller not available
        int offsetX = 50;
        int offsetY = 50;

        // Clear existing components ONLY if this is a different circuit
        if (this.currentCircuit != circuit) {
            clearCircuit();
            this.currentCircuit = circuit;
        }

        // Add gates
        for (GateComponent gate : circuit.getGates()) {
            // Check if gate already exists in UI to avoid duplicates
            boolean gateExists = false;
            for (DrawGates existingGate : gateUIs) {
                if (existingGate.getGate() == gate) {
                    gateExists = true;
                    break;
                }
            }
            
            if (!gateExists) {
                DrawGates gateUI = new DrawGates(gate);
                Point pos = gate.getPosition();
                gateUI.setLocation(pos.x == 0 && pos.y == 0 ? offsetX : pos.x, 
                                  pos.x == 0 && pos.y == 0 ? offsetY : pos.y);
                gate.setPosition(gateUI.getLocation().x, gateUI.getLocation().y);
                add(gateUI);
                enableDrag(gateUI);
                enableConnection(gateUI);
                gateUIs.add(gateUI);

                offsetX += gateUI.getWidth() + 20;
                if (offsetX > getWidth() - 100) {
                    offsetX = 50;
                    offsetY += 100;
                }
            }
        }

        // Add connectors/wires
        updateWireConnections();
        showCircuit(circuit.getName());
        revalidate();
        repaint();
    }

    /**
     * Create wire connection between gates
     */
    public void createWireConnection(DrawGates sourceGate, int sourcePort, 
                                    DrawGates targetGate, int targetPort) {
        
        if (currentCircuit == null) {
            JOptionPane.showMessageDialog(this, 
                "No circuit selected. Please create or open a circuit first.",
                "No Circuit",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        // Create the business layer connector
        Connector connector = new Connector(
            sourceGate.getGate(), sourcePort,
            targetGate.getGate(), targetPort
        );
        
        // Create the UI connector with the UI gates and port indices
        DrawWire connectorUI = new DrawWire(
            connector, 
            sourceGate, sourcePort, 
            targetGate, targetPort
        );
        
        // Add to circuit and design area
        currentCircuit.addConnectionWire(connector);
        add(connectorUI);
        setComponentZOrder(connectorUI, 0); // Send to back
        wireUIs.add(connectorUI);
        
        // Update connector with UI references
        connector.setFromUI(sourceGate);
        connector.setToUI(targetGate);
        
        // Special handling for LED connections
        if (targetGate.getGate() instanceof LED) {
            // Force LED to update its state immediately
            LED led = (LED) targetGate.getGate();
            led.computeOutput();
            targetGate.repaint(); // Force visual update
            
            // Also update the LED status panel
            repaint();
        }
        
        // Repaint
        repaint();
    }
    /**
 * Enhanced refresh method that updates entire circuit visually
 */
public void refreshEntireCircuit() {
    System.out.println("DEBUG: DesignArea - Refreshing entire circuit...");
    
    if (controller != null && currentCircuit != null) {
        // Use controller to propagate signals and update visuals
        controller.propagateSignals();
        
        // Force update of all gate visuals
        updateAllGateVisuals();
        
        // Update LED states specifically
        updateLEDStates();
        
        // Update wire connections
        updateWireConnections();
        
        // Force repaint
        repaint();
        
        System.out.println("DEBUG: DesignArea - Refresh completed");
    } else {
        System.out.println("DEBUG: Cannot refresh - controller: " + controller + ", circuit: " + currentCircuit);
    }
}

/**
 * Force update of all gate visuals
 */
public void updateAllGateVisuals() {
    for (DrawGates gateUI : gateUIs) {
        gateUI.repaint();
    }
    
    // Also use viewService if available
    if (viewService != null) {
        viewService.updateAllGateVisuals();
    }
}

/**
 * Get the BoardHelper instance for external access
 */
public BoardHelper getViewService() {
    return viewService;
}

/**
 * Get the Board controller for external access
 */
public Board getBoardController() {
    return controller;
}

/**
 * Enhanced LED state update that forces circuit recomputation
 */
public void updateLEDStatesWithPropagation() {
    if (currentCircuit != null) {
        // First propagate signals through the circuit
        currentCircuit.sigPropogation();
        
        // Then update LED states
        updateLEDStates();
        
        // Force visual update
        repaint();
    }
}

    /**
     * Update wire connections in the UI
     */
    private void updateWireConnections() {
        // Remove old wires
        for (DrawWire wireUI : wireUIs) {
            remove(wireUI);
        }
        wireUIs.clear();

        if (currentCircuit != null) {
            currentCircuit.updateConnectorPos();
            
            // Recreate all wires with proper gate references
            for (Connector wire : currentCircuit.getWires()) {
                // Find the UI gates for this wire
                DrawGates sourceGateUI = null;
                DrawGates targetGateUI = null;
                
                for (DrawGates gateUI : gateUIs) {
                    if (gateUI.getGate() == wire.getFromGate()) {
                        sourceGateUI = gateUI;
                    }
                    if (gateUI.getGate() == wire.getToGate()) {
                        targetGateUI = gateUI;
                    }
                }
                
                if (sourceGateUI != null && targetGateUI != null) {
                    DrawWire wireUI = new DrawWire(
                        wire, 
                        sourceGateUI, wire.getSourcePortIndex(),
                        targetGateUI, wire.getDestinationPortIndex()
                    );
                    wireUI.setBounds(0, 0, getWidth(), getHeight());
                    add(wireUI);
                    wireUIs.add(wireUI);
                    setComponentZOrder(wireUI, 0);
                    
                    // Update the connector with UI references
                    wire.setFromUI(sourceGateUI);
                    wire.setToUI(targetGateUI);
                }
            }
        }
        
        // Make sure gates are on top
        for (DrawGates gateUI : gateUIs) {
            setComponentZOrder(gateUI, 1);
        }
    }

    private void enableConnection(DrawGates gateUI) {
        gateUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point localPoint = e.getPoint();
                Point absPoint = SwingUtilities.convertPoint(gateUI, localPoint, DesignArea.this);
                
                int outputPort = gateUI.getOutputPortAt(localPoint);
                if (outputPort >= 0) {
                    isConnecting = true;
                    sourceGate = gateUI;
                    sourcePort = outputPort;
                    tempWireEnd = absPoint;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isConnecting && sourceGate != null) {
                    Point localPoint = e.getPoint();
                    int inputPort = gateUI.getInputPortAt(localPoint);
                    
                    if (inputPort >= 0 && gateUI != sourceGate) {
                        createWireConnection(sourceGate, sourcePort, gateUI, inputPort);
                    }
                    
                    isConnecting = false;
                    sourceGate = null;
                    sourcePort = -1;
                    tempWireEnd = null;
                    repaint();
                }
            }
        });
    }

    /**
     * Add a new gate component to the current circuit at the specified position
     */
    public boolean addGateToCircuit(GateComponent gate, int x, int y) {
        // Use controller to add gate
        if (controller != null) {
            boolean result = controller.addGate(gate, x, y);
            if (result) {
                // Update local lists for backward compatibility
                gateUIs = viewService.getGateViews();
                // Ensure wires are updated for the new gate
                updateWireConnections();
                
                // Special handling for LED - ensure it gets proper updates
                if (gate instanceof LED) {
                    // Force initial state update
                    ((LED) gate).computeOutput();
                    // Force visual update
                    viewService.updateLEDStates();
                }
            }
            return result;
        }
        
        // Fallback to old implementation if controller not available
        if (currentCircuit == null) {
            JOptionPane.showMessageDialog(this, 
                "Please open or create a circuit first.\n" +
                "1. Select a project\n" +
                "2. Create a new circuit or open an existing one",
                "No Circuit Selected",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (gate == null) return false;
        
        // Ensure position is within bounds
        if (x < 0) x = 50;
        if (y < 50) y = 50; // Leave space for label

        // Set the gate position
        gate.setPosition(x, y);

        // Add to circuit
        currentCircuit.addGateFunc(gate);

        // Create UI component
        DrawGates gateUI = new DrawGates(gate);
        gateUI.setLocation(x, y);
        add(gateUI);
        
        // Enable interactions
        if (controller == null) {
            enableDrag(gateUI);
            enableConnection(gateUI);
        }
        gateUIs.add(gateUI);

        // Special handling for LED
        if (gate instanceof LED) {
            // Force initial state computation
            ((LED) gate).computeOutput();
            gateUI.repaint(); // Force visual update
            
            // Update LED status panel
            repaint();
        }

        // Update wire connections to reflect new gate
        updateWireConnections();

        revalidate();
        repaint();
        return true;
    }

    /**
     * Update LED states when circuit simulation runs
     */
    public void updateLEDStates() {
        for (DrawGates gateUI : gateUIs) {
            if (gateUI.getGate() instanceof LED) {
                LED led = (LED) gateUI.getGate();
                led.computeOutput(); // Recompute LED state
                gateUI.repaint(); // Force visual update
            }
        }
        repaint();
    }

    /**
     * Get all LED components in the circuit
     */
    public List<LED> getLEDs() {
        List<LED> leds = new ArrayList<>();
        for (DrawGates gateUI : gateUIs) {
            if (gateUI.getGate() instanceof LED) {
                leds.add((LED) gateUI.getGate());
            }
        }
        return leds;
    }

    /**
     * Check if any LED is currently lit (useful for testing)
     */
    public boolean hasLitLEDs() {
        for (LED led : getLEDs()) {
            if (led.isLit()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Run circuit simulation and update all LED states
     */
    public void simulateCircuit() {
        if (currentCircuit != null) {
            // Evaluate the circuit
            currentCircuit.evaluate();
            
            // Force LED state updates
            updateLEDStates();
            
            // Update all gate visuals
            for (DrawGates gateUI : gateUIs) {
                gateUI.repaint();
            }
            
            repaint();
        }
    }

    /**
     * Clears the current design area
     */
    public void clearCircuit() {
        for (DrawGates g : gateUIs) remove(g);
        for (DrawWire w : wireUIs) remove(w);
        gateUIs.clear();
        wireUIs.clear();
        currentCircuit = null;
        label.setText("NO CIRCUIT SELECTED");
        if (viewService != null) {
            viewService.selectGate(null);
        }
        repaint();
    }

    /**
     * Checks if a circuit by this name is already placed
     */
    public boolean isCircuitPlaced(String circuitName) {
        for (DrawGates g : gateUIs) {
            if (g.getGate().getName().equals(circuitName)) return true;
        }
        return false;
    }

    /**
     * Enable dragging for gates
     */
    private void enableDrag(JComponent comp) {
        DragListener dl = new DragListener();
        comp.addMouseListener(dl);
        comp.addMouseMotionListener(dl);
    }

    /**
     * Dragging behavior
     */
    private class DragListener extends MouseAdapter {
        private Point pressed;
        private Component target;

        @Override
        public void mousePressed(MouseEvent e) {
            target = e.getComponent();
            pressed = SwingUtilities.convertPoint(target, e.getPoint(), target.getParent());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (target == null || pressed == null) return;

            Point now = SwingUtilities.convertPoint(target, e.getPoint(), target.getParent());
            int dx = now.x - pressed.x;
            int dy = now.y - pressed.y;

            Rectangle bounds = target.getBounds();
            bounds.translate(dx, dy);

            // Keep inside parent
            int pw = target.getParent().getWidth();
            int ph = target.getParent().getHeight();
            if (bounds.x < 0) bounds.x = 0;
            if (bounds.y < 0) bounds.y = 0;
            if (bounds.x + bounds.width > pw) bounds.x = Math.max(0, pw - bounds.width);
            if (bounds.y + bounds.height > ph) bounds.y = Math.max(0, ph - bounds.height);

            target.setBounds(bounds);
            
            // Update gate position
            if (target instanceof DrawGates) {
                DrawGates gateUI = (DrawGates) target;
                if (gateUI.getGate() != null) {
                    gateUI.getGate().setPosition(bounds.x, bounds.y);
                    // Update wire connections only if circuit exists
                    if (currentCircuit != null) {
                        updateWireConnections();
                    }
                }
            }

            target.getParent().repaint();
            pressed = now;
        }
    }

    /**
     * Mouse adapter for connection mode
     */
    private class ConnectionMouseAdapter extends MouseAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            if (isConnecting && sourceGate != null) {
                tempWireEnd = e.getPoint();
                repaint();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            // Cancel connection if clicking on empty space
            if (isConnecting && e.getClickCount() == 2) {
                isConnecting = false;
                sourceGate = null;
                sourcePort = -1;
                tempWireEnd = null;
                repaint();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawGridBackground(g2d);
        
        // Draw temporary wire during connection using controller
        if (controller != null) {
            controller.paintTemporaryConnection(g);
        }
        
        // Also draw using old method for backward compatibility
        if (isConnecting && sourceGate != null && tempWireEnd != null) {
            Point sourcePoint = sourceGate.getOutputPortAbsolutePosition(sourcePort);
            
            // Glowing wire effect
            g2d.setColor(new Color(100, 150, 255, 200));
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(sourcePoint.x, sourcePoint.y, tempWireEnd.x, tempWireEnd.y);
            
            // Main wire
            g2d.setColor(ACCENT_COLOR);
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(sourcePoint.x, sourcePoint.y, tempWireEnd.x, tempWireEnd.y);
        }
        
        // Draw LED status indicators in corner for quick overview
        drawLEDStatusPanel(g2d);
    }

    /**
     * Draw LED status panel in corner for quick overview
     */
    private void drawLEDStatusPanel(Graphics2D g2d) {
        List<LED> leds = getLEDs();
        if (leds.isEmpty()) return;
        
        int panelX = getWidth() - 150;
        int panelY = 10;
        int panelWidth = 140;
        int panelHeight = 30 + leds.size() * 20;
        
        // Draw background
        g2d.setColor(new Color(36, 41, 54, 200));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);
        g2d.setColor(BORDER_COLOR);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);
        
        // Draw title
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.drawString("LED Status", panelX + 10, panelY + 15);
        
        // Draw LED states
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (int i = 0; i < leds.size(); i++) {
            LED led = leds.get(i);
            int ledY = panelY + 30 + i * 20;
            
            // Draw LED indicator
            Color ledColor = led.isLit() ? Color.GREEN : Color.RED;
            g2d.setColor(ledColor);
            g2d.fillOval(panelX + 10, ledY - 5, 8, 8);
            
            // Draw glow effect for lit LEDs
            if (led.isLit()) {
                g2d.setColor(new Color(50, 255, 50, 80));
                g2d.fillOval(panelX + 8, ledY - 7, 12, 12);
            }
            
            // Draw LED label
            g2d.setColor(TEXT_COLOR);
            g2d.drawString("LED " + (i + 1) + ": " + (led.isLit() ? "ON" : "OFF"), panelX + 25, ledY + 2);
        }
    }

    /**
     * Draw circuit component as a rectangle with input/output pins
     * This is the EXACT SAME METHOD that makes CircuitPanel work
     */
    private void drawCircuitComponent(Graphics graphics, SubcircuitComponent circuitComp) {
        Point pos = circuitComp.getPosition();
        
        // Draw as a rectangle representing the circuit
        graphics.setColor(new Color(200, 230, 255)); // Light blue for circuit components
        graphics.fillRect(pos.x, pos.y, 120, 80);
        graphics.setColor(Color.BLUE);
        graphics.drawRect(pos.x, pos.y, 120, 80);
        
        // Draw circuit name
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Arial", Font.BOLD, 12));
        String displayName = circuitComp.getCircuitName();
        if (displayName.length() > 10) {
            displayName = displayName.substring(0, 10) + "...";
        }
        graphics.drawString(displayName, pos.x + 10, pos.y + 20);
        
        // Draw input pins on left side - use ACTUAL input count
        int inputCount = circuitComp.getInputs();
        for (int i = 0; i < inputCount; i++) {
            int pinY = pos.y + 25 + (i * 15);
            graphics.setColor(Color.RED);
            graphics.fillRect(pos.x - 5, pinY, 5, 3);
            graphics.setColor(Color.BLACK);
            graphics.setFont(new Font("Arial", Font.PLAIN, 10));
            graphics.drawString("In" + i, pos.x - 25, pinY + 3);
        }
        
        // Draw output pins on right side - use ACTUAL output count
        int outputCount = circuitComp.getOutputs();
        for (int i = 0; i < outputCount; i++) {
            int pinY = pos.y + 25 + (i * 15);
            graphics.setColor(Color.GREEN);
            graphics.fillRect(pos.x + 120, pinY, 5, 3);
            graphics.setColor(Color.BLACK);
            graphics.setFont(new Font("Arial", Font.PLAIN, 10));
            graphics.drawString("Out" + i, pos.x + 125, pinY + 3);
        }
    }

    /**
     * Draw modern grid background
     */
    private void drawGridBackground(Graphics2D g2d) {
        // Save original stroke
        Stroke originalStroke = g2d.getStroke();
        
        // Draw minor grid lines (more subtle)
        g2d.setColor(GRID_LINE_COLOR);
        g2d.setStroke(new BasicStroke(0.5f));
        
        // Vertical lines
        for (int x = GRID_SPACING; x < getWidth(); x += GRID_SPACING) {
            g2d.drawLine(x, 0, x, getHeight());
        }
        
        // Horizontal lines
        for (int y = GRID_SPACING; y < getHeight(); y += GRID_SPACING) {
            g2d.drawLine(0, y, getWidth(), y);
        }
        
        // Draw major grid lines (slightly more visible)
        g2d.setColor(GRID_MAJOR_COLOR);
        g2d.setStroke(new BasicStroke(1.0f));
        
        // Vertical major lines
        for (int x = MAJOR_GRID_SPACING; x < getWidth(); x += MAJOR_GRID_SPACING) {
            g2d.drawLine(x, 0, x, getHeight());
        }
        
        // Horizontal major lines
        for (int y = MAJOR_GRID_SPACING; y < getHeight(); y += MAJOR_GRID_SPACING) {
            g2d.drawLine(0, y, getWidth(), y);
        }
        
        // Restore original stroke
        g2d.setStroke(originalStroke);
    }

    public static DesignArea getInstance() {
        return instance;
    }
}