package org.yourcompany.yourproject.backend.businessLayer.analysis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.yourcompany.yourproject.frontend.DesignArea;
import org.yourcompany.yourproject.backend.businessLayer.components.Circuit;
import org.yourcompany.yourproject.backend.businessLayer.components.GateComponent;
import org.yourcompany.yourproject.backend.businessLayer.components.Connector;
import org.yourcompany.yourproject.backend.businessLayer.components.Project;
import org.yourcompany.yourproject.backend.businessLayer.components.gates.andd;
import org.yourcompany.yourproject.backend.businessLayer.components.gates.orr;
import org.yourcompany.yourproject.backend.businessLayer.components.gates.nott;
import org.yourcompany.yourproject.backend.businessLayer.service.ProjectService;

public class EventHandlers {

    private final ProjectService projectService;
    private Map<GateComponent, Integer> componentToIdMap;
    private Map<Integer, GateComponent> idToComponentMap;
    private Map<Connector, Integer> connectorToIdMap;

    public EventHandlers() {
        this.projectService = new ProjectService();
        this.componentToIdMap = new HashMap<>();
        this.idToComponentMap = new HashMap<>();
        this.connectorToIdMap = new HashMap<>();
    }

    // =============================
    // SAVE PROJECT
    // =============================
    public boolean saveProject(Project project) {
        if (project == null) {
            System.out.println("SaveLoadHandler: Project is null");
            return false;
        }

        System.out.println("=== SAVE PROJECT: " + project.getName() + " ===");

        try {
            initializeMappings();

            // Step 1: Save the main project
            int projectId = saveProjectEntity(project);
            if (projectId == -1) {
                System.out.println("Failed to save project entity");
                return false;
            }

            // Step 2: Save all circuits
            return saveAllCircuits(projectId, project.getCircuits());

        } catch (Exception e) {
            handleException("Error saving project", e);
            return false;
        }
    }

    private void initializeMappings() {
        componentToIdMap.clear();
        idToComponentMap.clear();
        connectorToIdMap.clear();
    }

    private boolean saveAllCircuits(int projectId, List<Circuit> circuits) {
        if (circuits == null || circuits.isEmpty()) {
            System.out.println("No circuits to save");
            return true;
        }

        for (Circuit circuit : circuits) {
            if (!validateAndSaveCircuit(projectId, circuit)) {
                System.out.println("Failed to save circuit: " + circuit.getName());
                return false;
            }
        }
        return true;
    }

    private boolean validateAndSaveCircuit(int projectId, Circuit circuit) {
        if (circuit == null)
            return false;

        System.out.println("Saving circuit: " + circuit.getName());

        // Save circuit entity
        boolean circuitSuccess = projectService.addCircuit(projectId, circuit.getName());
        if (!circuitSuccess) {
            return false;
        }

        int circuitId = findCircuitIdByName(projectId, circuit.getName());
        if (circuitId == -1) {
            return false;
        }

        return saveCircuitComponentsAndConnectors(circuitId, circuit);
    }

    private boolean saveCircuitComponentsAndConnectors(int circuitId, Circuit circuit) {
        // Save components
        if (!saveAllComponents(circuitId, circuit.getGates())) {
            return false;
        }

        // Save connectors
        return saveAllConnectors(circuitId, circuit.getWires());
    }

    private boolean saveAllComponents(int circuitId, List<GateComponent> components) {
        if (components == null)
            return true;

        for (GateComponent component : components) {
            if (!saveComponentWithValidation(circuitId, component)) {
                System.out.println("Failed to save component: " + component.getName());
                return false;
            }
        }
        return true;
    }

    private boolean saveComponentWithValidation(int circuitId, GateComponent component) {
        if (component == null)
            return false;

        String componentId = generateComponentId(component);
        boolean componentSuccess = projectService.insertComponent(
                circuitId,
                component.getName(),
                getComponentType(component),
                component.getInputs(),
                component.getOutputs(),
                component.getPosition().x,
                component.getPosition().y,
                componentId);

        if (!componentSuccess) {
            return false;
        }

        int dbComponentId = findComponentIdByUniqueId(circuitId, componentId);
        if (dbComponentId == -1) {
            return false;
        }

        storeComponentMapping(component, dbComponentId);
        return true;
    }

    private void storeComponentMapping(GateComponent component, int dbComponentId) {
        componentToIdMap.put(component, dbComponentId);
        idToComponentMap.put(dbComponentId, component);
    }

    private boolean saveAllConnectors(int circuitId, List<Connector> connectors) {
        if (connectors == null)
            return true;

        for (Connector connector : connectors) {
            if (!saveConnectorWithValidation(circuitId, connector)) {
                System.out.println("Failed to save connector");
                return false;
            }
        }
        return true;
    }

    private boolean saveConnectorWithValidation(int circuitId, Connector connector) {
        if (!isConnectorValid(connector)) {
            System.out.println("Connector is invalid");
            return false;
        }

        Integer fromComponentId = componentToIdMap.get(connector.getFromGate());
        Integer toComponentId = componentToIdMap.get(connector.getToGate());

        if (!areComponentIdsValid(fromComponentId, toComponentId)) {
            System.out.println("Invalid component IDs for connector");
            return false;
        }

        return saveConnectorToDatabase(circuitId, connector, fromComponentId, toComponentId);
    }

    private boolean isConnectorValid(Connector connector) {
        return connector != null && connector.isConnected() &&
                connector.getFromGate() != null && connector.getToGate() != null;
    }

    private boolean areComponentIdsValid(Integer fromComponentId, Integer toComponentId) {
        return fromComponentId != null && toComponentId != null;
    }

    private boolean saveConnectorToDatabase(int circuitId, Connector connector, int fromComponentId,
            int toComponentId) {
        String connectorId = generateConnectorId(connector);
        String connectorName = connector.getName() != null ? connector.getName() : "Wire";

        boolean connSuccess = projectService.addConnectingWire(
                circuitId,
                connectorName,
                "BLACK",
                fromComponentId,
                connector.getSourcePortIndex(),
                toComponentId,
                connector.getDestinationPortIndex(),
                false,
                connectorId);

        if (connSuccess) {
            storeConnectorMapping(circuitId, connector, connectorId);
        }

        return connSuccess;
    }

    private void storeConnectorMapping(int circuitId, Connector connector, String connectorId) {
        int dbConnectorId = findConnectorIdByUniqueId(circuitId, connectorId);
        if (dbConnectorId != -1) {
            connectorToIdMap.put(connector, dbConnectorId);
        }
    }

    private int saveProjectEntity(Project project) {
        boolean success = projectService.storeProject(project.getName());
        return success ? findProjectIdByName(project.getName()) : -1;
    }

    private String generateComponentId(GateComponent component) {
        return component.getName() + "_" + component.getPosition().x + "_" +
                component.getPosition().y + "_" + System.currentTimeMillis();
    }

    private String getComponentType(GateComponent component) {
        if (component instanceof andd) {
            return "AND";
        } else if (component instanceof orr) {
            return "OR";
        } else if (component instanceof nott) {
            return "NOT";
        } else {
            return extractComponentTypeName(component);
        }
    }

    private String extractComponentTypeName(GateComponent component) {
        String className = component.getClass().getSimpleName().toUpperCase();
        return className.replace("GATE", "");
    }

    private String generateConnectorId(Connector connector) {
        return "Wire_" + connector.getSourcePortIndex() + "_to_" +
                connector.getDestinationPortIndex() + "_" + System.currentTimeMillis();
    }

    // =============================
    // LOAD PROJECT
    // =============================
    public Project loadProject(String projectName) {
        System.out.println("=== LOAD PROJECT: " + projectName + " ===");

        try {
            initializeMappings();

            int projectId = findProjectIdByName(projectName);
            if (projectId == -1) {
                System.out.println("Project not found: " + projectName);
                return null;
            }

            System.out.println("Found project ID: " + projectId);

            Hashtable<String, String> projectData = projectService.loadProject(projectId);
            if (projectData == null) {
                System.out.println("Failed to load project data");
                return null;
            }

            return createProjectWithCircuits(projectData, projectId);

        } catch (Exception e) {
            handleException("Error loading project", e);
            return null;
        }
    }

    private Project createProjectWithCircuits(Hashtable<String, String> projectData, int projectId) {
        Project project = new Project(projectData.get("name"));
        List<Circuit> circuits = loadCircuitsForProject(projectId);

        if (circuits != null && !circuits.isEmpty()) {
            addCircuitsToProject(project, circuits);
            System.out.println("Successfully loaded " + circuits.size() + " circuits");
        } else {
            System.out.println("No circuits loaded for project");
        }

        System.out.println("Project loaded successfully: " + project.getName() +
                " with " + project.getCircuits().size() + " circuits");
        return project;
    }

    private void addCircuitsToProject(Project project, List<Circuit> circuits) {
        for (Circuit circuit : circuits) {
            project.getCircuits().add(circuit);
            System.out.println("Added circuit to project: " + circuit.getName());
        }
    }

    private List<Circuit> loadCircuitsForProject(int projectId) {
        List<Circuit> circuits = new ArrayList<>();

        try {
            System.out.println("=== LOADING CIRCUITS FOR PROJECT: " + projectId + " ===");
            ArrayList<Hashtable<String, String>> circuitDataList = projectService.fetchCircuitsForProject(projectId);

            if (circuitDataList != null) {
                System.out.println("Found " + circuitDataList.size() + " circuits in database");
                loadAllCircuits(circuitDataList, circuits);
            } else {
                System.out.println("No circuit data returned from service");
            }

        } catch (Exception e) {
            handleException("Error loading circuits for project " + projectId, e);
        }

        return circuits;
    }

    private void loadAllCircuits(ArrayList<Hashtable<String, String>> circuitDataList, List<Circuit> circuits) {
        for (Hashtable<String, String> circuitData : circuitDataList) {
            int circuitId = Integer.parseInt(circuitData.get("circuit_id"));
            String circuitName = circuitData.get("name");
            System.out.println("Loading circuit: " + circuitName + " (ID: " + circuitId + ")");

            Circuit circuit = loadCircuit(circuitId);
            if (circuit != null) {
                circuits.add(circuit);
                System.out.println("✓ Successfully loaded circuit: " + circuitName);
            } else {
                System.out.println("✗ Failed to load circuit: " + circuitName);
            }
        }
    }

    private Circuit loadCircuit(int circuitId) {
        System.out.println("=== LOAD CIRCUIT ID: " + circuitId + " ===");

        try {
            String circuitName = findCircuitNameById(circuitId);
            if (circuitName == null) {
                System.out.println("Circuit name not found for ID: " + circuitId);
                return null;
            }

            System.out.println("Loading circuit: " + circuitName + " (ID: " + circuitId + ")");

            Circuit circuit = new Circuit(circuitName);
            loadCircuitComponents(circuitId, circuit);
            loadCircuitConnectors(circuitId, circuit);

            System.out.println("Circuit loaded successfully: " + circuitName);
            return circuit;

        } catch (Exception e) {
            handleException("Error loading circuit ID " + circuitId, e);
            return null;
        }
    }

    private void loadCircuitComponents(int circuitId, Circuit circuit) {
        List<GateComponent> components = loadComponentsForCircuit(circuitId);
        if (components != null && !components.isEmpty()) {
            System.out.println("Loaded " + components.size() + " components");
            addComponentsToCircuit(circuit, components);
        } else {
            System.out.println("No components found for circuit " + circuitId);
        }
    }

    private void addComponentsToCircuit(Circuit circuit, List<GateComponent> components) {
        for (GateComponent component : components) {
            circuit.addGateFunc(component);
        }
    }

    private void loadCircuitConnectors(int circuitId, Circuit circuit) {
        List<Connector> connectors = loadConnectorsForCircuit(circuitId);
        if (connectors != null && !connectors.isEmpty()) {
            System.out.println("Loaded " + connectors.size() + " connectors");
            addConnectorsToCircuit(circuit, connectors);
        } else {
            System.out.println("No connectors found for circuit " + circuitId);
        }
    }

    private void addConnectorsToCircuit(Circuit circuit, List<Connector> connectors) {
        for (Connector connector : connectors) {
            circuit.addConnectionWire(connector);
        }
    }

    private String findCircuitNameById(int circuitId) {
        try {
            ArrayList<Hashtable<String, String>> allProjects = projectService.fetchAllProjects();
            for (Hashtable<String, String> project : allProjects) {
                int projectId = Integer.parseInt(project.get("project_id"));
                ArrayList<Hashtable<String, String>> circuits = projectService.fetchCircuitsForProject(projectId);
                for (Hashtable<String, String> circuit : circuits) {
                    int currentCircuitId = Integer.parseInt(circuit.get("circuit_id"));
                    if (currentCircuitId == circuitId) {
                        return circuit.get("name");
                    }
                }
            }
        } catch (Exception e) {
            handleException("Error finding circuit name for ID " + circuitId, e);
        }
        return null;
    }

    private List<GateComponent> loadComponentsForCircuit(int circuitId) {
        List<GateComponent> components = new ArrayList<>();

        try {
            System.out.println("=== LOADING COMPONENTS FOR CIRCUIT: " + circuitId + " ===");
            ArrayList<Hashtable<String, String>> componentDataList = projectService
                    .fetchComponentsForCircuit(circuitId);

            if (componentDataList != null) {
                System.out.println("Found " + componentDataList.size() + " components in database");
                processAllComponents(componentDataList, components);
            } else {
                System.out.println("No components data returned from service");
            }

        } catch (Exception e) {
            handleException("Error loading components for circuit " + circuitId, e);
        }

        return components;
    }

    private void processAllComponents(ArrayList<Hashtable<String, String>> componentDataList,
            List<GateComponent> components) {
        for (Hashtable<String, String> componentData : componentDataList) {
            System.out.println("Component data: " + componentData);
            GateComponent component = loadComponent(componentData);
            if (component != null) {
                components.add(component);
                storeLoadedComponentMapping(componentData, component);
                System.out.println("✓ Loaded component: " + component.getName() + " (ID: " +
                        componentData.get("component_id") + ")");
            } else {
                System.out.println("✗ Failed to load component from data: " + componentData);
            }
        }
    }

    private void storeLoadedComponentMapping(Hashtable<String, String> componentData, GateComponent component) {
        int componentId = Integer.parseInt(componentData.get("component_id"));
        idToComponentMap.put(componentId, component);
        componentToIdMap.put(component, componentId);
    }

    private GateComponent loadComponent(Hashtable<String, String> componentData) {
        try {
            String type = componentData.get("type");
            String name = componentData.get("name");
            int inputs = Integer.parseInt(componentData.get("inputs"));
            int outputs = Integer.parseInt(componentData.get("outputs"));
            int x = Integer.parseInt(componentData.get("position_x"));
            int y = Integer.parseInt(componentData.get("position_y"));

            System.out.println("Loading component - Type: " + type + ", Name: " + name +
                    ", Inputs: " + inputs + ", Outputs: " + outputs +
                    ", Position: (" + x + ", " + y + ")");

            GateComponent component = createComponentByType(type, inputs, outputs);
            if (component != null) {
                configureComponentProperties(component, name, x, y, inputs, outputs, type);
                System.out.println(
                        "✓ Successfully loaded component: " + type + " '" + name + "' at (" + x + "," + y + ")");
            } else {
                System.err.println("✗ Failed to create component of type: " + type);
            }

            return component;

        } catch (Exception e) {
            handleException("Error loading component from data: " + componentData, e);
            return null;
        }
    }

    private void configureComponentProperties(GateComponent component, String name, int x, int y,
            int inputs, int outputs, String type) {
        component.setName(name);
        component.setPosition(new Point(x, y));

        // Validate input/output counts
        validateComponentConfiguration(component, inputs, outputs, type);
    }

    private void validateComponentConfiguration(GateComponent component, int savedInputs,
            int savedOutputs, String type) {
        if (component.getInputs() != savedInputs) {
            System.out.println("Warning: Input count mismatch for " + type +
                    " - saved: " + savedInputs + ", created: " + component.getInputs());
        }
        if (component.getOutputs() != savedOutputs) {
            System.out.println("Warning: Output count mismatch for " + type +
                    " - saved: " + savedOutputs + ", created: " + component.getOutputs());
        }
    }

    private GateComponent createComponentByType(String type, int inputs, int outputs) {
        try {
            switch (type.toUpperCase()) {
                case "AND":
                    return createAndGate();
                case "OR":
                    return createOrGate();
                case "NOT":
                    return createNotGate();
                case "INPUT":
                    return createInputComponent();
                case "OUTPUT":
                    return createOutputComponent();
                default:
                    return createGenericComponent(type, inputs, outputs);
            }
        } catch (Exception e) {
            handleException("Error creating component of type '" + type + "'", e);
            return createGenericComponent(type, inputs, outputs);
        }
    }

    private GateComponent createAndGate() {
        andd andGate = new andd();
        System.out.println("✓ Created AND gate with proper shape");
        return andGate;
    }

    private GateComponent createOrGate() {
        orr orGate = new orr();
        System.out.println("✓ Created OR gate with proper shape");
        return orGate;
    }

    private GateComponent createNotGate() {
        nott notGate = new nott();
        System.out.println("✓ Created NOT gate with proper shape");
        return notGate;
    }

    private GateComponent createInputComponent() {
        GateComponent input = createGenericComponent("INPUT", 0, 1);
        input.setName("INPUT");
        return input;
    }

    private GateComponent createOutputComponent() {
        GateComponent output = createGenericComponent("OUTPUT", 1, 0);
        output.setName("OUTPUT");
        return output;
    }

    private GateComponent createGenericComponent(String type, int numInputs, int numOutputs) {
        return new GateComponent(type, numInputs, numOutputs) {
            @Override
            protected void calculateOutput() {
                boolean inputValue = getInputs() > 0 ? getInputVal(0) : false;
                for (int i = 0; i < getOutputs(); i++) {
                    setOutputVal(i, inputValue);
                }
            }

            @Override
            public void evaluate() {
                calculateOutput();
            }

            @Override
            public GateComponent copy() {
                GateComponent copy = createGenericComponent(this.getName(), this.getInputs(), this.getOutputs());
                copy.setPosition(new Point(this.getPosition()));
                return copy;
            }

            @Override
            public void update() {
                evaluate();
            }
        };
    }

    private List<Connector> loadConnectorsForCircuit(int circuitId) {
        List<Connector> connectors = new ArrayList<>();

        try {
            System.out.println("=== LOADING CONNECTORS FOR CIRCUIT: " + circuitId + " ===");
            ArrayList<Hashtable<String, String>> connectorDataList = projectService.loadConnectorsByCircuit(circuitId);

            if (connectorDataList != null) {
                System.out.println("Found " + connectorDataList.size() + " connectors in database");
                processAllConnectors(connectorDataList, connectors);
            } else {
                System.out.println("No connectors data returned from service");
            }

        } catch (Exception e) {
            handleException("Error loading connectors for circuit " + circuitId, e);
        }

        return connectors;
    }

    private void processAllConnectors(ArrayList<Hashtable<String, String>> connectorDataList,
            List<Connector> connectors) {
        for (Hashtable<String, String> connectorData : connectorDataList) {
            System.out.println("Connector data: " + connectorData);
            Connector connector = loadConnector(connectorData);
            if (connector != null) {
                connectors.add(connector);
                System.out.println("✓ Loaded connector: " + connectorData.get("from_component_id") +
                        " -> " + connectorData.get("to_component_id"));
            } else {
                System.out.println("✗ Failed to load connector from data: " + connectorData);
            }
        }
    }

    private Connector loadConnector(Hashtable<String, String> connectorData) {
        try {
            int fromComponentId = Integer.parseInt(connectorData.get("from_component_id"));
            int fromPort = Integer.parseInt(connectorData.get("from_port"));
            int toComponentId = Integer.parseInt(connectorData.get("to_component_id"));
            int toPort = Integer.parseInt(connectorData.get("to_port"));

            GateComponent fromComponent = idToComponentMap.get(fromComponentId);
            GateComponent toComponent = idToComponentMap.get(toComponentId);

            if (!areComponentsValidForConnector(fromComponent, toComponent, fromComponentId, toComponentId)) {
                return null;
            }

            Connector connector = new Connector(fromComponent, fromPort, toComponent, toPort);
            System.out.println("Loaded connector: " + fromComponentId + ":" + fromPort +
                    " -> " + toComponentId + ":" + toPort);

            return connector;

        } catch (Exception e) {
            handleException("Error loading connector", e);
            return null;
        }
    }

    private boolean areComponentsValidForConnector(GateComponent fromComponent, GateComponent toComponent,
            int fromComponentId, int toComponentId) {
        if (fromComponent == null || toComponent == null) {
            System.out.println("Could not find components for connector: " +
                    fromComponentId + " -> " + toComponentId);
            System.out.println("Available component mappings: " + idToComponentMap.keySet());
            return false;
        }
        return true;
    }

    // =============================
    // UTILITY METHODS
    // =============================

    private void handleException(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }

    public Hashtable<String, String> getProjectByName(String projectName) {
        int projectId = findProjectIdByName(projectName);
        return projectId != -1 ? projectService.loadProject(projectId) : null;
    }

    public boolean deleteProject(String projectName) {
        int projectId = findProjectIdByName(projectName);
        return projectId != -1 && projectService.delEntireProject(projectId);
    }

    private int findProjectIdByName(String projectName) {
        ArrayList<Hashtable<String, String>> allProjects = projectService.fetchAllProjects();
        for (Hashtable<String, String> project : allProjects) {
            if (projectName.equals(project.get("name"))) {
                return Integer.parseInt(project.get("project_id"));
            }
        }
        return -1;
    }

    private int findCircuitIdByName(int projectId, String circuitName) {
        ArrayList<Hashtable<String, String>> circuits = projectService.fetchCircuitsForProject(projectId);
        for (Hashtable<String, String> circuit : circuits) {
            if (circuitName.equals(circuit.get("name"))) {
                return Integer.parseInt(circuit.get("circuit_id"));
            }
        }
        return -1;
    }

    private int findComponentIdByUniqueId(int circuitId, String uniqueId) {
        ArrayList<Hashtable<String, String>> components = projectService.fetchComponentsForCircuit(circuitId);
        for (Hashtable<String, String> component : components) {
            if (uniqueId.equals(component.get("id"))) {
                return Integer.parseInt(component.get("component_id"));
            }
        }
        return -1;
    }

    private int findConnectorIdByUniqueId(int circuitId, String uniqueId) {
        ArrayList<Hashtable<String, String>> connectors = projectService.loadConnectorsByCircuit(circuitId);
        for (Hashtable<String, String> connector : connectors) {
            if (uniqueId.equals(connector.get("id"))) {
                return Integer.parseInt(connector.get("connector_id"));
            }
        }
        return -1;
    }

    public List<String> getAllProjectNames() {
        List<String> projectNames = new ArrayList<>();

        try {
            ArrayList<Hashtable<String, String>> projects = projectService.fetchAllProjects();
            if (projects != null) {
                extractProjectNames(projects, projectNames);
            }

            System.out.println("Found " + projectNames.size() + " projects in database");
            return projectNames;

        } catch (Exception e) {
            handleException("Error getting project names", e);
            return new ArrayList<>();
        }
    }

    private void extractProjectNames(ArrayList<Hashtable<String, String>> projects, List<String> projectNames) {
        for (Hashtable<String, String> project : projects) {
            String projectName = project.get("name");
            if (isValidProjectName(projectName)) {
                projectNames.add(projectName);
            }
        }
    }

    private boolean isValidProjectName(String projectName) {
        return projectName != null && !projectName.trim().isEmpty();
    }

    public boolean projectExists(String projectName) {
        return findProjectIdByName(projectName) != -1;
    }

    public void clearMappings() {
        componentToIdMap.clear();
        idToComponentMap.clear();
        connectorToIdMap.clear();
    }

    public boolean updateComponentPosition(GateComponent component, int newX, int newY) {
        Integer componentId = componentToIdMap.get(component);
        return componentId != null && projectService.posx(componentId, newX, newY);
    }

    /**
     * Exports the current circuit as an image file
     */
    public void exportCircuit(String circuitName, DesignArea circuitPanel) {
        if (circuitPanel == null) {
            JOptionPane.showMessageDialog(null, "Circuit panel not provided!");
            return;
        }

        // Check if there's a circuit currently displayed
        if (circuitPanel.getCurrentCircuit() == null) {
            JOptionPane.showMessageDialog(null,
                    "No circuit is currently open in the design area!\n" +
                            "Please open a circuit first.",
                    "No Circuit",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Circuit as Image - " + circuitName);

        // Set default file name
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String defaultFileName = circuitName + "_" + timestamp;
        fileChooser.setSelectedFile(new File(defaultFileName));

        // Filter for image formats
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG Images (*.png)", "png"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG Images (*.jpg)", "jpg"));

        // Set PNG as default (faster than JPEG for circuit diagrams)
        fileChooser.setFileFilter(fileChooser.getChoosableFileFilters()[0]);

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String format = getFormatFromFileFilter(fileChooser.getFileFilter());

            // Ensure file extension
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith("." + format.toLowerCase())) {
                fileToSave = new File(filePath + "." + format);
            }

            // Check if file exists
            if (fileToSave.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(null,
                        "File already exists. Overwrite?",
                        "File Exists",
                        JOptionPane.YES_NO_OPTION);
                if (overwrite != JOptionPane.YES_OPTION)
                    return;
            }

            // Perform export in background thread
            performExportInBackground(circuitPanel, fileToSave, format, circuitName);
        }
    }

    /**
     * Performs the export operation in a background thread to avoid UI freezing
     */
    private void performExportInBackground(DesignArea circuitPanel, File file, String format, String circuitName) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return captureCircuitPanelOptimized(circuitPanel, file, format);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(null,
                                "Circuit '" + circuitName + "' exported successfully!\n" +
                                        "Location: " + file.getAbsolutePath(),
                                "Export Successful",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Failed to export circuit!",
                                "Export Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                            "Error during export: " + ex.getMessage(),
                            "Export Error",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    /**
     * Captures the circuit panel as an optimized image
     */
    private boolean captureCircuitPanelOptimized(DesignArea circuitPanel, File file, String format) {
        try {
            // OPTIMIZATION 1: Calculate the actual bounds of circuit components
            Rectangle contentBounds = calculateContentBounds(circuitPanel);

            // Add some padding around the content
            int padding = 50;
            int width = Math.max(contentBounds.width + padding * 2, 800);
            int height = Math.max(contentBounds.height + padding * 2, 600);

            // OPTIMIZATION 2: Create image with optimal size
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            // OPTIMIZATION 3: Set rendering hints for speed vs quality balance
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);

            // White background
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // OPTIMIZATION 4: Translate to center the content with padding
            g2d.translate(padding - contentBounds.x, padding - contentBounds.y);

            // OPTIMIZATION 5: Paint only the circuit content (not the entire panel)
            paintCircuitContent(circuitPanel, g2d);

            g2d.dispose();

            // OPTIMIZATION 6: Use faster compression for JPEG
            if ("jpg".equals(format)) {
                return ImageIO.write(image, "JPEG", file);
            } else {
                return ImageIO.write(image, format.toUpperCase(), file);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Calculate the bounds of actual circuit content (gates and wires)
     */
    private Rectangle calculateContentBounds(DesignArea circuitPanel) {
        Rectangle bounds = new Rectangle();

        // Get all gate components from the circuit panel
        java.awt.Component[] components = circuitPanel.getComponents();
        boolean first = true;

        for (java.awt.Component comp : components) {
            if (comp.isVisible() && comp.getBounds() != null && !comp.getBounds().isEmpty()) {
                Rectangle compBounds = comp.getBounds();
                if (first) {
                    bounds.setBounds(compBounds);
                    first = false;
                } else {
                    bounds.add(compBounds);
                }
            }
        }

        // If no components found, use default size
        if (first) {
            bounds.setBounds(0, 0, 800, 600);
        }

        // Ensure minimum size
        if (bounds.width < 400)
            bounds.width = 400;
        if (bounds.height < 300)
            bounds.height = 300;

        return bounds;
    }

    /**
     * Paint only the circuit content, not the entire panel background
     */
    private void paintCircuitContent(DesignArea circuitPanel, Graphics2D g2d) {
        // Paint grid background
        paintGridBackground(g2d, circuitPanel.getWidth(), circuitPanel.getHeight());

        // Paint all components
        circuitPanel.paint(g2d);
    }

    /**
     * Paint a simplified grid background
     */
    private void paintGridBackground(Graphics2D g2d, int width, int height) {
        g2d.setColor(new Color(240, 240, 240)); // Light gray grid

        int gridSize = 20;
        for (int x = 0; x < width; x += gridSize) {
            g2d.drawLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += gridSize) {
            g2d.drawLine(0, y, width, y);
        }
    }

    /**
     * Extracts the file format from the file filter
     */
    private String getFormatFromFileFilter(javax.swing.filechooser.FileFilter fileFilter) {
        String description = fileFilter.getDescription().toLowerCase();
        if (description.contains("jpeg") || description.contains("jpg"))
            return "jpg";
        if (description.contains("png"))
            return "png";
        return "png"; // default
    }

    /**
     * Export multiple circuits as separate images
     */
    public void exportAllCircuits(Project project, String baseDirectory) {
        if (project == null || project.getCircuits().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No circuits to export!");
            return;
        }

        File directory = new File(baseDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        int successCount = 0;
        for (Circuit circuit : project.getCircuits()) {
            // This would need access to a CircuitPanel - you might need to adapt this
            // based on how your UI is structured
            System.out.println("Exporting circuit: " + circuit.getName());
            // exportCircuit(circuit.getName(), getCircuitPanelForCircuit(circuit));
            successCount++;
        }

        JOptionPane.showMessageDialog(null,
                "Exported " + successCount + " out of " + project.getCircuits().size() + " circuits successfully!",
                "Bulk Export Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Quick export with default settings
     */
    public boolean quickExport(DesignArea circuitPanel, String circuitName, File outputFile) {
        if (circuitPanel == null || outputFile == null) {
            return false;
        }

        try {
            String format = outputFile.getName().toLowerCase().endsWith(".jpg") ? "jpg" : "png";
            return captureCircuitPanelOptimized(circuitPanel, outputFile, format);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}