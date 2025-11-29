package org.yourcompany.yourproject.frontend;

import javax.swing.*;

import org.yourcompany.yourproject.backend.businessLayer.components.GateComponent;
import org.yourcompany.yourproject.backend.businessLayer.components.gates.LED;
import org.yourcompany.yourproject.brontend.DrawGates;
import org.yourcompany.yourproject.frontend.DrawWire;
import org.yourcompany.yourproject.backend.businessLayer.components.Connector;


import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * View Service for managing the canvas view and visual representation
 * Handles rendering, gate selection, visual feedback, and input/output display
 */
public class BoardHelper {

    private JPanel canvas;
    private List<DrawGates> gateViews;
    private List<DrawWire> wireViews;
    private DrawGates selectedGate;
    private DrawGates sourceGateForConnection;
    private int sourcePortForConnection;
    private Point tempWireEnd;
    private boolean isConnecting;
    private GateSelectionListener gateSelectionListener;

    // UI colors matching the current scheme
    private static final Color CANVAS_BG = new Color(245, 250, 255); // Very light blue-white
    private static final Color BORDER_COLOR = new Color(70, 130, 180); // Steel blue
    private static final Color SELECTION_COLOR = new Color(255, 215, 0); // Gold for selection

    public BoardHelper(JPanel canvas) {
        this.canvas = canvas;
        this.gateViews = new ArrayList<>();
        this.wireViews = new ArrayList<>();
        this.selectedGate = null;
        this.isConnecting = false;
        initializeCanvas();
    }

    /**
     * Initialize the canvas with proper styling
     */
    private void initializeCanvas() {
        canvas.setLayout(null);
        canvas.setBackground(CANVAS_BG);
        canvas.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                "Circuit Design Area",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13),
                BORDER_COLOR));
    }

    /**
     * Add a gate to the canvas view (GateComponent version)
     */
    public DrawGates addGateView(GateComponent gate, int x, int y) {
        if (gate == null)
            return null;

        DrawGates gateView = new DrawGates(gate);
        gateView.setLocation(x, y);
        canvas.add(gateView);
        gateViews.add(gateView);

        // Set z-order to ensure gates are above wires
        canvas.setComponentZOrder(gateView, 0);
        for (DrawWire wire : wireViews) {
            canvas.setComponentZOrder(wire, wireViews.size());
        }

        canvas.revalidate();
        canvas.repaint();
        return gateView;
    }

    /**
     * Remove a gate from the canvas view
     */
    public void removeGateView(DrawGates gateView) {
        if (gateView != null) {
            canvas.remove(gateView);
            gateViews.remove(gateView);
            if (selectedGate == gateView) {
                selectedGate = null;
            }
            canvas.revalidate();
            canvas.repaint();
        }
    }

    /**
     * Select a gate (visual feedback)
     */
    public void selectGate(DrawGates gate) {
        // Deselect previous
        if (selectedGate != null) {
            selectedGate.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 2),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        }

        // Select new
        selectedGate = gate;
        if (gate != null) {
            gate.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(SELECTION_COLOR, 3),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        }

        if (gateSelectionListener != null) {
            // Call both listener versions for compatibility
            if (gate != null) {
                GateComponent gateComponent = gate.getGate();
              
                
                if (gateComponent != null) {
                    gateSelectionListener.onGateSelected(gateComponent);
                }
              
                
            } else {
                gateSelectionListener.onGateSelected((GateComponent) null);
              
            }
        }
        canvas.repaint();
    }

    /**
     * Get the currently selected gate
     */
    public DrawGates getSelectedGate() {
        return selectedGate;
    }

    /**
     * Update wire connections in the view - Enhanced version
     */
    public void updateWireViews(List<Connector> connectors) {
        System.out.println("=== updateWireViews called ===");
        
        // Track deleted connectors to prevent recreation
        List<Connector> filteredConnectors = new ArrayList<>();
        for (Connector connector : connectors) {
            // Skip connectors that are marked as deleted or disconnected
            if (connector != null && connector.isConnected()) {
                filteredConnectors.add(connector);
            } else {
                System.out.println("Skipping deleted/disconnected connector: " + connector);
            }
        }
        
        System.out.println("Original connectors: " + connectors.size() + ", Filtered: " + filteredConnectors.size());
        
        // Remove old wire views that don't exist in filtered list
        List<DrawWire> wiresToRemove = new ArrayList<>();
        for (DrawWire existingWire : wireViews) {
            if (!filteredConnectors.contains(existingWire.getConnector())) {
                wiresToRemove.add(existingWire);
            }
        }
        
        // Remove the wires
        for (DrawWire wireToRemove : wiresToRemove) {
            canvas.remove(wireToRemove);
            wireViews.remove(wireToRemove);
            System.out.println("Removed UI wire: " + wireToRemove);
        }
        
        // Add UI wires ONLY for new connectors that don't have UI yet
        for (Connector connector : filteredConnectors) {
            boolean alreadyExists = false;
            for (DrawWire existingWire : wireViews) {
                if (existingWire.getConnector() == connector) {
                    alreadyExists = true;
                    break;
                }
            }
            
            if (!alreadyExists) {
                DrawWire wireView = new DrawWire(connector);
                wireView.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                canvas.add(wireView);
                wireViews.add(wireView);
                System.out.println("Created new UI wire for: " + connector);
            }
        }
        
        // Ensure wires are behind gates
        for (DrawWire wireView : wireViews) {
            canvas.setComponentZOrder(wireView, wireViews.size());
        }
        for (DrawGates gateView : gateViews) {
            canvas.setComponentZOrder(gateView, 0);
        }
        
        canvas.revalidate();
        canvas.repaint();
        
        System.out.println("Final wire count: " + wireViews.size());
    }

    /**
     * Remove a wire from the canvas view
     */
    public void removeWire(DrawWire wire) {
        if (wire != null) {
            canvas.remove(wire);
            wireViews.remove(wire);
            canvas.revalidate();
            canvas.repaint();
        }
    }

    /**
     * Start connection mode (dragging from output port)
     */
    public void startConnection(DrawGates sourceGate, int sourcePort, Point startPoint) {
        this.sourceGateForConnection = sourceGate;
        this.sourcePortForConnection = sourcePort;
        this.tempWireEnd = startPoint;
        this.isConnecting = true;
        canvas.repaint();
    }

    /**
     * Update temporary wire end during connection
     */
    public void updateConnectionEnd(Point endPoint) {
        if (isConnecting) {
            this.tempWireEnd = endPoint;
            canvas.repaint();
        }
    }

    /**
     * End connection mode
     */
    public void endConnection() {
        this.isConnecting = false;
        this.sourceGateForConnection = null;
        this.sourcePortForConnection = -1;
        this.tempWireEnd = null;
        canvas.repaint();
    }

    /**
     * Check if currently in connection mode
     */
    public boolean isConnecting() {
        return isConnecting;
    }

    /**
     * Get source gate for connection
     */
    public DrawGates getSourceGateForConnection() {
        return sourceGateForConnection;
    }

    /**
     * Get source port for connection
     */
    public int getSourcePortForConnection() {
        return sourcePortForConnection;
    }

    /**
     * Paint temporary connection wire
     */
    public void paintTemporaryConnection(Graphics g) {
        if (isConnecting && sourceGateForConnection != null && tempWireEnd != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Point sourcePoint = sourceGateForConnection.getOutputPortAbsolutePosition(sourcePortForConnection);

            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(sourcePoint.x, sourcePoint.y, tempWireEnd.x, tempWireEnd.y);
        }
    }

    /**
     * Get gate view at a specific point
     */
    public DrawGates getGateAt(Point point) {
        for (DrawGates gateView : gateViews) {
            Rectangle bounds = gateView.getBounds();
            if (bounds.contains(point)) {
                return gateView;
            }
        }
        return null;
    }

    /**
     * Clear all gates and wires from the canvas
     */
    public void clearCanvas() {
        for (DrawGates gate : gateViews) {
            canvas.remove(gate);
        }
        for (DrawWire wire : wireViews) {
            canvas.remove(wire);
        }
        gateViews.clear();
        wireViews.clear();
        selectedGate = null;
        canvas.revalidate();
        canvas.repaint();
    }

    /**
     * Get all gate views
     */
    public List<DrawGates> getGateViews() {
        return new ArrayList<>(gateViews);
    }

    /**
     * Update gate position in view
     */
    public void updateGatePosition(DrawGates gateView, int x, int y) {
        if (gateView != null) {
            gateView.setLocation(x, y);
            canvas.repaint();
        }
    }

    /**
     * NEW: Update LED states when circuit simulation runs
     * This ensures LEDs reflect their current input values visually
     */
    public void updateLEDStates() {
        for (DrawGates gateView : gateViews) {
            if (gateView.getGate() instanceof LED) {
                // Force the LED to repaint and show current state
                gateView.repaint();
            }
        }
        canvas.repaint();
    }

    /**
     * NEW: Force update of all gate visuals
     * Useful after circuit evaluation to refresh all visual states
     */
    public void updateAllGateVisuals() {
        for (DrawGates gateView : gateViews) {
            gateView.repaint();
        }
        canvas.repaint();
    }

    /**
     * NEW: Get all LED components in the circuit
     */
    public List<LED> getLEDs() {
        List<LED> leds = new ArrayList<>();
        for (DrawGates gateView : gateViews) {
            if (gateView.getGate() instanceof LED) {
                leds.add((LED) gateView.getGate());
            }
        }
        return leds;
    }

    /**
     * NEW: Check if any LED is currently lit
     * Useful for testing and debugging
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
     * NEW: Get LED status summary for display
     */
    public String getLEDStatusSummary() {
        List<LED> leds = getLEDs();
        if (leds.isEmpty()) {
            return "No LEDs in circuit";
        }
        
        int litCount = 0;
        for (LED led : leds) {
            if (led.isLit()) {
                litCount++;
            }
        }
        
        return String.format("LEDs: %d/%d LIT", litCount, leds.size());
    }

    /**
     * Show input/output dialog for a gate (GateComponent version)
     */
    public void showInputOutputDialog(GateComponent gate) {
        if (gate == null)
            return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(canvas),
                "Gate: " + gate.getName(), true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(CANVAS_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // NEW: Special handling for LED - show visual state
        if (gate instanceof LED) {
            LED led = (LED) gate;
            
            // LED status display
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            JLabel ledStatusLabel = new JLabel("LED STATUS: " + (led.isLit() ? "LIT (GREEN)" : "OFF (RED)"));
            ledStatusLabel.setForeground(led.isLit() ? Color.GREEN : Color.RED);
            ledStatusLabel.setFont(new Font("Arial", Font.BOLD, 14));
            contentPanel.add(ledStatusLabel, gbc);
            
            gbc.gridy++;
            JLabel infoLabel = new JLabel("LED lights up when input signal is HIGH");
            infoLabel.setForeground(BORDER_COLOR);
            infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
            contentPanel.add(infoLabel, gbc);
            
            gbc.gridwidth = 1;
            gbc.gridy++;
        }

        // Inputs section
        gbc.gridx = 0;
        gbc.gridy++;
        contentPanel.add(new JLabel("Inputs:"), gbc);

        List<JCheckBox> inputBoxes = new ArrayList<>();
        for (int i = 0; i < gate.getInputs(); i++) {
            final int index = i;
            gbc.gridy++;
            JLabel label = new JLabel("Input " + (index + 1) + ":");
            label.setForeground(BORDER_COLOR);
            contentPanel.add(label, gbc);

            gbc.gridx = 1;
            JCheckBox checkbox = new JCheckBox();
            checkbox.setSelected(gate.getInputVal(index));
            Connector inputConnector = gate.getInputWire(index);
            boolean isPortConnected = inputConnector != null;
            checkbox.setEnabled(!isPortConnected);
            checkbox.addActionListener(e -> {
                gate.setInputVal(index, checkbox.isSelected());
                canvas.repaint(); // Update port colors
                
                // NEW: Special handling for LED - update visual state immediately
                if (gate instanceof LED) {
                    ((LED) gate).computeOutput();
                    updateLEDStates();
                }
            });
            inputBoxes.add(checkbox);
            contentPanel.add(checkbox, gbc);

            gbc.gridx = 2;
            if (isPortConnected) {
                JLabel statusLabel = new JLabel("Port already connected");
                statusLabel.setForeground(Color.RED.darker());
                contentPanel.add(statusLabel, gbc);
            } else {
                contentPanel.add(Box.createHorizontalStrut(10), gbc);
            }

            gbc.gridx = 0;
        }

        // Outputs section (LED has no outputs)
        if (!(gate instanceof LED)) {
            gbc.gridy++;
            gbc.gridx = 0;
            contentPanel.add(new JLabel("Outputs:"), gbc);

            for (int i = 0; i < gate.getOutputs(); i++) {
                gbc.gridy++;
                JLabel label = new JLabel("Output " + (i + 1) + ":");
                label.setForeground(BORDER_COLOR);
                contentPanel.add(label, gbc);

                gbc.gridx = 1;
                JLabel outputLabel = new JLabel(gate.getOutputVal(i) ? "HIGH" : "LOW");
                outputLabel.setForeground(gate.getOutputVal(i) ? Color.GREEN : Color.RED);
                outputLabel.setFont(new Font("Arial", Font.BOLD, 12));
                contentPanel.add(outputLabel, gbc);
                gbc.gridx = 0;
            }
        }

        // Close button
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton closeButton = new JButton("Close");
        closeButton.setBackground(new Color(173, 216, 230));
        closeButton.setForeground(BORDER_COLOR);
        closeButton.addActionListener(e -> dialog.dispose());
        contentPanel.add(closeButton, gbc);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.setLocationRelativeTo(canvas);
        dialog.setVisible(true);
    }

    /**
     * Repaint the canvas
     */
    public void repaint() {
        canvas.repaint();
    }

    /**
     * Register listener for gate selection changes
     */
    public void setGateSelectionListener(GateSelectionListener listener) {
        this.gateSelectionListener = listener;
    }

    public interface GateSelectionListener {
        void onGateSelected(GateComponent gate);
    }
    /**
 * NEW: Refresh all gate views to reflect current state after circuit evaluation
 */
public void refreshAllGateViews() {
    System.out.println("DEBUG: Refreshing all gate views...");
    
    for (DrawGates gateView : gateViews) {
        // Force each gate to update its visual state
        gateView.updateGateState();
        gateView.repaint();
        
        // Special handling for LEDs
        if (gateView.getGate() instanceof LED) {
            LED led = (LED) gateView.getGate();
            led.computeOutput(); // Recompute LED state
            System.out.println("DEBUG: Updated LED " + led.getName() + " - Lit: " + led.isLit());
        }
    }
    
    // Update wire visuals
    for (DrawWire wireView : wireViews) {
        wireView.repaint();
    }
    
    canvas.repaint();
    System.out.println("DEBUG: All gate views refreshed. Total gates: " + gateViews.size());
}

/**
 * NEW: Force complete circuit visual update
 */
public void forceCircuitVisualUpdate() {
    System.out.println("DEBUG: Force updating circuit visuals...");
    
    // Update all gates
    refreshAllGateViews();
    
    // Special handling for signal propagation visualization
    updateSignalPropagationVisuals();
    
    canvas.repaint();
}

/**
 * NEW: Update signal propagation colors on wires
 */
private void updateSignalPropagationVisuals() {
    for (DrawWire wireView : wireViews) {
        wireView.repaint(); // This will make wires show current signal state
    }
}

/**
 * NEW: Get gate view by GateComponent
 */
public DrawGates getGateView(GateComponent gate) {
    for (DrawGates gateView : gateViews) {
        if (gateView.getGate() == gate) {
            return gateView;
        }
    }
    return null;
}
}