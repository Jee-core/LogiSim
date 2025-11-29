package org.yourcompany.yourproject.backend.businessLayer.components;

import java.awt.Color;
import java.awt.Point;
import org.yourcompany.yourproject.backend.businessLayer.components.GateComponent;

public class Connector {
    private String name;
    private Color color;
    private Point source;
    private Point sink;
    private Point position;

    private GateComponent sourceGate;
    private GateComponent destGate;

    private int sourcePort;
    private int destPort;

    private Boolean signalVal; // Current signal value on this wire
    private String id; // Unique identifier for the wire

    // Reference to UI components for getting accurate port positions
    private transient Object fromGateUI;
    private transient Object toGateUI;

    // ===== CONSTRUCTORS =====

    public Connector(String name, Color color, Point source, Point sink) {
        this.name = name;
        this.color = color != null ? color : Color.BLACK;
        this.source = source;
        this.sink = sink;
        this.signalVal = false;
        this.id = generateId();
    }

    public Connector(Connector other, GateComponent newFrom, GateComponent newTo) {
        this("Wire", other.getColor(), null, null);
        this.sourceGate = newFrom;
        this.sourcePort = other.getSourcePortIndex();
        this.destGate = newTo;
        this.destPort = other.getDestinationPortIndex();
        recalculateWirePositions();

        if (newFrom != null)
            newFrom.attachOutputConnector(sourcePort, this);
        if (newTo != null)
            newTo.attachInputConnector(destPort, this);
    }

    public Connector(GateComponent fromGate, int fromPort, GateComponent toGate, int toPort) {
        this("Wire", Color.BLACK, null, null);
        this.sourceGate = fromGate;
        this.sourcePort = fromPort;
        this.destGate = toGate;
        this.destPort = toPort;
        recalculateWirePositions();
        // Connect the wire to the gates
        if (fromGate != null) {
            fromGate.attachOutputConnector(fromPort, this);
        }
        if (toGate != null) {
            toGate.attachInputConnector(toPort, this);
        }
    }

    // New constructor that accepts UI components
    public Connector(GateComponent fromGate, int fromPort, GateComponent toGate, int toPort,
            Object fromGateUI, Object toGateUI) {
        this(fromGate, fromPort, toGate, toPort);
        this.fromGateUI = fromGateUI;
        this.toGateUI = toGateUI;
        recalculateWirePositions();
    }

    private String generateId() {
        return "Wire_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }

    // ===== CONNECTION POINT MANAGEMENT =====

    // Update source and sink points based on connected gates - FIXED VERSION
    public void recalculateWirePositions() {
        if (sourceGate != null && sourcePort >= 0) {
            // Try to get position from UI component first
            if (fromGateUI instanceof org.yourcompany.yourproject.frontend.DrawGates) {
                org.yourcompany.yourproject.frontend.DrawGates uiGate = (org.yourcompany.yourproject.frontend.DrawGates) fromGateUI;
                this.source = uiGate.getOutputPortAbsolutePosition(sourcePort);
            } else {
                // Fallback to business layer method
                this.source = sourceGate.calculateOutputPortLocation(sourcePort);
            }
        }

        if (destGate != null && destPort >= 0) {

            // Try to get position from UI component first
            if (toGateUI instanceof org.yourcompany.yourproject.frontend.DrawGates) {
                org.yourcompany.yourproject.frontend.DrawGates uiGate = (org.yourcompany.yourproject.frontend.DrawGates) toGateUI;
                this.sink = uiGate.getInputPortAbsolutePosition(destPort);

            } else {
                // Fallback to business layer method
                this.sink = destGate.calculateInputPortLocation(destPort);
            }

        }
    }

    // ===== SIGNAL PROPAGATION =====

    // Propagate signal from source gate to destination gate
    public void transmitSig() {
        if (sourceGate != null && destGate != null) {

            Boolean value = sourceGate.getOutputVal(sourcePort);
            this.signalVal = value;
            destGate.setInputVal(destPort, value);
        }
    }

    // ===== UI COMPONENT MANAGEMENT =====

    // Getters and setters for UI components
    public Object getFromUI() {
        return fromGateUI;
    }

    public void setFromUI(Object fromGateUI) {
        this.fromGateUI = fromGateUI;
        recalculateWirePositions();
    }

    public Object getToUI() {
        return toGateUI;
    }

    public void setToUI(Object toGateUI) {

        this.toGateUI = toGateUI;
        recalculateWirePositions();
    }

    // ===== BASIC PROPERTY ACCESSORS =====

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color != null ? color : Color.BLACK;
    }

    public Point getSource() {
        recalculateWirePositions(); // Ensure points are up to date
        return source;
    }

    public void setSource(Point source) {
        this.source = source;
    }

    public Point getSink() {
        recalculateWirePositions(); // Ensure points are up to date
        return sink;
    }

    public void setSink(Point sink) {
        this.sink = sink;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    // ===== GATE CONNECTION MANAGEMENT =====

    public GateComponent getFromGate() {
        return sourceGate;
    }

    public void setFromGate(GateComponent fromGate) {

        this.sourceGate = fromGate;
        recalculateWirePositions();
        if (fromGate != null && sourcePort >= 0) {
            fromGate.attachOutputConnector(sourcePort, this);
        }
    }

    public GateComponent getToGate() {
        return destGate;
    }

    public void setToGate(GateComponent toGate) {
        this.destGate = toGate;
        recalculateWirePositions();
        if (toGate != null && destPort >= 0) {
            toGate.attachInputConnector(destPort, this);
        }
    }

    public int getSourcePortIndex() {
        return sourcePort;
    }

    public void setSourcePortIndex(int fromPort) {
        this.sourcePort = fromPort;
        recalculateWirePositions();
        if (sourceGate != null) {
            sourceGate.attachOutputConnector(fromPort, this);
        }
    }

    public int getDestinationPortIndex() {
        return destPort;
    }

    public void setDestinationPortIndex(int toPort) {
        this.destPort = toPort;
        recalculateWirePositions();
        if (destGate != null) {
            destGate.attachInputConnector(toPort, this);
        }
    }

    public Boolean getSignalValue() {
        return signalVal;
    }

    public void setSignalValue(Boolean signalValue) {
        this.signalVal = signalValue;
    }

    public String getId() {
        return id;
    }

    // ===== CONNECTION STATUS AND MAINTENANCE =====

    // Disconnect this wire from both gates
    public void disconnectWire() {
        if (sourceGate != null && sourcePort >= 0) {
            Connector currentConnector = sourceGate.getOutputWire(sourcePort);
            if (currentConnector == this) {
                sourceGate.attachOutputConnector(sourcePort, null);
            }
        }
        if (destGate != null && destPort >= 0) {
            Connector currentConnector = destGate.getInputWire(destPort);
            if (currentConnector == this) {
                destGate.attachInputConnector(destPort, null);
            }
        }

        sourceGate = null;
        destGate = null;
        fromGateUI = null;
        toGateUI = null;
    }

    // Check if wire is properly connected
    public boolean isConnected() {
        return sourceGate != null && destGate != null;
    }
}