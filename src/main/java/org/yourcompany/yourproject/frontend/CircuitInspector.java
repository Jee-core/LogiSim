package org.yourcompany.yourproject.GUI.ui;

import javax.swing.*;
import org.yourcompany.yourproject.GUI.ui.Board;
import org.yourcompany.yourproject.businessLayer.analysis.TruthTableGenerator;
import org.yourcompany.yourproject.businessLayer.analysis.TruthTableGenerator.TruthTableRow;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.Connector;
import org.yourcompany.yourproject.businessLayer.components.Circuit;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;

/**
 * Circuit Inspector - Compact panel to display and edit inputs/outputs for the selected gate
 * With modern dark theme and compact layout
 */
public class CircuitInspector extends JPanel {

    private final Board controller;
    private JLabel gateNameLabel;
    private final JPanel inputsPanel;
    private final JPanel outputsPanel;
    //private JButton analyzeButton;
    private JButton toggleButton;
    private ComponentBase selectedGate;
    private Circuit currentCircuit;
    
    // Collapsible behavior variables
    private boolean isCollapsed = false;
    private int originalHeight;
    private int dragStartY;
    private boolean isDragging = false;
    private JPanel dragHandle;

    // Color scheme
    private final Color BACKGROUND_COLOR = new Color(45, 45, 55);
    private final Color ACCENT_COLOR = new Color(70, 130, 180);
    private final Color TEXT_COLOR = Color.GRAY;
    private final Color PANEL_BACKGROUND = new Color(35, 35, 45);
    private final Color BORDER_COLOR = new Color(100, 100, 150);

    // Store radio buttons to prevent recreation
    private List<JRadioButton[]> inputRadioButtons = new ArrayList<>();

    public CircuitInspector(Board controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        // Slightly larger size to accommodate bigger fonts
        setPreferredSize(new Dimension(350, 200));
        setMinimumSize(new Dimension(320, 180));
        setMaximumSize(new Dimension(400, 300));

        // Create drag handle
        createDragHandle();
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Main content - simplified without tabs
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 8, 0));
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        this.gateNameLabel = new JLabel();

        inputsPanel = new JPanel();
        inputsPanel.setOpaque(false);
        inputsPanel.setLayout(new BoxLayout(inputsPanel, BoxLayout.Y_AXIS));

        outputsPanel = new JPanel();
        outputsPanel.setOpaque(false);
        outputsPanel.setLayout(new BoxLayout(outputsPanel, BoxLayout.Y_AXIS));

        mainContent.add(createSectionPanel("INPUTS", inputsPanel));
        mainContent.add(createSectionPanel("OUTPUTS", outputsPanel));

        contentPanel.add(mainContent, BorderLayout.CENTER);
        
        // Add components to main panel
        add(dragHandle, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        // Store original height for restoration
        originalHeight = getPreferredSize().height;

        refreshContent();
    }

    /**
     * Create the compact drag handle
     */
    private void createDragHandle() {
        dragHandle = new JPanel(new BorderLayout());
        dragHandle.setBackground(new Color(55, 55, 65));
        dragHandle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        dragHandle.setPreferredSize(new Dimension(getWidth(), 8));
        dragHandle.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));

        // Add minimal dots indicator
        JPanel dotsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 1));
        dotsPanel.setOpaque(false);
        for (int i = 0; i < 3; i++) {
            JLabel dot = new JLabel("•");
            dot.setForeground(new Color(150, 150, 150));
            dot.setFont(new Font("Arial", Font.PLAIN, 8));
            dotsPanel.add(dot);
        }
        dragHandle.add(dotsPanel, BorderLayout.CENTER);

        // Add mouse listeners for dragging
        dragHandle.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStartY = e.getYOnScreen();
                isDragging = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    toggleCollapse();
                }
            }
        });

        dragHandle.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    int deltaY = dragStartY - e.getYOnScreen();
                    dragStartY = e.getYOnScreen();
                    
                    Container parent = getParent();
                    if (parent != null) {
                        LayoutManager layout = parent.getLayout();
                        if (layout instanceof BorderLayout) {
                            resizeToolbar(deltaY);
                        }
                    }
                }
            }
        });
    }

    /**
     * Create compact header panel
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Left side: Gate name
        gateNameLabel = new JLabel("No gate selected");
        gateNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gateNameLabel.setForeground(TEXT_COLOR);

        // Right side: Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);

        // Toggle button (maximize/minimize)
        toggleButton = new JButton("−");
        toggleButton.setFont(new Font("Arial", Font.BOLD, 14));
        toggleButton.setBackground(PANEL_BACKGROUND);
        toggleButton.setForeground(TEXT_COLOR);
        toggleButton.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        toggleButton.setFocusPainted(false);
        toggleButton.addActionListener(e -> toggleCollapse());

        buttonPanel.add(toggleButton);

        headerPanel.add(gateNameLabel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        return headerPanel;
    }
   
/**
 * Get topological order for circuit (same as TruthTableGenerator)
 */
private List<ComponentBase> getTopologicalOrder(Circuit circuit) {
    List<ComponentBase> result = new ArrayList<>();
    Map<ComponentBase, Integer> inDegree = new HashMap<>();
    Queue<ComponentBase> queue = new LinkedList<>();
    
    // Initialize in-degree for each gate
    for (ComponentBase gate : circuit.getGates()) {
        inDegree.put(gate, 0);
    }
    
    // Calculate in-degree (number of incoming connections from within circuit)
    for (Connector wire : circuit.getWires()) {
        ComponentBase toGate = wire.getToGate();
        if (circuit.getGates().contains(wire.getFromGate())) {
            inDegree.put(toGate, inDegree.get(toGate) + 1);
        }
    }
    
    // Add gates with zero in-degree to queue (input nodes)
    for (ComponentBase gate : circuit.getGates()) {
        if (inDegree.get(gate) == 0) {
            queue.add(gate);
        }
    }
    
    // Process queue
    while (!queue.isEmpty()) {
        ComponentBase current = queue.poll();
        result.add(current);
        
        // Decrease in-degree of neighbors
        for (int i = 0; i < current.getOutputs(); i++) {
            Connector outputConnector = current.getOutputConnector(i);
            if (outputConnector != null) {
                ComponentBase neighbor = outputConnector.getToGate();
                if (circuit.getGates().contains(neighbor)) {
                    int newDegree = inDegree.get(neighbor) - 1;
                    inDegree.put(neighbor, newDegree);
                    if (newDegree == 0) {
                        queue.add(neighbor);
                    }
                }
            }
        }
    }
    
    // If not all gates are processed, add remaining gates
    if (result.size() != circuit.getGates().size()) {
        for (ComponentBase gate : circuit.getGates()) {
            if (!result.contains(gate)) {
                result.add(gate);
            }
        }
    }
    
    return result;
}
/**
 * Get output names in the proper circuit order based on the truth table data
 */
private List<String> getOutputsInCircuitOrder(TruthTableRow firstRow) {
    if (firstRow == null) {
        return new ArrayList<>();
    }
    
    // Get all output names from the truth table
    Map<String, Boolean> outputs = firstRow.getOutputs();
    List<String> outputNames = new ArrayList<>(outputs.keySet());
    
    // DEBUG: Print the order we're getting from the truth table
    System.out.println("GUI - Raw output names from truth table: " + outputNames);
    
    // FIX: Use topological order to match the console exactly
    if (currentCircuit != null) {
        try {
            List<ComponentBase> topologicalOrder = getTopologicalOrder(currentCircuit);
            List<String> topologicalNames = new ArrayList<>();
            
            // Add gates in topological order
            for (ComponentBase gate : topologicalOrder) {
                String gateName = gate.getName();
                if (outputNames.contains(gateName) && !topologicalNames.contains(gateName)) {
                    topologicalNames.add(gateName);
                }
            }
            
            // Add any remaining gates that weren't in topological order
            for (String gateName : outputNames) {
                if (!topologicalNames.contains(gateName)) {
                    topologicalNames.add(gateName);
                }
            }
            
            System.out.println("GUI - Output names in topological order: " + topologicalNames);
            return topologicalNames;
            
        } catch (Exception e) {
            System.err.println("Error getting topological order: " + e.getMessage());
            // Fallback: return as-is
            return outputNames;
        }
    }
    
    return outputNames;
}
    /**
     * Show truth table in a message box dialog
     */
    public void showTruthTableDialog() {
        if (currentCircuit == null || currentCircuit.getGates().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No circuit available for analysis.", 
                "Analysis Error", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            TruthTableGenerator generator = new TruthTableGenerator();
            
            // Add debug information
            System.out.println("DEBUG: Analyzing circuit: " + currentCircuit.getName());
            System.out.println("DEBUG: Circuit has " + currentCircuit.getGates().size() + " gates");
            
            List<TruthTableRow> truthTable = generator.generateTruthTable(currentCircuit);
            
            System.out.println("DEBUG: Generated truth table with " + truthTable.size() + " rows");
            
            // Create a custom dialog for truth table
            JDialog truthTableDialog = createTruthTableDialog(truthTable);
            
            // FIX: Center the dialog on screen instead of relative to this component
            truthTableDialog.setLocationRelativeTo(null); // This centers on screen
            truthTableDialog.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error during analysis: " + ex.getMessage(), 
                "Analysis Error", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Create a custom dialog for displaying truth table
     */
    private JDialog createTruthTableDialog(List<TruthTableRow> table) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Circuit Truth Table", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(600, 400);
        // REMOVED: dialog.setLocationRelativeTo(this); - This was causing bottom placement
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);

        // Header
        JLabel headerLabel = new JLabel("Truth Table - " + currentCircuit.getName(), SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerLabel.setForeground(TEXT_COLOR);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        dialog.add(headerLabel, BorderLayout.NORTH);

        // Table content
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(PANEL_BACKGROUND);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (table == null || table.isEmpty()) {
        JLabel noDataLabel = new JLabel("No truth table data available", SwingConstants.CENTER);
        noDataLabel.setForeground(Color.GRAY);
        noDataLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablePanel.add(noDataLabel);
    } else {
        // Get input and output names using the same logic as first code
        Map<String, Boolean> firstInputs = table.get(0).getInputs();
        Map<String, Boolean> firstOutputs = table.get(0).getOutputs();
        
        List<String> inputNames = new ArrayList<>(firstInputs.keySet());
        List<String> outputNames = new ArrayList<>(firstOutputs.keySet());
        
        // DEBUG: Print what we're working with
        System.out.println("=== GUI TRUTH TABLE DEBUG ===");
        System.out.println("Input names: " + inputNames);
        System.out.println("Output names (raw from truth table): " + outputNames);
        
        // FIX: Get output names in proper circuit order
        outputNames = getOutputsInCircuitOrder(table.get(0));
        
        System.out.println("Output names (after ordering): " + outputNames);
        System.out.println("=== END DEBUG ===");
        
        // Inputs can stay alphabetical
        Collections.sort(inputNames);
        
        // Create header
        JPanel headerPanel = createDialogHeaderRow(inputNames, outputNames);
        tablePanel.add(headerPanel);
            
            // Add separator
            JSeparator separator = new JSeparator();
            separator.setForeground(BORDER_COLOR);
            tablePanel.add(separator);
            tablePanel.add(Box.createVerticalStrut(5));
            
            // Add data rows
            for (int i = 0; i < table.size(); i++) {
                JPanel rowPanel = createDialogDataRow(inputNames, outputNames, table.get(i), i);
                tablePanel.add(rowPanel);
                if (i < table.size() - 1) {
                    tablePanel.add(Box.createVerticalStrut(3));
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(PANEL_BACKGROUND);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton closeButton = new JButton("Close");
        closeButton.setBackground(ACCENT_COLOR);
        closeButton.setForeground(TEXT_COLOR);
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        return dialog;
    }

    /**
     * Get output names in the proper circuit order based on the truth table data
     */

    /**
     * Helper method to find a gate by name in the current circuit
     */
    private ComponentBase findGateByName(String gateName) {
        if (currentCircuit == null) return null;
        
        for (ComponentBase gate : currentCircuit.getGates()) {
            if (gate.getName().equals(gateName)) {
                return gate;
            }
        }
        return null;
    }

    /**
     * Create header row for truth table dialog
     */
    private JPanel createDialogHeaderRow(List<String> inputNames, List<String> outputNames) {
        JPanel headerPanel = new JPanel(new GridLayout(1, inputNames.size() + outputNames.size() + 1, 5, 0));
        headerPanel.setBackground(new Color(60, 60, 70));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Input headers
        for (String inputName : inputNames) {
            JLabel label = new JLabel(inputName, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setForeground(new Color(100, 200, 255));
            headerPanel.add(label);
        }
        
        // Separator
        JLabel separator = new JLabel("→", SwingConstants.CENTER);
        separator.setForeground(ACCENT_COLOR);
        separator.setFont(new Font("Segoe UI", Font.BOLD, 14));
        headerPanel.add(separator);
        
        // Output headers (in circuit order)
        for (String outputName : outputNames) {
            JLabel label = new JLabel(outputName, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setForeground(new Color(255, 200, 100));
            headerPanel.add(label);
        }
        
        return headerPanel;
    }

    /**
     * Create data row for truth table dialog
     */
    private JPanel createDialogDataRow(List<String> inputNames, List<String> outputNames, TruthTableRow row, int rowIndex) {
        JPanel rowPanel = new JPanel(new GridLayout(1, inputNames.size() + outputNames.size() + 1, 5, 0));
        rowPanel.setBackground(rowIndex % 2 == 0 ? PANEL_BACKGROUND : new Color(40, 40, 50));
        rowPanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        // Input values
        for (String inputName : inputNames) {
            boolean value = row.getInput(inputName);
            JLabel label = new JLabel(value ? "1" : "0", SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(TEXT_COLOR);
            rowPanel.add(label);
        }
        
        // Separator
        JLabel separator = new JLabel("→", SwingConstants.CENTER);
        separator.setForeground(BORDER_COLOR);
        separator.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rowPanel.add(separator);
        
        // Output values (in circuit order)
        for (String outputName : outputNames) {
            boolean value = row.getOutput(outputName);
            JLabel label = new JLabel(value ? "1" : "0", SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(value ? new Color(100, 255, 100) : new Color(255, 100, 100));
            rowPanel.add(label);
        }
        
        return rowPanel;
    }

    /**
     * Toggle between collapsed and expanded states
     */
    private void toggleCollapse() {
        isCollapsed = !isCollapsed;
        
        if (isCollapsed) {
            // Collapse: show only header
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setPreferredSize(new Dimension(getWidth(), 40));
            toggleButton.setText("+");
        } else {
            // Expand: restore original size
            setMaximumSize(new Dimension(Integer.MAX_VALUE, originalHeight));
            setPreferredSize(new Dimension(getWidth(), originalHeight));
            toggleButton.setText("−");
        }
        
        revalidate();
        repaint();
        
        Container parent = getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }

    /**
     * Resize toolbar by dragging
     */
    private void resizeToolbar(int deltaY) {
        if (isCollapsed) return;
        
        int currentHeight = getHeight();
        int newHeight = Math.max(150, Math.min(450, currentHeight + deltaY));
        
        if (newHeight != currentHeight) {
            setPreferredSize(new Dimension(getWidth(), newHeight));
            revalidate();
            
            Container parent = getParent();
            if (parent != null) {
                parent.revalidate();
            }
        }
    }

    /**
     * Set the current circuit for analysis
     */
    public void setCurrentCircuit(Circuit circuit) {
        this.currentCircuit = circuit;
        
        // Automatically show truth table popup when circuit is set
        if (circuit != null && !circuit.getGates().isEmpty()) {
            //showTruthTableDialog();
        }
    }

    /**
     * Expand the toolbar if collapsed
     */
    public void ensureExpanded() {
        if (isCollapsed) {
            toggleCollapse();
        }
    }

    private JPanel createSectionPanel(String title, JPanel content) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(ACCENT_COLOR);

        wrapper.add(titleLabel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setPreferredSize(new Dimension(150, 120));
        
        // Style scrollbars
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setPreferredSize(new Dimension(10, 0));
        vertical.setBackground(PANEL_BACKGROUND);
        
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Update toolbar content with selected gate.
     */
    public void setSelectedGate(ComponentBase gate) {
        this.selectedGate = gate;
        SwingUtilities.invokeLater(this::refreshContent);
    }

    private void refreshContent() {
        inputsPanel.removeAll();
        outputsPanel.removeAll();
        inputRadioButtons.clear(); // Clear previous radio buttons

        if (selectedGate == null) {
            gateNameLabel.setText("No gate selected");
            inputsPanel.add(createCompactInfoLabel("Select a gate to edit inputs"));
            outputsPanel.add(createCompactInfoLabel("Outputs will appear here"));
        } else {
            gateNameLabel.setText(selectedGate.getName());
            renderCompactInputs();
            renderCompactOutputs();
        }

        revalidate();
        repaint();
    }

    private JLabel createCompactInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.GRAY);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void renderCompactInputs() {
        int inputCount = selectedGate.getInputs();
        if (inputCount == 0) {
            inputsPanel.add(createCompactInfoLabel("No inputs"));
            return;
        }

        for (int i = 0; i < inputCount; i++) {
            final int index = i;
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel label = new JLabel("Input " + (index + 1) + ":");
            label.setForeground(TEXT_COLOR);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));

            boolean currentValue = selectedGate.getInputValue(index);
            Connector connector = selectedGate.getInputConnector(index);
            boolean connected = connector != null;

            // Create radio button group for 0/1 selection
            JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            radioPanel.setOpaque(false);
            
            JRadioButton radio0 = new JRadioButton("0");
            JRadioButton radio1 = new JRadioButton("1");
            
            // Store radio buttons to prevent garbage collection
            inputRadioButtons.add(new JRadioButton[]{radio0, radio1});
            
            ButtonGroup radioGroup = new ButtonGroup();
            radioGroup.add(radio0);
            radioGroup.add(radio1);
            
            // Set initial selection based on current value
            if (currentValue) {
                radio1.setSelected(true);
            } else {
                radio0.setSelected(true);
            }
            
            // Style radio buttons
            radio0.setFont(new Font("Segoe UI", Font.BOLD, 12));
            radio1.setFont(new Font("Segoe UI", Font.BOLD, 12));
            radio0.setForeground(TEXT_COLOR);
            radio1.setForeground(TEXT_COLOR);
            radio0.setBackground(PANEL_BACKGROUND);
            radio1.setBackground(PANEL_BACKGROUND);
            radio0.setOpaque(false);
            radio1.setOpaque(false);
            radio0.setFocusPainted(false);
            radio1.setFocusPainted(false);
            
            // Enable/disable based on connection status
            boolean enabled = !connected && controller != null;
            radio0.setEnabled(enabled);
            radio1.setEnabled(enabled);
            
            // Add action listeners
            radio0.addActionListener(e -> {
                if (controller != null && selectedGate != null && radio0.isSelected()) {
                    controller.setGateInput(selectedGate, index, false);
                    refreshContent();
                }
            });
            
            radio1.addActionListener(e -> {
                if (controller != null && selectedGate != null && radio1.isSelected()) {
                    controller.setGateInput(selectedGate, index, true);
                    refreshContent();
                }
            });

            radioPanel.add(radio0);
            radioPanel.add(radio1);

            JLabel statusLabel = new JLabel(connected ? "Connected" : "Free");
            statusLabel.setForeground(connected ? new Color(255, 100, 100) : new Color(100, 255, 100));
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            row.add(label);
            row.add(radioPanel);
            row.add(statusLabel);
            inputsPanel.add(row);
            
            // Add some spacing between input rows
            inputsPanel.add(Box.createVerticalStrut(3));
        }
    }

    private void renderCompactOutputs() {
        int outputCount = selectedGate.getOutputs();
        if (outputCount == 0) {
            outputsPanel.add(createCompactInfoLabel("No outputs"));
            return;
        }

        for (int i = 0; i < outputCount; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel label = new JLabel("Output " + (i + 1) + ":");
            label.setForeground(TEXT_COLOR);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));

            boolean value = selectedGate.getOutputValue(i);
            JLabel valueLabel = new JLabel(value ? "HIGH (1)" : "LOW (0)");
            valueLabel.setForeground(value ? new Color(100, 255, 100) : new Color(255, 100, 100));
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

            row.add(label);
            row.add(valueLabel);
            outputsPanel.add(row);
            
            // Add some spacing between output rows
            outputsPanel.add(Box.createVerticalStrut(3));
        }
    }
}