package org.yourcompany.yourproject.frontend;

import org.yourcompany.yourproject.backend.businessLayer.components.Circuit;
import org.yourcompany.yourproject.backend.businessLayer.components.GateComponent;
import org.yourcompany.yourproject.backend.businessLayer.components.Connector;

import javax.swing.*;

import java.awt.*;

public class DrawWire extends JComponent {
    private Connector connector;
    private DrawGates sourceGate;
    private DrawGates targetGate;
    private int sourcePort;
    private int targetPort;
    private static final Color HIGH_SIGNAL_COLOR = new Color(50, 168, 82); // Green for high signal
    private static final Color LOW_SIGNAL_COLOR = new Color(180, 0, 0); // Red for low signal
    private static final Color DEFAULT_COLOR = new Color(100, 100, 150); // Default wire color matching theme
    private boolean selected = false;

    public DrawWire(Connector connector, DrawGates sourceGate, int sourcePort,
            DrawGates targetGate, int targetPort) {
        this.connector = connector;
        this.sourceGate = sourceGate;
        this.targetGate = targetGate;
        this.sourcePort = sourcePort;
        this.targetPort = targetPort;
        setBounds(0, 0, 1200, 800);
        setOpaque(false);
    }

    public DrawWire(Connector connector) {
        this.connector = connector;
        setBounds(0, 0, 1200, 800);
        setOpaque(false);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    public boolean isSelected() {
        return selected;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
        repaint();
    }

    // Getters and setters for UI gates
    public DrawGates getSourceGate() {
        return sourceGate;
    }

    public void setSourceGate(DrawGates sourceGate) {
        this.sourceGate = sourceGate;
        repaint();
    }

    public DrawGates getTargetGate() {
        return targetGate;
    }

    public void setTargetGate(DrawGates targetGate) {
        this.targetGate = targetGate;
        repaint();
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
        repaint();
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
        repaint();
    }

    @Override
    public boolean contains(int x, int y) {
        // Don't intercept mouse events if connector is null
        if (connector == null)
            return false;

        Point source = null;
        Point sink = null;

        // Get positions from UI gates
        if (sourceGate != null && targetGate != null) {
            source = sourceGate.getOutputPortAbsolutePosition(sourcePort);
            sink = targetGate.getInputPortAbsolutePosition(targetPort);
        }
        // Fallback to connector's points
        else if (connector.getSource() != null && connector.getSink() != null) {
            source = connector.getSource();
            sink = connector.getSink();
        }

        if (source == null || sink == null)
            return false;

        // Check if point is near the wire line (within 5 pixels)
        return isPointNearLine(x, y, source.x, source.y, sink.x, sink.y, 5);
    }

    // Helper method to check if point is near a line segment
    private boolean isPointNearLine(int px, int py, int x1, int y1, int x2, int y2, double tolerance) {
        // Calculate the length of the line segment
        double lineLength = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        if (lineLength == 0)
            return false;

        // Calculate the distance from the point to the line segment
        double distance = Math.abs((y2 - y1) * px - (x2 - x1) * py + x2 * y1 - y2 * x1) / lineLength;

        // Also check if the point is within the bounding box of the line segment
        // This ensures we only detect points that are near the actual segment, not the
        // infinite line
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);

        // Expand the bounding box by tolerance
        boolean withinBoundingBox = (px >= minX - tolerance) && (px <= maxX + tolerance) &&
                (py >= minY - tolerance) && (py <= maxY + tolerance);

        return (distance <= tolerance) && withinBoundingBox;
    }

    private boolean isPainting = false;

    @Override
    protected void paintComponent(Graphics g) {
        // Check if wire is marked as deleted
        if (Boolean.TRUE.equals(getClientProperty("deleted"))) {
            return;
        }

        // Prevent recursive painting
        if (isPainting)
            return;
        isPainting = true;

        try {
            super.paintComponent(g);

            // Don't paint if connector is null
            if (connector == null)
                return;

            Point source = null;
            Point sink = null;

            // Get positions from UI gates
            if (sourceGate != null && targetGate != null) {
                source = sourceGate.getOutputPortAbsolutePosition(sourcePort);
                sink = targetGate.getInputPortAbsolutePosition(targetPort);
            }
            // Fallback to connector's points
            else if (connector.getSource() != null && connector.getSink() != null) {
                source = connector.getSource();
                sink = connector.getSink();
            }

            if (source != null && sink != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw selection highlight first if wire is selected
                if (selected) {
                    g2d.setColor(Color.YELLOW);
                    g2d.setStroke(new BasicStroke(6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.drawLine(source.x, source.y, sink.x, sink.y);
                }

                // Draw the actual wire with signal-based coloring
                Color wireColor;
                Boolean signalValue = connector.getSignalValue();
                if (signalValue != null && signalValue) {
                    wireColor = HIGH_SIGNAL_COLOR;
                } else if (signalValue != null && !signalValue) {
                    wireColor = LOW_SIGNAL_COLOR;
                } else {
                    wireColor = DEFAULT_COLOR;
                }

                g2d.setColor(wireColor);
                g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(source.x, source.y, sink.x, sink.y);

                // Optional: Draw direction arrow for better visualization
                // drawDirectionArrow(g2d, source, sink, wireColor);
            }
        } finally {
            isPainting = false;
        }
    }
}