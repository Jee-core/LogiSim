package org.yourcompany.yourproject.frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.yourcompany.yourproject.backend.businessLayer.components.Circuit;
import org.yourcompany.yourproject.backend.businessLayer.components.GateComponent;
import org.yourcompany.yourproject.backend.businessLayer.components.gates.LED;
import org.yourcompany.yourproject.backend.businessLayer.components.Connector;

/**
 * Controller for managing canvas interactions.
 * Handles gate selection, dragging, connections, inputs, and outputs.
 */
public class Board {

    private BoardHelper viewService;
    private Circuit currentCircuit;
    private JPanel canvas;

    // Drag state
    private Point dragStartPoint;
    private DrawGates draggedGate;
    private boolean isDragging;

    public Board(JPanel canvas, BoardHelper viewService) {
        this.canvas = canvas;
        this.viewService = viewService;
        this.isDragging = false;
        setupMouseListeners();
    }

    public void setCurrentCircuit(Circuit circuit) {
        this.currentCircuit = circuit;
        if (circuit != null) {
            loadCircuitToView(circuit);
        }
    }

    public Circuit getCurrentCircuit() {
        return currentCircuit;
    }

    private void loadCircuitToView(Circuit circuit) {
        viewService.clearCanvas();
        int i = 0;
        while (i < circuit.getGates().size()) {
            GateComponent gate = circuit.getGates().get(i);
            Point pos = gate.getPosition();
            int x = calculateInitialX(pos);
            int y = calculateInitialY(pos);
            DrawGates gateView = viewService.addGateView(gate, x, y);
            if (gateView != null) {
                setupGateListeners(gateView);
            }
            i++;
        }
        updateWireViews();
    }
      
/**
 * Repaint the circuit panel to refresh visual display
 * This method is called by CircuitInspector to update LED states
 */
public void repaintCircuit() {
    if (canvas != null) {
        canvas.repaint();
    }
}
    private int calculateInitialX(Point pos) {
        return (pos.x == 0 && pos.y == 0) ? 50 : pos.x;
    }

    private int calculateInitialY(Point pos) {
        return (pos.x == 0 && pos.y == 0) ? 50 : pos.y;
    }

    private void setupMouseListeners() {
        setupCanvasMouseListeners();
        setupConnectionUpdateListener();
    }

    private void setupCanvasMouseListeners() {
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { handleCanvasMousePressed(e); }
            @Override
            public void mouseReleased(MouseEvent e) { handleCanvasMouseReleased(e); }
            @Override
            public void mouseClicked(MouseEvent e) { handleCanvasMouseClicked(e); }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) { handleCanvasMouseMoved(e); }
            @Override
            public void mouseDragged(MouseEvent e) { handleCanvasMouseDragged(e); }
        });
    }

    private void setupConnectionUpdateListener() {
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (viewService.isConnecting()) {
                    viewService.updateConnectionEnd(e.getPoint());
                }
            }
        });
    }

    private void setupGateListeners(DrawGates gateView) {
        gateView.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { handleGateMousePressed(gateView, e); }
            @Override
            public void mouseReleased(MouseEvent e) { handleGateMouseReleased(gateView, e); }
            @Override
            public void mouseClicked(MouseEvent e) { handleGateMouseClicked(gateView, e); }
        });

        gateView.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) { handleGateMouseDragged(gateView, e); }
        });
    }

    private void handleCanvasMousePressed(MouseEvent e) {
        if (viewService.isConnecting() && e.getClickCount() == 2) {
            viewService.endConnection();
            viewService.selectGate(null);
        }
    }

    private void handleCanvasMouseReleased(MouseEvent e) {
        handleConnectionRelease(e);
        handleDragCompletion();
    }

    private void handleConnectionRelease(MouseEvent e) {
        if (viewService.isConnecting()) {
            processConnectionRelease(e);
        }
    }

    private void processConnectionRelease(MouseEvent e) {
        Point releasePoint = e.getPoint();
        DrawGates targetGate = viewService.getGateAt(releasePoint);
        DrawGates sourceGate = viewService.getSourceGateForConnection();

        if (isValidConnectionTarget(targetGate, sourceGate)) {
            Point localPoint = SwingUtilities.convertPoint(canvas, releasePoint, targetGate);
            int inputPort = targetGate.getInputPortAt(localPoint);
            if (isValidInputPort(inputPort, targetGate, sourceGate)) {
                createConnection(
                    sourceGate,
                    viewService.getSourcePortForConnection(),
                    targetGate,
                    inputPort
                );
            }
        }
        viewService.endConnection();
    }

    private boolean isValidConnectionTarget(DrawGates target, DrawGates source) {
        return target != null && source != null && target != source;
    }

    private boolean isValidInputPort(int port, DrawGates target, DrawGates source) {
        return port >= 0 && target != source;
    }

    private void handleDragCompletion() {
        if (isDragging && draggedGate != null) {
            saveGatePosition();
            resetDragState();
        }
    }

    private void saveGatePosition() {
        Point location = draggedGate.getLocation();
        draggedGate.getGate().setPosition(location.x, location.y);
        //updateWireViews();
    }

    private void resetDragState() {
        isDragging = false;
        draggedGate = null;
    }

    private void handleCanvasMouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1) {
            handleSingleCanvasClick(e);
        }
    }

    private void handleSingleCanvasClick(MouseEvent e) {
        Point clickPoint = e.getPoint();
        DrawGates gateAtPoint = viewService.getGateAt(clickPoint);
        
        if (gateAtPoint == null) {
            clearSelectionAndConnection();
        }
    }

    private void clearSelectionAndConnection() {
        if (viewService.isConnecting()) {
            viewService.endConnection();
        }
        viewService.selectGate(null);
    }

    private void handleCanvasMouseMoved(MouseEvent e) {
        if (viewService.isConnecting()) {
            viewService.updateConnectionEnd(e.getPoint());
        }
    }

    private void handleCanvasMouseDragged(MouseEvent e) {
        if (viewService.isConnecting()) {
            viewService.updateConnectionEnd(e.getPoint());
        }
    }

    private void handleGateMousePressed(DrawGates gateView, MouseEvent e) {
        initializeDrag(gateView, e);
        if (!viewService.isConnecting()) {
            viewService.selectGate(gateView);
        }
    }

    private void initializeDrag(DrawGates gateView, MouseEvent e) {
        isDragging = true;
        draggedGate = gateView;
        dragStartPoint = SwingUtilities.convertPoint(gateView, e.getPoint(), canvas);
    }

    private void handleGateMouseReleased(DrawGates gateView, MouseEvent e) {
        if (isDragging && draggedGate == gateView) {
            saveGatePosition();
            resetDragState();
        }
    }

    private void handleGateMouseClicked(DrawGates gateView, MouseEvent e) {
        switch (e.getClickCount()) {
            case 1 -> handleSingleGateClick(gateView);
            case 2 -> handleDoubleGateClick(gateView);
        }
    }

    private void handleSingleGateClick(DrawGates gateView) {
        if (!viewService.isConnecting()) {
            startConnectionFromGate(gateView);
        } else {
            finishConnectionToGate(gateView);
        }
    }

    private void handleDoubleGateClick(DrawGates gateView) {
        if (!viewService.isConnecting()) {
            viewService.selectGate(gateView);
        }
    }

    private void handleGateMouseDragged(DrawGates gateView, MouseEvent e) {
        if (isDragging && draggedGate == gateView) {
            dragGate(gateView, e);
        } else if (viewService.isConnecting()) {
            updateConnectionDuringDrag(gateView, e);
        }
    }

    private void updateConnectionDuringDrag(DrawGates gateView, MouseEvent e) {
        Point absPoint = SwingUtilities.convertPoint(gateView, e.getPoint(), canvas);
        viewService.updateConnectionEnd(absPoint);
    }

    private void dragGate(DrawGates gateView, MouseEvent e) {
        Point currentPoint = SwingUtilities.convertPoint(gateView, e.getPoint(), canvas);
        int deltaX = currentPoint.x - dragStartPoint.x;
        int deltaY = currentPoint.y - dragStartPoint.y;

        Rectangle newBounds = calculateNewBounds(gateView, deltaX, deltaY);
        gateView.setBounds(newBounds);
        dragStartPoint = currentPoint;
        canvas.repaint();
    }

    private Rectangle calculateNewBounds(DrawGates gateView, int deltaX, int deltaY) {
        Rectangle bounds = gateView.getBounds();
        bounds.translate(deltaX, deltaY);
        return constrainBoundsToCanvas(bounds);
    }

    private Rectangle constrainBoundsToCanvas(Rectangle bounds) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        bounds.x = Math.max(0, Math.min(bounds.x, canvasWidth - bounds.width));
        bounds.y = Math.max(0, Math.min(bounds.y, canvasHeight - bounds.height));
        
        return bounds;
    }
private void startConnectionFromGate(DrawGates gateView) {
    GateComponent gate = gateView.getGate();
    if (gate == null) return;

    int availableOutput = findAvailableOutputPort(gate);
    if (availableOutput < 0) {
        // SILENT FAILURE - No popup, just don't start connection
        System.out.println("No available output ports on " + gate.getName());
        return;
    }
    
    viewService.selectGate(gateView);
    Point startPoint = gateView.getOutputPortAbsolutePosition(availableOutput);
    viewService.startConnection(gateView, availableOutput, startPoint);
}

private void finishConnectionToGate(DrawGates gateView) {
    DrawGates sourceGate = viewService.getSourceGateForConnection();
    
    if (!isValidConnectionCompletion(sourceGate, gateView)) {
        viewService.endConnection();
        return;
    }

    int availableInput = findAvailableInputPort(gateView.getGate());
    if (availableInput < 0) {
        // SILENT FAILURE - No popup, just end connection
        System.out.println("No available input ports on " + gateView.getGate().getName());
        viewService.endConnection();
        return;
    }
    
    completeConnection(sourceGate, gateView, availableInput);
}

    private boolean isValidConnectionCompletion(DrawGates sourceGate, DrawGates targetGate) {
        return sourceGate != null && targetGate != sourceGate;
    }

    private void completeConnection(DrawGates sourceGate, DrawGates targetGate, int inputPort) {
        createConnection(
            sourceGate,
            viewService.getSourcePortForConnection(),
            targetGate,
            inputPort
        );
        viewService.endConnection();
        viewService.selectGate(targetGate);
    }

    private int findAvailableOutputPort(GateComponent gate) {
        if (gate == null) return -1;
        int i = 0;
        while (i < gate.getOutputs()) {
            if (gate.getOutputWire(i) == null) return i;
            i++;
        }
        return -1;
    }

    private int findAvailableInputPort(GateComponent gate) {
        if (gate == null) return -1;
        int i = 0;
        while (i < gate.getInputs()) {
            if (gate.getInputWire(i) == null) return i;
            i++;
        }
        return -1;
    }

    private void createConnection(DrawGates fromGate, int fromPort, DrawGates toGate, int toPort) {
        if (!validateConnectionParameters(fromGate, toGate)) {
            return;
        }

        GateComponent fromComponent = fromGate.getGate();
        GateComponent toComponent = toGate.getGate();
        
        if (!validateGateComponents(fromComponent, toComponent)) {
            return;
        }

        logConnectionAttempt(fromComponent, fromPort, toComponent, toPort);
        
        Connector wire = currentCircuit.gatesConnectorFunc(fromComponent, fromPort, toComponent, toPort);
        if (wire != null) {
            handleSuccessfulConnection();
        } else {
            logConnectionFailure();
        }
    }

    private boolean validateConnectionParameters(DrawGates fromGate, DrawGates toGate) {
        if (currentCircuit == null || fromGate == null || toGate == null) {
            System.out.println("Connection failed: circuit or gates null");
            return false;
        }
        return true;
    }

    private boolean validateGateComponents(GateComponent fromComponent, GateComponent toComponent) {
        if (fromComponent == null || toComponent == null) {
            System.out.println("Connection failed: gate components null");
            return false;
        }
        return true;
    }

    private void logConnectionAttempt(GateComponent fromComponent, int fromPort, GateComponent toComponent, int toPort) {
        System.out.println("Attempting to connect: " + fromComponent.getName() + " port " + fromPort +
                " to " + toComponent.getName() + " port " + toPort);
    }

    private void handleSuccessfulConnection() {
        System.out.println("Connection created successfully!");
        updateWireViews();
        propagateSignals();
        canvas.repaint();
    }

    private void logConnectionFailure() {
        System.out.println("Connection creation returned null - port may already be connected");
    }

    public boolean addGate(GateComponent gate, int x, int y) {
        if (!validateCircuitExists()) {
            return false;
        }
        
        if (gate == null) return false;

        x = Math.max(x, 50);
        y = Math.max(y, 50);

        gate.setPosition(x, y);
        currentCircuit.addGateFunc(gate);

        DrawGates gateView = viewService.addGateView(gate, x, y);
        if (gateView != null) {
            setupGateListeners(gateView);
            updateWireViews();
            return true;
        }
        return false;
    }

    private boolean validateCircuitExists() {
        if (currentCircuit == null) {
            showNoCircuitMessage();
            return false;
        }
        return true;
    }

    public void removeGate(DrawGates gateView) {
        if (gateView == null || currentCircuit == null) return;

        GateComponent gate = gateView.getGate();
        if (gate != null) {
            currentCircuit.removeGate(gate);
            viewService.removeGateView(gateView);
            //updateWireViews();
        }
    }

    private void updateWireViews() {
        if (currentCircuit != null) {
            currentCircuit.updateConnectorPos();
            viewService.updateWireViews(currentCircuit.getWires());
        }
    }

   

    public void setGateInput(GateComponent gate, int inputIndex, boolean value) {
        if (gate != null) {
            gate.setInputVal(inputIndex, value);
            propagateSignals();
            canvas.repaint();
        }
    }

    public boolean getGateOutput(GateComponent gate, int outputIndex) {
        if (gate != null) return gate.getOutputVal(outputIndex);
        return false;
    }

    public void clearCanvas() {
        viewService.clearCanvas();
        if (currentCircuit != null) currentCircuit.clear();
    }

    public DrawGates getSelectedGate() {
        return viewService.getSelectedGate();
    }

    public void paintTemporaryConnection(Graphics g) {
        viewService.paintTemporaryConnection(g);
    }
    

    private CircuitInspector circuitInspector; // Add this field

  

    public void setCircuitInspector(CircuitInspector inspector) {
        this.circuitInspector = inspector;
    }

    /**
     * Enhanced propagateSignals that updates ALL visuals
     */
public void refreshEntireCircuit() {
    System.out.println("🔄 MANUAL REFRESH TRIGGERED");
    propagateSignals();
}
public void propagateSignals() {
    if (currentCircuit != null) {
        System.out.println("=== FORCING CIRCUIT RECOMPUTATION ===");
        
        // METHOD 1: Try the circuit's propagation
        currentCircuit.sigPropogation();
        
        // METHOD 2: Manual propagation - force EVERY gate to recompute
        for (GateComponent gate : currentCircuit.getGates()) {
            System.out.println("Processing: " + gate.getName() + " (Type: " + gate.getClass().getSimpleName() + ")");
            
            // Force input refresh from connected gates
            for (int i = 0; i < gate.getInputs(); i++) {
                Connector inputConnector = gate.getInputWire(i);
                if (inputConnector != null) {
                    GateComponent sourceGate = inputConnector.getFromGate();
                    if (sourceGate != null) {
                        // Get the actual value from the source gate
                        boolean sourceValue = sourceGate.getOutputVal(inputConnector.getSourcePortIndex());
                        gate.setInputVal(i, sourceValue);
                        System.out.println("  Input " + i + " connected to " + sourceGate.getName() + ":" + inputConnector.getSourcePortIndex() + " = " + sourceValue);
                    }
                } else {
                    System.out.println("  Input " + i + ": NOT CONNECTED = " + gate.getInputVal(i));
                }
            }
            
            // Force output computation
            gate.computeOutput();
            
            // Print results
            System.out.print("  Inputs: [");
            for (int i = 0; i < gate.getInputs(); i++) {
                System.out.print(gate.getInputVal(i) ? "1" : "0");
                if (i < gate.getInputs() - 1) System.out.print(", ");
            }
            System.out.print("] -> Outputs: [");
            for (int i = 0; i < gate.getOutputs(); i++) {
                System.out.print(gate.getOutputVal(i) ? "1" : "0");
                if (i < gate.getOutputs() - 1) System.out.print(", ");
            }
            System.out.println("]");
            
            // Special LED handling
            if (gate instanceof LED) {
                LED led = (LED) gate;
                System.out.println("  LED STATE: " + (led.isLit() ? "LIT" : "OFF"));
            }
        }
        
        // Update visuals
        viewService.forceCircuitVisualUpdate();
        
        // Notify inspector
        if (circuitInspector != null) {
            circuitInspector.onCircuitStateChanged();
        }
        
        canvas.repaint();
        System.out.println("=== RECOMPUTATION COMPLETE ===");
    }
}
    /**
     * Enhanced refresh method for the entire circuit
     */
   
    // ======================
    // Custom black dialog boxes
    // ======================
    private void showOutputFullMessage() {
        showCustomMessage("Oops! No outputs available on this gate.", "Output Full", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showInputFullMessage() {
        showCustomMessage("Oops! No inputs available on this gate.", "Input Full", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showNoCircuitMessage() {
        showCustomMessage(
            "Please select a project and open or create a circuit first.",
            "No Circuit Selected",
            JOptionPane.WARNING_MESSAGE
        );
    }

    private void showCustomMessage(String message, String title, int messageType) {
    // Create a completely custom dialog
    JDialog dialog = new JDialog((Frame) null, title, true);
    dialog.setLayout(new BorderLayout());
    dialog.getContentPane().setBackground(Color.BLACK);
    
    // Message label
    JLabel messageLabel = new JLabel("<html><div style='text-align: center; padding: 20px; color: white;'>" + message + "</div></html>");
    messageLabel.setForeground(Color.WHITE);
    messageLabel.setBackground(Color.BLACK);
    messageLabel.setOpaque(true);
    messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
    
    // OK button
    JButton okButton = new JButton("OK");
    okButton.setBackground(Color.BLACK);
    okButton.setForeground(Color.BLACK);
    okButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
    okButton.setFocusPainted(false);
    okButton.setPreferredSize(new Dimension(80, 30));
    
    okButton.addActionListener(e -> dialog.dispose());
    
    // Button panel
    JPanel buttonPanel = new JPanel();
    buttonPanel.setBackground(Color.BLACK);
    buttonPanel.add(okButton);
    
    // Add components to dialog
    dialog.add(messageLabel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);
    
    // Size and display
    dialog.pack();
    dialog.setLocationRelativeTo(canvas);
    dialog.setVisible(true);

}
}
