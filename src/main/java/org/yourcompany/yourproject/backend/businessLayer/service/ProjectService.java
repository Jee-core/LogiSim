package org.yourcompany.yourproject.backend.businessLayer.service;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.yourcompany.yourproject.backend.dataAccessLayer.dao.DataBaseHandler;

public class ProjectService {

    public final DataBaseHandler db = new DataBaseHandler();

    // ===== CREATE OPERATIONS =====

    // Save a project and return true if successful
    public boolean storeProject(String name) {
        System.out.println("=== DEBUG ProjectService.saveProject ===");
        System.out.println("Name: " + name);

        Hashtable<String, String> project = new Hashtable<>();
        project.put("table", "projects");
        project.put("name", name);

        System.out.println("Calling DBDAO.save with data: " + project);
        int id = db.save(project);
        System.out.println("DBDAO.save returned ID: " + id);

        boolean success = id > 0;
        System.out.println("Save operation result: " + success);
        System.out.println("=== END DEBUG ===\n");

        return success;
    }

    // Add a component and return true if successful
    public boolean insertComponent(int circuitId, String name, String type, int inputs, int outputs, int positionX,
            int positionY, String componentId) {
        System.out.println("=== DEBUG ProjectService.addComponent ===");
        System.out.println("CircuitID: " + circuitId + ", Name: " + name + ", Type: " + type);
        System.out.println("Inputs: " + inputs + ", Outputs: " + outputs);
        System.out.println("Position: (" + positionX + "," + positionY + "), ComponentID: " + componentId);

        Hashtable<String, String> component = new Hashtable<>();
        component.put("table", "components");
        component.put("circuit_id", String.valueOf(circuitId));
        component.put("name", name);
        component.put("type", type);
        component.put("inputs", String.valueOf(inputs));
        component.put("outputs", String.valueOf(outputs));
        component.put("position_x", String.valueOf(positionX));
        component.put("position_y", String.valueOf(positionY));
        component.put("id", componentId);

        System.out.println("Calling DBDAO.save with data: " + component);
        int id = db.save(component);
        System.out.println("DBDAO.save returned ID: " + id);

        boolean success = id > 0;
        System.out.println("Add component result: " + success);
        System.out.println("=== END DEBUG ===\n");

        return success;
    }

    // Add a circuit and return true if successful
    public boolean addCircuit(int projectId, String name) {
        System.out.println("=== DEBUG ProjectService.addCircuit ===");
        System.out.println("ProjectID: " + projectId + ", Name: " + name);

        Hashtable<String, String> circuit = new Hashtable<>();
        circuit.put("table", "circuits");
        circuit.put("project_id", String.valueOf(projectId));
        circuit.put("name", name);

        System.out.println("Calling DBDAO.save with data: " + circuit);
        int id = db.save(circuit);
        System.out.println("DBDAO.save returned ID: " + id);

        boolean success = id > 0;
        System.out.println("Add circuit result: " + success);
        System.out.println("=== END DEBUG ===\n");

        return success;
    }

    // Add a connector and return true if successful
    public boolean addConnectingWire(int circuitId, String name, String color, Integer fromComponentId, int fromPort,
            Integer toComponentId, int toPort,
            boolean signalValue, String connectorId) {
        System.out.println("=== DEBUG ProjectService.addConnector ===");
        System.out.println("CircuitID: " + circuitId + ", Name: " + name + ", Color: " + color);
        System.out.println("From Component: " + fromComponentId + " Port: " + fromPort);
        System.out.println("To Component: " + toComponentId + " Port: " + toPort);
        System.out.println("Signal Value: " + signalValue + ", ConnectorID: " + connectorId);

        Hashtable<String, String> connector = new Hashtable<>();
        connector.put("table", "connectors");
        connector.put("circuit_id", String.valueOf(circuitId));
        connector.put("name", name);
        connector.put("color", color);

        if (fromComponentId != null)
            connector.put("from_component_id", String.valueOf(fromComponentId));
        connector.put("from_port", String.valueOf(fromPort));

        if (toComponentId != null)
            connector.put("to_component_id", String.valueOf(toComponentId));
        connector.put("to_port", String.valueOf(toPort));
        connector.put("signal_value", String.valueOf(signalValue));
        connector.put("id", connectorId);

        System.out.println("Calling DBDAO.save with data: " + connector);
        int id = db.save(connector);
        System.out.println("DBDAO.save returned ID: " + id);

        boolean success = id > 0;
        System.out.println("Add connector result: " + success);
        System.out.println("=== END DEBUG ===\n");

        return success;
    }

    // Add a component value and return true if successful
    public boolean addComponentVal(int componentId, int portIndex, boolean value, String type) {
        System.out.println("=== DEBUG ProjectService.addComponentValue ===");
        System.out.println("ComponentID: " + componentId + ", PortIndex: " + portIndex);
        System.out.println("Value: " + value + ", Type: " + type);

        Hashtable<String, String> componentValue = new Hashtable<>();
        componentValue.put("table", "component_values");
        componentValue.put("component_id", String.valueOf(componentId));
        componentValue.put("port_index", String.valueOf(portIndex));
        componentValue.put("value", String.valueOf(value));
        componentValue.put("type", type);

        System.out.println("Calling DBDAO.save with data: " + componentValue);
        int id = db.save(componentValue);
        System.out.println("DBDAO.save returned ID: " + id);

        boolean success = id > 0;
        System.out.println("Add component value result: " + success);
        System.out.println("=== END DEBUG ===\n");

        return success;
    }

    // ===== READ OPERATIONS - SPECIFIC ENTITIES =====

    // Load circuits by project
    public ArrayList<Hashtable<String, String>> fetchCircuitsForProject(int projectId) {
        System.out.println("=== DEBUG ProjectService.loadCircuitsByProject ===");
        System.out.println("ProjectID: " + projectId);

        ArrayList<Hashtable<String, String>> result = db.loadCircuitsByProject(projectId);

        System.out.println("Found " + result.size() + " circuits");

        for (int i = 0; i < result.size(); i++) {
            System.out.println("Circuit " + i + ": " + result.get(i));
        }
        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    // Load components by circuit - USING GENERIC METHOD SINCE DBDAO DOESN'T HAVE
    // SPECIFIC ONE
    public ArrayList<Hashtable<String, String>> fetchComponentsForCircuit(int circuitId) {
        System.out.println("=== DEBUG ProjectService.loadComponentsByCircuit ===");
        System.out.println("CircuitID: " + circuitId);

        ArrayList<Hashtable<String, String>> result = db.loadByForeignKey("components", "circuit_id", circuitId);

        System.out.println("Found " + result.size() + " components");

        for (int i = 0; i < result.size(); i++) {

            System.out.println("Component " + i + ": " + result.get(i));
        }

        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    // Load connectors by circuit - USING GENERIC METHOD SINCE DBDAO DOESN'T HAVE
    // SPECIFIC ONE
    public ArrayList<Hashtable<String, String>> loadConnectorsByCircuit(int circuitId) {
        System.out.println("=== DEBUG ProjectService.loadConnectorsByCircuit ===");
        System.out.println("CircuitID: " + circuitId);

        ArrayList<Hashtable<String, String>> result = db.loadByForeignKey("connectors", "circuit_id", circuitId);

        System.out.println("Found " + result.size() + " connectors");
        for (int i = 0; i < result.size(); i++) {
            System.out.println("Connector " + i + ": " + result.get(i));
        }

        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    // Load component values by component - USING GENERIC METHOD SINCE DBDAO DOESN'T
    // HAVE SPECIFIC ONE
    public ArrayList<Hashtable<String, String>> fetchValuesForComponent(int componentId) {
        System.out.println("=== DEBUG ProjectService.loadComponentValuesByComponent ===");
        System.out.println("ComponentID: " + componentId);

        ArrayList<Hashtable<String, String>> result = db.loadByForeignKey("component_values", "component_id",
                componentId);

        System.out.println("Found " + result.size() + " component values");

        for (int i = 0; i < result.size(); i++) {
            System.out.println("Component Value " + i + ": " + result.get(i));
        }

        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    // Load a project with its circuits
    public Hashtable<String, String> loadProject(int projectId) {
        System.out.println("=== DEBUG ProjectService.loadProject ===");
        System.out.println("Loading project ID: " + projectId);

        String projectKey = "projects:" + projectId;
        System.out.println("Calling DBDAO.load with key: " + projectKey);

        Hashtable<String, String> project = db.load(projectKey);

        if (project == null) {
            System.out.println("Project not found!");
            System.out.println("=== END DEBUG ===\n");
            return null;
        }

        System.out.println("Project loaded: " + project);

        ArrayList<Hashtable<String, String>> circuits = db.loadCircuitsByProject(projectId);
        project.put("CircuitsCount", String.valueOf(circuits.size()));

        System.out.println("Added CircuitsCount: " + circuits.size());
        System.out.println("Final project data: " + project);
        System.out.println("=== END DEBUG ===\n");

        return project;
    }

    // ===== READ OPERATIONS - GENERIC METHODS =====

    // Generic foreign key loader
    public List<Hashtable<String, String>> fetchByForeignKey(String table, String column, int value) {
        System.out.println("=== DEBUG ProjectService.loadByForeignKey ===");
        System.out.println("Table: " + table + ", Column: " + column + ", Value: " + value);

        List<Hashtable<String, String>> result = db.loadByForeignKey(table, column, value);

        System.out.println("Found " + result.size() + " records");

        for (int i = 0; i < result.size(); i++) {
            System.out.println("Record " + i + ": " + result.get(i));
        }
        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    // Load a single entity by ID
    public Hashtable<String, String> load(String table, int id) {
        System.out.println("=== DEBUG ProjectService.load ===");
        System.out.println("Table: " + table + ", ID: " + id);

        String key = table + ":" + id;
        System.out.println("Calling DBDAO.load with key: " + key);

        Hashtable<String, String> result = db.load(key);

        if (result == null) {
            System.out.println("Record not found!");
        } else {
            System.out.println("Record loaded: " + result);
        }

        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    // Load all entities from a table
    public ArrayList<Hashtable<String, String>> fetchAllRecords(String table) {
        System.out.println("=== DEBUG ProjectService.loadAll ===");
        System.out.println("Table: " + table);

        ArrayList<Hashtable<String, String>> result = db.loadAll(table);

        System.out.println("Found " + result.size() + " records");

        for (int i = 0; i < result.size(); i++) {
            System.out.println("Record " + i + ": " + result.get(i));
        }

        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    // ===== DELETE OPERATIONS =====

    // Delete entities
    public boolean delEntireProject(int projectId) {
        System.out.println("=== DEBUG ProjectService.deleteProject ===");
        System.out.println("Deleting project ID: " + projectId);

        String key = "projects:" + projectId;
        System.out.println("Calling DBDAO.delete with key: " + key);

        boolean result = db.delete(key);
        System.out.println("Delete result: " + result);
        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    public boolean deleteCircuit(int circuitId) {
        System.out.println("=== DEBUG ProjectService.deleteCircuit ===");
        System.out.println("Deleting circuit ");

        String key = "circuits:" + circuitId;
        System.out.println("Calling DBDAO.delete with key: " + key);

        boolean result = db.delete(key);
        System.out.println("Delete result: " + result);
        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    public boolean deleteComponent(int componentId) {
        System.out.println("=== DEBUG ProjectService.deleteComponent ===");
        System.out.println("Deleting component ID: " + componentId);

        String key = "components:" + componentId;
        System.out.println("Calling DBDAO.delete with key: " + key);

        boolean result = db.delete(key);
        System.out.println("Delete result: " + result);
        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    public boolean deleteConnectingWire(int connectorId) {
        System.out.println("=== DEBUG ProjectService.deleteConnector ===");
        System.out.println("Deleting connector ID: " + connectorId);

        String key = "connectors:" + connectorId;
        System.out.println("Calling DBDAO.delete with key: " + key);

        boolean result = db.delete(key);
        System.out.println("Delete result: " + result);
        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    // ===== READ OPERATIONS - GET ALL ENTITIES =====

    // Get all entities
    public ArrayList<Hashtable<String, String>> getAllComponentVal() {
        System.out.println("=== DEBUG ProjectService.getAllComponentValues ===");

        ArrayList<Hashtable<String, String>> result = db.loadAll("component_values");

        System.out.println("Found " + result.size() + " component values");

        for (int i = 0; i < result.size(); i++) {
            System.out.println("Component Value " + i + ": " + result.get(i));
        }
        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    public ArrayList<Hashtable<String, String>> fetchAllProjects() {
        System.out.println("=== DEBUG ProjectService.getAllProjects ===");

        ArrayList<Hashtable<String, String>> result = db.loadAll("projects");

        System.out.println("Found " + result.size() + " projects");
        for (int i = 0; i < result.size(); i++) {
            System.out.println("Project " + i + ": " + result.get(i));
        }
        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    public ArrayList<Hashtable<String, String>> getAllConnectingWires() {
        System.out.println("=== DEBUG ProjectService.getAllConnectors ===");

        ArrayList<Hashtable<String, String>> result = db.loadAll("connectors");

        System.out.println("Found " + result.size() + " connectors");
        for (int i = 0; i < result.size(); i++) {
            System.out.println("Connector " + i + ": " + result.get(i));
        }
        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    public ArrayList<Hashtable<String, String>> getAllCircuits() {
        System.out.println("=== DEBUG ProjectService.getAllCircuits ===");

        ArrayList<Hashtable<String, String>> result = db.loadAll("circuits");

        System.out.println("Found " + result.size() + " circuits");

        for (int i = 0; i < result.size(); i++) {
            System.out.println("Circuit " + i + ": " + result.get(i));
        }

        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    public ArrayList<Hashtable<String, String>> FetchAllComponents() {
        System.out.println("=== DEBUG ProjectService.getAllComponents ===");

        ArrayList<Hashtable<String, String>> result = db.loadAll("components");

        System.out.println("Found " + result.size() + " components");

        for (int i = 0; i < result.size(); i++) {
            System.out.println("Component " + i + ": " + result.get(i));

        }

        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    // ===== UPDATE OPERATIONS =====

    // Update methods

    public boolean updateComponentValue(int componentId, int portIndex, boolean value, String type) {
        System.out.println("=== DEBUG ProjectService.updateComponentValue ===");
        System.out.println("ComponentID: " + componentId + ", PortIndex: " + portIndex);
        System.out.println("Value: " + value + ", Type: " + type);

        boolean result = db.updateComponentValue(componentId, portIndex, value, type);
        System.out.println("Update result: " + result);
        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    public boolean posx(int compID, int positionX, int posy) {
        System.out.println("=== DEBUG ProjectService.updateComponentPosition ===");
        System.out.println("ComponentID: " + compID + ", New Position: (" + positionX + "," + posy + ")");

        boolean result = db.updateComponentPosition(compID, positionX, posy);
        System.out.println("Update result: " + result);
        System.out.println("=== END DEBUG ===\n");

        return result;
    }

    public boolean modifyConnectorSignal(int connectorId, boolean signalVal) {
        System.out.println("=== DEBUG ProjectService.updateConnectorSignal ===");
        System.out.println("ConnectorID: " + connectorId + ", Signal Value: " + signalVal);

        boolean result = db.updateConnectorSignal(connectorId, signalVal);
        System.out.println("Update result: " + result);
        System.out.println("=== END DEBUG ===\n");

        return result;
    }

}