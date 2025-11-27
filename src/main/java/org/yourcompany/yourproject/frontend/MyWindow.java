package org.yourcompany.yourproject.GUI.ui;

import javax.swing.*;

import org.yourcompany.yourproject.businessLayer.analysis.EventHandlers;
import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.CircuitComponent;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.Connector;
import org.yourcompany.yourproject.businessLayer.components.Project;
import org.yourcompany.yourproject.businessLayer.components.gates.AndGate;
import org.yourcompany.yourproject.businessLayer.components.gates.NotGate;
import org.yourcompany.yourproject.businessLayer.components.gates.OrGate;

import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

public class MyWindow extends JFrame {

    private Map<String, DrawGates> componentIdToUIMap = new HashMap<>();
    private List<DrawWire> wireUIComponents = new ArrayList<>();
    private Circuit currentCircuit;

    private Map<String, Circuit> circuitObjects = new HashMap<>();
    private java.util.Set<String> circuitsPlacedInCurrentDesign = new java.util.HashSet<>();
    private JButton btnNewCir, btnNew, btnExp, btnRun, btnTruthTable, btnRemComp, btnremCir, btndb_save, btndb_load,
            btnAddComp,
            btndb_del;
    private Hierarchy projectPanel;
    private DesignArea circuitPanel;
    private GatePallete componentPalette;
    private CircuitInspector gateToolbar;

    private Map<String, List<String>> projectCircuits = new HashMap<>();
    private String selectedProject = null;
    private String currentCircuitName = null;

    // ===================================================CONSTRUCTOR================================================
    public MyWindow() {
        setupDarkTheme();
        setTitle("LogiSim - Circuit Simulator");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Define the styling constants (matching GatePallete colors)
        final Color BACKGROUND_COLOR = new Color(45, 45, 55);
        final Color ACCENT_COLOR = new Color(70, 130, 180);
        final Color TEXT_COLOR = Color.WHITE;
        final Color PANEL_BACKGROUND = new Color(35, 35, 45);
        final Color BORDER_COLOR = new Color(100, 100, 150);
        final Color BUTTON_BACKGROUND = new Color(35, 35, 45);
        final Color BUTTON_HOVER = new Color(70, 70, 65);
        final Color BUTTON_FOREGROUND = Color.WHITE;

        // Create main top panel with three sections
        JPanel topContainer = new JPanel(new GridLayout(1, 3, 10, 0));
        topContainer.setBackground(BACKGROUND_COLOR);
        topContainer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // 1. Component Palette (Left)
        componentPalette = new GatePallete();

        // 2. Circuit Operations (Center) - using GridLayout for equal spacing
        JPanel circuitOperationsPanel = new JPanel(new GridLayout(2, 4, 8, 8));
        circuitOperationsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                "CIRCUIT OPERATIONS",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 10),
                TEXT_COLOR));
        circuitOperationsPanel.setBackground(BACKGROUND_COLOR);
        circuitOperationsPanel.setPreferredSize(new Dimension(400, 120));

        btnNewCir = createStyledButton("➕ Circuit", BUTTON_BACKGROUND, BUTTON_FOREGROUND);
        btnAddComp = createStyledButton("🔌Connect", BUTTON_BACKGROUND, BUTTON_FOREGROUND);
        btnRemComp = createStyledButton("🗑️Gate", BUTTON_BACKGROUND, BUTTON_FOREGROUND);
        btnremCir = createStyledButton("🗑️ Circuit", BUTTON_BACKGROUND, BUTTON_FOREGROUND);
        btnRun = createStyledButton("▶️ Run", BUTTON_BACKGROUND, BUTTON_FOREGROUND);
        btnTruthTable = createStyledButton("📊 Analysis", BUTTON_BACKGROUND, BUTTON_FOREGROUND);
        btnExp = createStyledButton("💾 Export", BUTTON_BACKGROUND, BUTTON_FOREGROUND);

        // Add buttons in organized grid pattern
        circuitOperationsPanel.add(btnNewCir);
        circuitOperationsPanel.add(btnAddComp);
        circuitOperationsPanel.add(btnRemComp);
        circuitOperationsPanel.add(btnremCir);
        circuitOperationsPanel.add(btnRun);
        circuitOperationsPanel.add(btnTruthTable);
        circuitOperationsPanel.add(btnExp);
        circuitOperationsPanel.add(Box.createGlue()); // Empty space for alignment

        // 3. Project Operations (Right) - using GridLayout for equal spacing
        JPanel projectOperationsPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        projectOperationsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                "PROJECT OPERATIONS",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 10),
                TEXT_COLOR));
        projectOperationsPanel.setBackground(BACKGROUND_COLOR);
        projectOperationsPanel.setPreferredSize(new Dimension(200, 120));

        btnNew = createStyledButton("🆕 Project", BUTTON_BACKGROUND, BUTTON_FOREGROUND);
        btndb_save = createStyledButton("💾 Save", BUTTON_BACKGROUND, BUTTON_FOREGROUND);
        btndb_load = createStyledButton("📂 Load", BUTTON_BACKGROUND, BUTTON_FOREGROUND);
        btndb_del = createStyledButton("🗑️ Delete", BUTTON_BACKGROUND, BUTTON_FOREGROUND);

        // Add buttons in organized grid pattern
        projectOperationsPanel.add(btnNew);
        projectOperationsPanel.add(btndb_save);
        projectOperationsPanel.add(btndb_load);
        projectOperationsPanel.add(btndb_del);

        // Add all three panels to top container
        topContainer.add(componentPalette);
        topContainer.add(circuitOperationsPanel);
        topContainer.add(projectOperationsPanel);

        // Add to frame
        add(topContainer, BorderLayout.NORTH);

        // Main circuit panel (takes full center)
        circuitPanel = new DesignArea();

        // Setup gate toolbar
        gateToolbar = new CircuitInspector(circuitPanel.getController());
        circuitPanel.setGateSelectionListener(gateToolbar::setSelectedGate);

        // Create center container for Project Panel (left sidebar) and Circuit Panel
        JPanel centerContainer = new JPanel(new BorderLayout());

        // Add Project Panel to left sidebar
        projectPanel = new Hierarchy();
        projectPanel.setPreferredSize(new Dimension(200, 0)); // Width 200, height ignored in WEST
        centerContainer.add(projectPanel, BorderLayout.WEST);

        // Add circuit panel to center
        centerContainer.add(circuitPanel, BorderLayout.CENTER);

        // Add main components to frame
        add(centerContainer, BorderLayout.CENTER); // Project sidebar + circuit panel in center
        add(gateToolbar, BorderLayout.SOUTH); // GateIO at bottom

        setupPaletteListeners();

        // =====================================================EVENT
        // LISTENERS===================================================

        btnRun.addActionListener(e -> {
            simulateCircuit();
        });
        // ------------------------------------------------------------------DB
        // BUTTONS------------------------------------------------
        btndb_save.addActionListener(e -> {
            Project project = getCurrentProjectFromUI();
            EventHandlers handler = new EventHandlers();
            if (handler.saveProject(project)) {
                showDarkThemeMessage("Project saved successfully!", "Save Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showDarkThemeMessage("Failed to save project.", "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btndb_load.addActionListener(e -> {
            EventHandlers handler = new EventHandlers();
            List<String> availableProjects = handler.getAllProjectNames();

            if (availableProjects == null || availableProjects.isEmpty()) {
                showDarkThemeMessage("No projects found in database.", "No Projects", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String selectedProject = (String) JOptionPane.showInputDialog(this,
                    "Select project to load:",
                    "Load Project",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    availableProjects.toArray(),
                    availableProjects.get(0));

            if (selectedProject != null) {
                // Clear current UI first
                clearCurrentUI();

                Project project = handler.loadProject(selectedProject);
                if (project != null) {
                    loadProjectIntoUI(project);
                    showDarkThemeMessage("Project '" + selectedProject + "' loaded successfully!",
                            "Load Successful", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showDarkThemeMessage("Failed to load project: " + selectedProject,
                            "Load Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btndb_del.addActionListener(e -> {
            String projectName = getSelectedProjectName();
            if (projectName == null || projectName.trim().isEmpty()) {
                showDarkThemeMessage("No project selected for deletion.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            UIManager.put("OptionPane.background", Color.BLACK);
            UIManager.put("Panel.background", Color.BLACK);
            UIManager.put("OptionPane.messageForeground", Color.WHITE);
            UIManager.put("OptionPane.messageBackground", Color.BLACK);
            UIManager.put("Button.background", Color.DARK_GRAY);
            UIManager.put("Button.foreground", Color.DARK_GRAY); // Changed to WHITE for better visibility
            UIManager.put("Button.focus", Color.GRAY);

            int result = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete project '" + projectName + "'?\nThis action cannot be undone!",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            // Reset UI manager

            if (result == JOptionPane.YES_OPTION) {
                EventHandlers handler = new EventHandlers();
                if (handler.deleteProject(projectName)) {
                    showDarkThemeMessage("Project '" + projectName + "' deleted successfully!", "Deletion Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                    refreshProjectListUI();
                } else {
                    showDarkThemeMessage("Failed to delete project '" + projectName + "'.", "Deletion Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        // ---------------------------------------------------------------CIRCUIT-------------------------------------------------------
        btnAddComp.addActionListener(e -> {
            connectCircuitAsComponent();
        });
        btnRemComp.addActionListener(e -> {
            if (circuitPanel.getCurrentCircuit() == null) {
                JOptionPane.showMessageDialog(this,
                        "No circuit is currently open!",
                        "No Circuit",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<ComponentBase> gates = circuitPanel.getCurrentCircuit().getGates();
            if (gates.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "The circuit is empty!",
                        "No Gates",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] gateNames = new String[gates.size()];
            for (int i = 0; i < gates.size(); i++) {
                ComponentBase gate = gates.get(i);
                Point pos = gate.getPosition();
                gateNames[i] = gate.getName() + " at (" + pos.x + ", " + pos.y + ")";
            }

            String selectedGateName = (String) JOptionPane.showInputDialog(this,
                    "Select a gate to remove:",
                    "Remove Gate",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    gateNames,
                    gateNames[0]);

            if (selectedGateName != null) {

                int selectedIndex = -1;
                for (int i = 0; i < gateNames.length; i++) {
                    if (gateNames[i].equals(selectedGateName)) {
                        selectedIndex = i;
                        break;
                    }
                }

                if (selectedIndex != -1) {
                    ComponentBase gateToRemove = gates.get(selectedIndex);

                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Remove gate: " + gateToRemove.getName() + "?",
                            "Confirm Removal",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        // Remove the gate using circuit panel's method
                        circuitPanel.getCurrentCircuit().removeGate(gateToRemove);

                        // Refresh the circuit panel
                        circuitPanel.addCircuitComponents(circuitPanel.getCurrentCircuit());

                        JOptionPane.showMessageDialog(this,
                                "Gate removed successfully!",
                                "Removal Complete",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        btnremCir.addActionListener(e -> {
            String selected = projectPanel.list.getSelectedValue();
            if (selected == null)
                return;

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to remove \"" + selected.trim() + "\"?",
                    "Confirm Removal",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION)
                return; // exit if not confirmed

            DefaultListModel<String> model = projectPanel.model;

            if (selected.startsWith("   → ")) {
                // Circuit selected
                String circuitName = selected.substring(5);

                if (selectedProject != null) {
                    // Remove from projectCircuits map
                    List<String> circuits = projectCircuits.get(selectedProject);
                    if (circuits != null)
                        circuits.remove(circuitName);

                    // Remove from circuitObjects map
                    String key = selectedProject + ":" + circuitName;
                    circuitObjects.remove(key);

                    // Remove from JList
                    model.removeElement(selected);

                    // If currently open, clear design area
                    if (circuitName.equals(currentCircuitName)) {
                        circuitPanel.clearCircuit();
                        currentCircuitName = null;
                    }

                    // Also remove from circuitsPlacedInCurrentDesign
                    circuitsPlacedInCurrentDesign.remove(circuitName);

                    // NEW: Remove this circuit from all other circuits that use it as a component
                    removeCircuitFromAllParentCircuits(circuitName);
                }

            } else {
                // Project selected
                String projectName = selected;

                // Remove all circuits under project
                List<String> circuits = projectCircuits.get(projectName);
                if (circuits != null) {
                    for (String cir : circuits) {
                        // NEW: Remove each circuit from all parent circuits before removing
                        removeCircuitFromAllParentCircuits(cir);

                        circuitObjects.remove(projectName + ":" + cir);
                        circuitsPlacedInCurrentDesign.remove(cir);
                        model.removeElement("   → " + cir);
                    }
                }

                // Remove project from projectCircuits map
                projectCircuits.remove(projectName);

                // Remove project from JList
                model.removeElement(projectName);

                // Clear design area if a circuit from this project was open
                if (selectedProject != null && selectedProject.equals(projectName)) {
                    circuitPanel.clearCircuit();
                    selectedProject = null;
                    currentCircuitName = null;
                }
            }
        });

        // CREATE CIRCUIT (inside selected project)
        btnNewCir.addActionListener(e -> {
            if (selectedProject == null) {
                JOptionPane.showMessageDialog(this, "Select or create a project first.");
                return;
            }
            String name = JOptionPane.showInputDialog(this, "Enter Circuit Name:");
            if (name == null)
                return;
            name = name.trim();
            if (name.isEmpty())
                return;

            // avoid duplicate circuit names within same project
            List<String> list = projectCircuits.get(selectedProject);
            if (list == null) {
                list = new ArrayList<>();
                projectCircuits.put(selectedProject, list);
            }
            if (list.contains(name)) {
                JOptionPane.showMessageDialog(this, "Circuit with this name already exists in the project.");
                return;
            }
            list.add(name);

            // Create a new Circuit object for this circuit
            Circuit newCircuit = new Circuit(name);
            String key = selectedProject + ":" + name;
            circuitObjects.put(key, newCircuit);

            // Insert project sub-item in the JList model just after the project element
            DefaultListModel<String> model = projectPanel.model;
            int projIndex = model.indexOf(selectedProject);
            if (projIndex >= 0) {
                model.add(projIndex + modelIndexCountUnderProject(model, projIndex) + 1, "   → " + name);
            } else {
                model.addElement("   → " + name);
            }
        });
        // -----------------------------------------PROJECT
        // CREATION-------------------------------------------------
        btnNew.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter Project Name:");
            if (name == null)
                return;
            name = name.trim();
            if (name.isEmpty())
                return;
            if (projectCircuits.containsKey(name)) {
                JOptionPane.showMessageDialog(this, "Project already exists.");
                return;
            }
            Circuit newCircuit = new Circuit(name);
            circuitObjects.put(selectedProject + ":" + name, newCircuit);

            projectPanel.model.addElement(name);
            projectCircuits.put(name, new ArrayList<>());
        });
        projectPanel.list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                String selected = projectPanel.list.getSelectedValue();
                if (selected == null)
                    return;
                if (!selected.startsWith("   → ")) {
                    selectedProject = selected;
                    currentCircuitName = null;
                    circuitPanel.showCircuit("No circuit selected");
                }
            }
        });

        // ---------------------------------------------------TRUTH
        // TABLE-------------------------------------------------
        btnTruthTable.addActionListener(e -> {
            if (selectedProject == null || currentCircuitName == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select a project and open a circuit first.",
                        "No Circuit Selected",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String key = selectedProject + ":" + currentCircuitName;
            Circuit circuit = circuitObjects.get(key);

            if (circuit == null) {
                JOptionPane.showMessageDialog(this,
                        "No circuit loaded. Please open a circuit first.",
                        "Circuit Not Found",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            gateToolbar.setCurrentCircuit(circuit);
            gateToolbar.showTruthTableDialog(); // Add this line to show the truth table
        });

        projectPanel.list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting())
                return;
            String selected = projectPanel.list.getSelectedValue();
            if (selected == null)
                return;

            if (selected.startsWith("   → ")) {
                // circuit clicked
                String circuitName = selected.substring(5);
                currentCircuitName = circuitName;

                // Get the circuit object and load it into the panel
                if (selectedProject != null) {
                    String key = selectedProject + ":" + circuitName;
                    Circuit circuit = circuitObjects.get(key);
                    if (circuit != null) {
                        circuitPanel.setCurrentCircuit(circuit);
                        circuitPanel.addCircuitComponents(circuit);
                        circuitPanel.refreshCircuitLabel(); // Ensure label is updated
                        gateToolbar.setCurrentCircuit(circuit);
                    } else {
                        // Circuit doesn't exist yet - create it and set it
                        circuit = new Circuit(circuitName);
                        circuitObjects.put(key, circuit);
                        circuitPanel.setCurrentCircuit(circuit);
                        circuitPanel.refreshCircuitLabel();
                    }
                } else {
                    // No project selected - still create circuit for editing
                    Circuit circuit = new Circuit(circuitName);
                    circuitPanel.setCurrentCircuit(circuit);
                    circuitPanel.refreshCircuitLabel();
                }
            } else {
                // project clicked
                selectedProject = selected;
                currentCircuitName = null;
                circuitPanel.setCurrentCircuit(null);
                circuitPanel.showCircuit("No circuit selected");
            }
        });
        EventHandlers eventHandlers = new EventHandlers();

        // --------------------------------------------------------EXPORT------------------------------------------
        btnExp.addActionListener(e -> {
            String selected = projectPanel.list.getSelectedValue();

            if (selected == null) {
                showStyledMessageDialog("No circuit selected!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String circuitName = selected.startsWith("   → ") ? selected.substring(5) : selected;

            // Quick validation
            if (currentCircuitName == null || !currentCircuitName.equals(circuitName)) {
                showStyledMessageDialog(
                        "Please open circuit '" + circuitName + "' in the design area before exporting.",
                        "Circuit Not Open",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (circuitPanel.getCurrentCircuit() == null || circuitPanel.getCurrentCircuit().getGates().isEmpty()) {
                showStyledMessageDialog(
                        "The circuit is empty! There's nothing to export.",
                        "Empty Circuit",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Show quick progress indicator
            showStyledMessageDialog(
                    "Exporting circuit...\nThis will take just a moment.",
                    "Export Started",
                    JOptionPane.INFORMATION_MESSAGE);

            // Use EventHandlers export logic instead of ExportHandler
            eventHandlers.exportCircuit(circuitName, circuitPanel);
        });

        setVisible(true);
    }

    // Helper method for dark theme buttons
    private JButton createDarkButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100)),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                Color brighter = bgColor.brighter();
                button.setBackground(brighter);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    // Enhanced dark theme message dialog
    private void showDarkThemeMessage(String message, String title, int messageType) {
        // Store original colors
        Object originalOptionPaneBg = UIManager.get("OptionPane.background");
        Object originalPanelBg = UIManager.get("Panel.background");
        Object originalMessageFg = UIManager.get("OptionPane.messageForeground");
        Object originalMessageBg = UIManager.get("OptionPane.messageBackground");
        Object originalButtonBg = UIManager.get("Button.background");
        Object originalButtonFg = UIManager.get("Button.foreground");

        // Set dark theme colors
        UIManager.put("OptionPane.background", Color.BLACK);
        UIManager.put("Panel.background", Color.BLACK);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("OptionPane.messageBackground", Color.BLACK);
        UIManager.put("Button.background", new Color(50, 50, 50));
        UIManager.put("Button.foreground", Color.DARK_GRAY);

        try {
            JOptionPane.showMessageDialog(this, message, title, messageType);
        } finally {
            // Restore original colors
            UIManager.put("OptionPane.background", originalOptionPaneBg);
            UIManager.put("Panel.background", originalPanelBg);
            UIManager.put("OptionPane.messageForeground", originalMessageFg);
            UIManager.put("OptionPane.messageBackground", originalMessageBg);
            UIManager.put("Button.background", originalButtonBg);
            UIManager.put("Button.foreground", originalButtonFg);
        }
    }

    // Helper method to create styled message dialogs with white text on black
    // background
    private void showStyledMessageDialog(String message, String title, int messageType) {
        UIManager.put("OptionPane.background", Color.BLACK);
        UIManager.put("Panel.background", Color.BLACK);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("OptionPane.messageBackground", Color.BLACK);
        UIManager.put("Button.background", Color.DARK_GRAY);
        UIManager.put("Button.foreground", Color.DARK_GRAY);
        UIManager.put("Button.focus", Color.GRAY);

        try {
            JOptionPane.showMessageDialog(this, message, title, messageType);
        } finally {
            // Reset to default colors
            UIManager.put("OptionPane.background", Color.BLACK);
            UIManager.put("Panel.background", Color.BLACK);
            UIManager.put("OptionPane.messageForeground", Color.WHITE);
            UIManager.put("OptionPane.messageBackground", Color.BLACK);
            UIManager.put("Button.background", Color.DARK_GRAY);
            UIManager.put("Button.foreground", Color.DARK_GRAY);
            UIManager.put("Button.focus", Color.GRAY);
        }
    }

    private void setupDarkTheme() {
        UIManager.put("OptionPane.background", Color.BLACK);
        UIManager.put("Panel.background", Color.BLACK);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("OptionPane.messageBackground", Color.BLACK);
        UIManager.put("Button.background", Color.DARK_GRAY);
        UIManager.put("Button.foreground", Color.DARK_GRAY); // Changed to WHITE for better visibility
        UIManager.put("Button.focus", Color.GRAY);
    }

    /**
     * Create a styled button with light blue theme
     */
    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);

        // Set colors FIRST
        button.setBackground(bgColor);
        button.setForeground(fgColor);

        // THEN set opaque - this order matters
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true); // THIS IS CRUCIAL!

        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 150), 1), // Use your theme color instead of GRAY
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // IMPROVED Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            private final Color originalBg = bgColor;
            private final Color hoverColor = bgColor.brighter();

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalBg);
            }
        });

        return button;
    }

    /**
     * Setup listeners for component palette - handles both click and drag
     * operations
     */
    private void setupPaletteListeners() {
        // AND Gate button
        componentPalette.getAndButton().addActionListener(e -> {
            addGateFromPalette("AND Gate", 200, 150);
        });
        setupDragAndDrop(componentPalette.getAndButton(), "AND Gate");

        // OR Gate button
        componentPalette.getOrButton().addActionListener(e -> {
            addGateFromPalette("OR Gate", 200, 150);
        });
        setupDragAndDrop(componentPalette.getOrButton(), "OR Gate");

        // NOT Gate button
        componentPalette.getNotButton().addActionListener(e -> {
            addGateFromPalette("NOT Gate", 200, 150);
        });
        setupDragAndDrop(componentPalette.getNotButton(), "NOT Gate");
    }

    /**
     * Add a gate from the palette when clicked
     */
    private void addGateFromPalette(String gateType, int x, int y) {
        // Check if circuit is open
        if (circuitPanel.getCurrentCircuit() == null) {
            JOptionPane.showMessageDialog(this,
                    "Please open or create a circuit first.\n" +
                            "1. Select a project\n" +
                            "2. Create a new circuit or open an existing one",
                    "No Circuit Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ComponentBase gate = createGateFromType(gateType);
        if (gate != null) {
            circuitPanel.addGateToCircuit(gate, x, y);
        }
    }

    /**
     * Create a gate instance from type string
     */
    private ComponentBase createGateFromType(String gateType) {
        switch (gateType) {
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

    /**
     * Setup drag and drop for palette buttons
     */
    private void setupDragAndDrop(JButton button, String gateType) {
        final boolean[] isDragging = { false };
        final Point[] dragStartPoint = { null };

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                // Don't check circuit here - allow drag to start
                isDragging[0] = true;
                dragStartPoint[0] = e.getPoint();
                button.setCursor(new Cursor(Cursor.MOVE_CURSOR));
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));

                if (isDragging[0]) {
                    // Convert screen coordinates to circuitPanel coordinates
                    Point screenPoint = e.getLocationOnScreen();
                    Point circuitPoint = new Point(screenPoint);
                    SwingUtilities.convertPointFromScreen(circuitPoint, circuitPanel);

                    // Check if released over circuit panel
                    if (circuitPanel.contains(circuitPoint)) {
                        // Check if circuit is open
                        if (circuitPanel.getCurrentCircuit() == null) {
                            JOptionPane.showMessageDialog(MyWindow.this,
                                    "Please open or create a circuit first.\n" +
                                            "1. Select a project\n" +
                                            "2. Create a new circuit or open an existing one",
                                    "No Circuit Selected",
                                    JOptionPane.WARNING_MESSAGE);
                        } else {
                            ComponentBase gate = createGateFromType(gateType);
                            if (gate != null) {
                                // Use the mouse position in circuit panel coordinates
                                circuitPanel.addGateToCircuit(gate, circuitPoint.x, circuitPoint.y);
                            }
                        }
                    }
                }

                isDragging[0] = false;
                dragStartPoint[0] = null;
            }
        });

        button.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (isDragging[0]) {
                    Point screenPoint = e.getLocationOnScreen();
                    Point circuitPoint = new Point(screenPoint);
                    SwingUtilities.convertPointFromScreen(circuitPoint, circuitPanel);

                    if (circuitPanel.contains(circuitPoint) && circuitPanel.getCurrentCircuit() != null) {
                        circuitPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                    } else {
                        circuitPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            }
        });

    }

    // ---------------------------------------------FUNCTIONS-------------------------------------------------------------------------
    private int modelIndexCountUnderProject(DefaultListModel<String> model, int projIndex) {
        int count = 0;
        for (int i = projIndex + 1; i < model.size(); i++) {
            String s = model.get(i);
            if (s.startsWith("   → "))
                count++;
            else
                break; // next project reached
        }
        return count;
    }

    private Circuit deepCopyCircuit(Circuit original) {
        if (original == null)
            return null;

        Circuit copy = new Circuit(original.getName() + "_Copy");

        // Create a mapping from original gates to copied gates
        Map<ComponentBase, ComponentBase> gateMap = new HashMap<>();

        // Deep copy gates
        for (ComponentBase originalGate : original.getGates()) {
            ComponentBase copiedGate = originalGate.copy();
            if (copiedGate != null) {
                copiedGate.setPosition(originalGate.getPosition().x, originalGate.getPosition().y);
                copy.addGate(copiedGate);
                gateMap.put(originalGate, copiedGate);
            }
        }

        // Deep copy wires using the gate mapping
        for (Connector originalWire : original.getWires()) {
            ComponentBase fromGate = originalWire.getFromGate();
            ComponentBase toGate = originalWire.getToGate();

            ComponentBase copiedFromGate = gateMap.get(fromGate);
            ComponentBase copiedToGate = gateMap.get(toGate);

            if (copiedFromGate != null && copiedToGate != null) {
                int fromPort = originalWire.getFromPort();
                int toPort = originalWire.getToPort();

                copy.connectGates(copiedFromGate, fromPort, copiedToGate, toPort);
            }
        }

        return copy;
    }

    private String getSelectedProjectName() {
        if (projectPanel.list != null && projectPanel.list.getSelectedValue() != null) {
            String selected = projectPanel.list.getSelectedValue().toString();
            // Return only if it's a project (not a circuit)
            if (!selected.startsWith("   → ")) {
                return selected;
            }
        }

        // If no project is selected in UI but we have a loaded project, return it
        if (selectedProject != null) {
            return selectedProject;
        }

        JOptionPane.showMessageDialog(this, "Please select a project first!");
        return null;
    }

    // Helper method to find the final output of the circuit
    private boolean getFinalCircuitOutput(Circuit circuit, Map<String, Boolean> results) {
        // Look for gates that have no outgoing connections (final outputs)
        for (ComponentBase gate : circuit.getGates()) {
            boolean hasOutputConnections = false;

            // Check if this gate's outputs are connected to other gates
            for (Connector wire : circuit.getWires()) {
                if (wire.getFromGate() == gate) {
                    hasOutputConnections = true;
                    break;
                }
            }

            // If no output connections, this is likely a final output
            if (!hasOutputConnections) {
                Boolean output = results.get(gate.getName());
                if (output != null) {
                    return output;
                }
            }
        }

        // Fallback: use the last gate's output
        if (!circuit.getGates().isEmpty()) {
            ComponentBase lastGate = circuit.getGates().get(circuit.getGates().size() - 1);
            return results.getOrDefault(lastGate.getName(), false);
        }

        return false;
    }

    /**
     * Create a styled button for dialogs
     */
    private JButton createDialogButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(173, 216, 230)); // Light blue
        button.setForeground(new Color(70, 130, 180)); // Dark blue
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(183, 226, 240));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(173, 216, 230));
            }
        });

        return button;
    }

    // Method to display the final output
    private void showFinalOutput(boolean output, String circuitName) {
        String outputValue = output ? "1" : "0";
        String message = "<html>" +
                "<div style='text-align:center; font-family:Segoe UI, sans-serif; padding:15px; "
                + "background:#2d2d37; border:2px solid #6464c8; border-radius:8px;'> " +

                "<h2 style='margin:0 0 12px 0; color:#8cb3d9;'>⚡ Circuit Simulation</h2>" +

                "<p style='margin:8px 0; font-size:14px; color:#cccccc;'>"
                + "<b>Circuit:</b> " + circuitName +
                "</p>" +

                "<div style='margin-top:15px; padding:15px; "
                + "background:#1e1e24; border:2px solid " + (output ? "#4CAF50" : "#F44336") + "; "
                + "border-radius:8px;'> " +

                "<p style='font-size:24px; font-weight:bold; margin:0; color:"
                + (output ? "#4CAF50" : "#F44336") + ";'>"
                + "Final Output: " + outputValue +
                "</p>" +
                "</div>" +

                "</div>" +
                "</html>";

        JOptionPane.showMessageDialog(this,
                message,
                "Simulation Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private Project getCurrentProjectFromUI() {
        String projectName = projectPanel.list.getSelectedValue(); // selected project
        if (projectName == null || projectName.startsWith("   → ")) {
            JOptionPane.showMessageDialog(this, "Select a project first!");
            return null;
        }

        Project project = new Project(projectName);

        List<String> circuits = projectCircuits.get(projectName);
        if (circuits != null) {
            for (String circuitName : circuits) {
                String key = projectName + ":" + circuitName;
                Circuit cir = circuitObjects.get(key);
                if (cir != null)
                    project.getCircuits().add(cir);
            }
        }

        return project;
    }

    private int getSelectedProjectId() {
        String projectName = projectPanel.list.getSelectedValue();
        if (projectName == null || projectName.startsWith("   → ")) {
            JOptionPane.showMessageDialog(this, "Select a project first!");
            return -1;
        }

        EventHandlers handler = new EventHandlers();
        Hashtable<String, String> projectData = handler.getProjectByName(projectName);
        if (projectData == null) {
            JOptionPane.showMessageDialog(this, "Project not found!");
            return -1;
        }

        return Integer.parseInt(projectData.get("Id"));
    }

    private void loadProjectIntoUI(Project project) {
        if (project == null) {
            System.out.println("LoadProjectIntoUI: Project is null");
            return;
        }

        System.out.println("Loading project into UI: " + project.getName());

        String projectName = project.getName();

        // Check if project already exists in UI
        if (projectCircuits.containsKey(projectName)) {
            int option = JOptionPane.showConfirmDialog(this,
                    "Project '" + projectName + "' already exists. Do you want to reload it?",
                    "Project Exists",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (option != JOptionPane.YES_OPTION) {
                return; // User chose not to reload
            }

            // Remove existing project data
            removeProjectFromUI(projectName);
        }

        // Add project to UI (appends to existing projects)
        projectPanel.model.addElement(projectName);
        List<String> circuitsList = new ArrayList<>();
        projectCircuits.put(projectName, circuitsList);

        // Add circuits to UI
        for (Circuit circuit : project.getCircuits()) {
            String circuitName = circuit.getName();
            String key = projectName + ":" + circuitName;

            // Store the circuit object
            circuitObjects.put(key, circuit);
            circuitsList.add(circuitName);

            // Add to project panel
            projectPanel.model.addElement("   → " + circuitName);

            System.out.println("Added circuit to UI: " + circuitName);
        }

        // Auto-select the loaded project and first circuit
        selectedProject = projectName;
        if (!project.getCircuits().isEmpty()) {
            Circuit firstCircuit = project.getCircuits().get(0);
            currentCircuitName = firstCircuit.getName();

            // Load the first circuit into the circuit panel
            String key = projectName + ":" + currentCircuitName;
            Circuit circuit = circuitObjects.get(key);
            if (circuit != null) {
                circuitPanel.setCurrentCircuit(circuit);
                circuitPanel.addCircuitComponents(circuit);
                circuitPanel.refreshCircuitLabel();
                gateToolbar.setCurrentCircuit(circuit);
                System.out.println("Auto-loaded circuit: " + currentCircuitName);
            }
        } else {
            // No circuits in project
            currentCircuitName = null;
            circuitPanel.setCurrentCircuit(null);
            circuitPanel.showCircuit("Project loaded - No circuits");
            System.out.println("Project loaded but no circuits found");
        }

        // Select the loaded project in the list
        projectPanel.list.setSelectedValue(projectName, true);

        // Refresh the UI
        revalidate();
        repaint();

        System.out.println("Project loading completed: " + projectName);
    }

    /**
     * Remove existing project data from UI
     */
    private void removeProjectFromUI(String projectName) {
        // Remove from projectCircuits map
        List<String> circuits = projectCircuits.remove(projectName);

        // Remove from circuitObjects map
        if (circuits != null) {
            for (String circuitName : circuits) {
                String key = projectName + ":" + circuitName;
                circuitObjects.remove(key);
                circuitsPlacedInCurrentDesign.remove(circuitName);
            }
        }

        // Remove from JList model
        DefaultListModel<String> model = projectPanel.model;
        int projectIndex = model.indexOf(projectName);
        if (projectIndex >= 0) {
            // Remove all circuits under this project first
            int i = projectIndex + 1;
            while (i < model.size() && model.get(i).startsWith("   → ")) {
                model.remove(i);
                // Don't increment i because size decreases
            }
            // Remove the project itself
            model.remove(projectIndex);
        }

        // Clear circuit panel if the removed project was selected
        if (selectedProject != null && selectedProject.equals(projectName)) {
            circuitPanel.clearCircuit();
            selectedProject = null;
            currentCircuitName = null;
        }
    }

    private void refreshProjectListUI() {
        projectPanel.model.clear();
        for (String projectName : projectCircuits.keySet()) {
            projectPanel.model.addElement(projectName);
            List<String> circuits = projectCircuits.get(projectName);
            if (circuits != null) {
                for (String cir : circuits) {
                    projectPanel.model.addElement("   → " + cir);
                }
            }
        }
    }

    /**
     * Get the circuit panel instance for export functionality
     */
    public DesignArea getCircuitPanel() {
        return this.circuitPanel;
    }

    /**
     * Connect another circuit as a component in the current circuit
     * This creates a hierarchical design where one circuit acts as a module in
     * another
     */
    /**
     * Connect another circuit as a component in the current circuit
     * This creates a hierarchical design where one circuit acts as a module in
     * another
     */
    private void connectCircuitAsComponent() {
        if (selectedProject == null) {
            JOptionPane.showMessageDialog(this, "Select a project first.");
            return;
        }
        if (currentCircuitName == null) {
            JOptionPane.showMessageDialog(this, "Open a circuit first.");
            return;
        }

        List<String> circuitsInProject = projectCircuits.get(selectedProject);
        if (circuitsInProject == null || circuitsInProject.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No circuits in this project.");
            return;
        }

        // Prepare available circuits (exclude current circuit)
        List<String> availableCircuits = new ArrayList<>();
        for (String circuitName : circuitsInProject) {
            if (!circuitName.equals(currentCircuitName)) {
                availableCircuits.add(circuitName);
            }
        }

        if (availableCircuits.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No other circuits available to connect.\nCreate another circuit first.",
                    "No Circuits Available",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Create selection dialog
        JDialog connectDialog = new JDialog(this, "Connect Circuit as Component", true);
        connectDialog.setLayout(new BorderLayout());
        connectDialog.setSize(400, 300);
        connectDialog.setLocationRelativeTo(this);

        JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        dialogPanel.setBackground(new Color(240, 248, 255));

        // Title
        JLabel titleLabel = new JLabel("Select a circuit to use as component in: " + currentCircuitName);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(70, 130, 180));
        dialogPanel.add(titleLabel, BorderLayout.NORTH);

        // Circuit list
        DefaultListModel<String> circuitListModel = new DefaultListModel<>();
        for (String circuitName : availableCircuits) {
            circuitListModel.addElement(circuitName);

            // Show circuit details
            String key = selectedProject + ":" + circuitName;
            Circuit circuit = circuitObjects.get(key);
            if (circuit != null) {
                int inputCount = countCircuitInputs(circuit);
                int outputCount = countCircuitOutputs(circuit);
                circuitListModel.addElement("    → Inputs: " + inputCount + ", Outputs: " + outputCount);
            }
        }

        JList<String> circuitList = new JList<>(circuitListModel);
        circuitList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        circuitList.setFont(new Font("Arial", Font.PLAIN, 12));
        circuitList.setBackground(Color.WHITE);

        // Only allow selection of circuit names (not details)
        circuitList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value.toString().startsWith("    →")) {
                    setForeground(Color.GRAY);
                    setFont(getFont().deriveFont(Font.ITALIC, 11f));
                    if (isSelected) {
                        setBackground(list.getSelectionBackground());
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(circuitList);
        scrollPane.setPreferredSize(new Dimension(350, 150));
        dialogPanel.add(scrollPane, BorderLayout.CENTER);

        // Info panel
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(new Color(245, 245, 245));
        infoArea.setBorder(BorderFactory.createTitledBorder("Circuit Info"));
        infoArea.setFont(new Font("Arial", Font.PLAIN, 11));
        infoArea.setText("Selected circuit will appear as a component box with input/output pins.\n" +
                "You can connect its pins to other gates in the current circuit.");
        dialogPanel.add(infoArea, BorderLayout.SOUTH);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(240, 248, 255));

        JButton connectButton = createDialogButton("Connect as Component");
        JButton cancelButton = createDialogButton("Cancel");

        buttonPanel.add(connectButton);
        buttonPanel.add(cancelButton);

        // Add components to dialog
        connectDialog.add(dialogPanel, BorderLayout.CENTER);
        connectDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Connect button action
        connectButton.addActionListener(connectEvent -> {
            String selectedCircuitName = circuitList.getSelectedValue();
            if (selectedCircuitName == null || selectedCircuitName.startsWith("    →")) {
                JOptionPane.showMessageDialog(connectDialog,
                        "Please select a circuit name (not the details line).",
                        "Invalid Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Get the circuit to connect
            String keyToConnect = selectedProject + ":" + selectedCircuitName;
            Circuit circuitToConnect = circuitObjects.get(keyToConnect);

            // Get the current circuit
            String currentKey = selectedProject + ":" + currentCircuitName;
            Circuit currentCircuit = circuitObjects.get(currentKey);

            if (circuitToConnect != null && currentCircuit != null) {
                // Add the circuit as a component to the current circuit
                boolean success = addCircuitAsComponent(currentCircuit, circuitToConnect, selectedCircuitName);
                if (success) {
                    // Refresh the circuit panel to show the new component
                    circuitPanel.setCurrentCircuit(currentCircuit);
                    circuitPanel.addCircuitComponents(currentCircuit);

                    connectDialog.dispose();
                    JOptionPane.showMessageDialog(this,
                            "Circuit '" + selectedCircuitName + "' added as component!\n" +
                                    "It will appear as a box with input/output pins that you can connect to other gates.",
                            "Circuit Connected",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(connectDialog,
                            "Failed to add circuit as component.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Cancel button action
        cancelButton.addActionListener(cancelEvent -> {
            connectDialog.dispose();
        });

        // Double-click to connect
        circuitList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selected = circuitList.getSelectedValue();
                    if (selected != null && !selected.startsWith("    →")) {
                        connectButton.doClick();
                    }
                }
            }
        });

        connectDialog.setVisible(true);
    }

    /**
     * Add a circuit as a component to another circuit
     */
    private boolean addCircuitAsComponent(Circuit parentCircuit, Circuit childCircuit, String childCircuitName) {
        try {
            // Create a special component that represents the child circuit
            CircuitComponent circuitComponent = new CircuitComponent(childCircuit, childCircuitName);

            // Position it at a reasonable location
            int x = 100 + (parentCircuit.getGates().size() * 120) % 600;
            int y = 100 + (parentCircuit.getGates().size() * 80) % 400;
            circuitComponent.setPosition(x, y);

            // Add to parent circuit
            parentCircuit.addGate(circuitComponent);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Count the number of input pins a circuit would have as a component
     */
    /**
     * Count the number of input pins a circuit would have as a component
     */
    /**
     * Count the number of input pins a circuit would have as a component
     */
    private int countCircuitInputs(Circuit circuit) {
        if (circuit == null)
            return 0;
        return circuit.countCircuitInputs();
    }

    /**
     * Count the number of output pins a circuit would have as a component
     */
    private int countCircuitOutputs(Circuit circuit) {
        if (circuit == null)
            return 0;
        return circuit.countCircuitOutputs();
    }

    private void simulateCircuit() {
        if (selectedProject == null || currentCircuitName == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a project and open a circuit first.",
                    "No Circuit Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String key = selectedProject + ":" + currentCircuitName;
        Circuit circuit = circuitObjects.get(key);

        if (circuit == null || circuit.getGates().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "The circuit is empty. Add some gates first.",
                    "Empty Circuit",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            // Evaluate the entire circuit (this will handle circuit components internally)
            circuit.evaluate();

            // Get the final output
            boolean finalOutput = getFinalCircuitOutput(circuit);

            // Display results
            showFinalOutput(finalOutput, circuit.getName());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error during simulation: " + ex.getMessage(),
                    "Simulation Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void clearCurrentUI() {
        // Clear the mapping
        componentIdToUIMap.clear();

        // Remove all wire UI components
        for (DrawWire wireUI : wireUIComponents) {
            remove(wireUI);
        }
        wireUIComponents.clear();

        // Remove all gate UI components
        Component[] components = getContentPane().getComponents();
        for (Component comp : components) {
            if (comp instanceof DrawGates) {
                remove(comp);
            }
        }

        // Clear current circuit reference if you have one
        currentCircuit = null;

        // Single repaint after clearing
        revalidate();
        repaint();
    }

    private void loadCircuitIntoUI(Circuit circuit) {
        if (circuit == null)
            return;

        // Store reference to current circuit
        currentCircuit = circuit;

        // STEP 1: Load all components first and create UI for them
        for (ComponentBase component : circuit.getGates()) {
            createGateUI(component);
        }

        // STEP 2: Load all connectors after components are created
        for (Connector connector : circuit.getWires()) {
            createWireUI(connector);
        }

        // STEP 3: Single repaint after everything is loaded
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }

    private void createWireUI(Connector connector) {
        if (connector == null)
            return;

        ComponentBase fromGate = connector.getFromGate();
        ComponentBase toGate = connector.getToGate();

        if (fromGate == null || toGate == null)
            return;

        // Get the UI components for the connected gates
        DrawGates sourceGateUI = componentIdToUIMap.get(fromGate.getId());
        DrawGates targetGateUI = componentIdToUIMap.get(toGate.getId());

        if (sourceGateUI != null && targetGateUI != null) {
            // Create the wire UI component
            DrawWire wireUI = new DrawWire(
                    connector,
                    sourceGateUI,
                    connector.getFromPort(),
                    targetGateUI,
                    connector.getToPort());

            // Add to UI
            add(wireUI);
            wireUIComponents.add(wireUI);

            // Set wire to back so gates appear on top
            setComponentZOrder(wireUI, getComponentCount() - 1);

            // Update connector with UI references for position updates
            connector.setFromGateUI(sourceGateUI);
            connector.setToGateUI(targetGateUI);
        }
    }

    private void loadConnectorIntoUI(Connector connector) {
        if (connector == null)
            return;

        // Find the UI components for the connected gates
        DrawGates sourceGateUI = componentToUIMap.get(connector.getFromGate().getId());
        DrawGates targetGateUI = componentToUIMap.get(connector.getToGate().getId());

        if (sourceGateUI != null && targetGateUI != null) {
            // Create the wire UI component
            DrawWire wire = new DrawWire(connector, sourceGateUI,
                    connector.getFromPort(), targetGateUI, connector.getToPort());
            add(wire);

            // Set wire to back so gates appear on top
            setComponentZOrder(wire, getComponentCount() - 1);

            // Update connector with UI references
            connector.setFromGateUI(sourceGateUI);
            connector.setToGateUI(targetGateUI);
        }
    }

    // Helper map to track component-UI relationships
    private Map<String, DrawGates> componentToUIMap = new HashMap<>();

    private DrawGates createGateUI(ComponentBase component) {
        DrawGates gateUI = new DrawGates(component);
        gateUI.setLocation(component.getPosition());
        return gateUI;
    }

    private boolean getFinalCircuitOutput(Circuit circuit) {
        // Look for gates that have no outgoing connections (final outputs)
        for (ComponentBase gate : circuit.getGates()) {
            boolean hasOutputConnections = false;

            for (Connector wire : circuit.getWires()) {
                if (wire.getFromGate() == gate) {
                    hasOutputConnections = true;
                    break;
                }
            }

            // If no output connections, this is likely a final output
            if (!hasOutputConnections && gate.getOutputs() > 0) {
                return gate.getOutputValue(0);
            }
        }

        // Fallback: return first gate's output
        if (!circuit.getGates().isEmpty()) {
            return circuit.getGates().get(0).getOutputValue(0);
        }

        return false;
    }

    /**
     * Remove a circuit from all other circuits that use it as a component
     */
    private void removeCircuitFromAllParentCircuits(String circuitNameToRemove) {
        System.out.println("DEBUG: Removing circuit '" + circuitNameToRemove + "' from all parent circuits");

        // Iterate through all projects and circuits
        for (Map.Entry<String, List<String>> projectEntry : projectCircuits.entrySet()) {
            String projectName = projectEntry.getKey();
            List<String> circuits = projectEntry.getValue();

            for (String circuitName : circuits) {
                // Skip the circuit we're removing
                if (circuitName.equals(circuitNameToRemove)) {
                    continue;
                }

                // Get the circuit object
                String key = projectName + ":" + circuitName;
                Circuit circuit = circuitObjects.get(key);

                if (circuit != null) {
                    // Look for CircuitComponent gates that reference the circuit to remove
                    List<ComponentBase> gatesToRemove = new ArrayList<>();

                    for (ComponentBase gate : circuit.getGates()) {
                        if (gate instanceof CircuitComponent) {
                            CircuitComponent circuitComp = (CircuitComponent) gate;
                            String referencedCircuitName = circuitComp.getCircuitDisplayName();

                            if (circuitNameToRemove.equals(referencedCircuitName)) {
                                gatesToRemove.add(gate);
                                System.out.println("DEBUG: Found and marking for removal: " +
                                        circuitComp.getCircuitDisplayName() + " from circuit " + circuitName);
                            }
                        }
                    }

                    // Remove the found CircuitComponent gates
                    for (ComponentBase gateToRemove : gatesToRemove) {
                        circuit.removeGate(gateToRemove);
                        System.out.println("DEBUG: Removed circuit component from " + circuitName);
                    }

                    // If this is the currently displayed circuit, refresh the display
                    if (circuitName.equals(currentCircuitName) && selectedProject != null
                            && selectedProject.equals(projectName)) {
                        circuitPanel.addCircuitComponents(circuit);
                        circuitPanel.repaint();
                    }
                }
            }
        }

        System.out.println("DEBUG: Finished removing circuit '" + circuitNameToRemove + "' from all parent circuits");
    }

    /**
     * Check if a circuit is being used by any other circuits as a component
     */
    private boolean isCircuitUsedByOthers(String circuitName) {
        for (Map.Entry<String, List<String>> projectEntry : projectCircuits.entrySet()) {
            String projectName = projectEntry.getKey();
            List<String> circuits = projectEntry.getValue();

            for (String otherCircuitName : circuits) {
                if (otherCircuitName.equals(circuitName)) {
                    continue; // Skip self
                }

                String key = projectName + ":" + otherCircuitName;
                Circuit circuit = circuitObjects.get(key);

                if (circuit != null) {
                    for (ComponentBase gate : circuit.getGates()) {
                        if (gate instanceof CircuitComponent) {
                            CircuitComponent circuitComp = (CircuitComponent) gate;
                            if (circuitName.equals(circuitComp.getCircuitDisplayName())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MyWindow::new);
    }
}