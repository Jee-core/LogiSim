package org.yourcompany.yourproject.Backend.businessLayer.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yourcompany.yourproject.Backend.businessLayer.components.gates.LED;

public class Circuit 
{
    private String CircuitName;
    private List<GateComponent> circuitInputNodes = new ArrayList<>();
    private List<GateComponent> circuitOutputNodes = new ArrayList<>();
    
    private List<Connector> wires = new ArrayList<>();


private List<GateComponent> gates = new ArrayList<>();

    // ===== CONSTRUCTORS AND BASIC PROPERTIES =====
    
    // Constructor
    public Circuit(String name) {
        this.CircuitName = name;
    }

    // Getters and setters
    public String getName() {
        return CircuitName;
    }

    public void setName(String name) {
        this.CircuitName = name;
    }

    public List<GateComponent> getGates() {
        return gates;
    }

    public void setGates(List<GateComponent> gates) {
        this.gates = gates;
    }

    public List<Connector> getWires() {
        return wires;
    }

    public void setWires(List<Connector> wires) {
        this.wires = wires;
    }

    public List<GateComponent> getInputNodes() {
        return circuitInputNodes;
    }

    public List<GateComponent> getOutputNodes() {
        return circuitOutputNodes;
    }

    // ===== COMPONENT MANAGEMENT =====
    
    // Add a component (gate) to the circuit
    public void addGateFunc(GateComponent gate) {
        if (gate != null && !gates.contains(gate)) {
            gates.add(gate);
        }
    }
    
    public List<Connector> getConnectors() {
        return wires; // or whatever your connector list is called
    }
    
    // Remove a component from the circuit
    public void removeGate(GateComponent gate) {
        if (gate != null) {
            // Disconnect all wires connected to this gate
            List<Connector> toRemove = new ArrayList<>();
            for (Connector wire : wires) {
                if (wire.getFromGate() == gate || wire.getToGate() == gate) {
                    wire.disconnectWire();
                    toRemove.add(wire);
                }
            }
            wires.removeAll(toRemove);
            gates.remove(gate);
            
            // Also remove from input/output nodes if present
            circuitInputNodes.remove(gate);
            circuitOutputNodes.remove(gate);
        }
    }

    // Add a wire/connector to the circuit
    public void addConnectionWire(Connector wire) {
        if (wire != null && !wires.contains(wire)) {
            wires.add(wire);
            wire.recalculateWirePositions();
        }
    }

    // Remove a wire from the circuit
    public void removeConnectionWire(Connector wire) {
        if (wire != null) {
            wire.disconnectWire();
            wires.remove(wire);
        }
    }

    // Create a connection between two gates
    public Connector gatesConnectorFunc(GateComponent fromGate, int fromPort, GateComponent toGate, int toPort) {
        if (fromGate == null || toGate == null) return null;
        if (fromPort < 0 || fromPort >= fromGate.getOutputs()) return null;
        if (toPort < 0 || toPort >= toGate.getInputs()) return null;

        // Check if output port is already connected
        if (fromGate.getOutputWire(fromPort) != null) {
            return null; // Port already connected
        }

        // Check if input port is already connected
        if (toGate.getInputWire(toPort) != null) {
            return null; // Port already connected
        }

        // Create new connector
        Connector wire = new Connector(fromGate, fromPort, toGate, toPort);
        addConnectionWire(wire);
        return wire;
    }

    // ===== CIRCUIT LAYOUT AND VISUAL MANAGEMENT =====
    
    // Update all wire positions (should be called when gates are moved)
    public void updateConnectorPos() {
        for (Connector wire : wires) {
            wire.recalculateWirePositions();
        }
    }

    // Update component positions in the circuit
    public void updateCircuitComponentPos() {
        updateConnectorPos();
    }

    // ===== COMPONENT LOOKUP AND IDENTIFICATION =====
    
    // Find a gate by its ID
    public GateComponent getGateById(String id) {
        for (GateComponent gate : gates) {
            if (gate.getId().equals(id)) {
                return gate;
            }
        }
        return null;
    }

    // Find a wire by its ID
    public Connector getWireById(String id) {
        for (Connector wire : wires) {
            if (wire.getId().equals(id)) {
                return wire;
            }
        }
        return null;
    }

    // ===== CIRCUIT SIMULATION AND EVALUATION =====
    
    // Propagate signals through the circuit
    public void sigPropogation() {
        for (Connector wire : wires) {
            wire.transmitSig();
        }
        
        // NEW: Force LED state updates after signal propagation
        updateLEDStates();
    }

    /**
     * Evaluate the entire circuit using sequential propagation
     */
    public void evaluate() {
        if (gates.isEmpty()) return;
        
        // Reset all gate states
        reinitializeCircuitState();
        
        // Get all gates in evaluation order (inputs first, then dependencies)
        List<GateComponent> evaluationOrder = computeEvaluationSequence();
        
        // Evaluate each gate in order
        for (GateComponent gate : evaluationOrder) {
            // Propagate inputs to this gate first
            updateGateInputSignals(gate);
            
            // Then evaluate the gate
            gate.evaluate();
        }
        
        // One final propagation to ensure outputs are set
        sigPropogation();
    }

    /**
     * NEW: Special method to update LED states
     * This ensures LEDs reflect their current input values
     */
    public void updateLEDStates() {
        for (GateComponent gate : gates) {
            if (gate instanceof LED) {
                LED led = (LED) gate;
                // Force LED to compute its state based on current inputs
                led.computeOutput();
            }
        }
    }

    /**
     * NEW: Enhanced signal propagation with LED support
     * This method ensures LEDs get immediate visual feedback
     */
    public void propagateSignalsWithLEDUpdate() {
        boolean changed;
        int maxIterations = 100; // Prevent infinite loops
        int iterations = 0;
        
        do {
            changed = false;
            
            // Propagate signals through all wires
            for (Connector wire : wires) {
                Boolean oldValue = wire.getSignalValue();
                wire.transmitSig();
                Boolean newValue = wire.getSignalValue();
                
                if (oldValue != newValue) {
                    changed = true;
                }
            }
            
            // Update LED states after each propagation cycle
            updateLEDStates();
            
            iterations++;
        } while (changed && iterations < maxIterations);
    }

    /**
     * Get gates in proper evaluation order (topological sort)
     */
    private List<GateComponent> computeEvaluationSequence() {
        List<GateComponent> result = new ArrayList<>();
        List<GateComponent> remaining = new ArrayList<>(gates);
        
        // Start with input nodes (gates with no internal dependencies)
        List<GateComponent> inputNodes = findCircuitInputs();
        result.addAll(inputNodes);
        remaining.removeAll(inputNodes);
        
        // Then add the rest in the order they appear (simple approach)
        result.addAll(remaining);
        
        return result;
    }

    /**
     * Propagate all input signals to a specific gate
     */
    private void updateGateInputSignals(GateComponent gate) 
    {
        for (int i = 0; i < gate.getInputs(); i++) 
        {
            Connector inputConnector = gate.getInputWire(i);
            if (inputConnector != null) 
            {
                inputConnector.transmitSig();
            }
        }
        
        // NEW: Special handling for LED - ensure it computes its state
        if (gate instanceof LED) {
            ((LED) gate).computeOutput();
        }
    }

    /**
     * Improved reset that preserves external connections
     */
    private void reinitializeCircuitState() 
    {
        for (GateComponent gate : gates) 
        {
            // Only reset inputs that are connected internally
            for (int i = 0; i < gate.getInputs(); i++) 
            {
                Connector connector = gate.getInputWire(i);
                if (connector != null && gates.contains(connector.getFromGate())) 
                {
                    gate.assignInputDirectly(i, false);
                }
            }
            // Reset all outputs
            for (int i = 0; i < gate.getOutputs(); i++) 
            {
                gate.assignOutputDirectly(i, false);
            }
            
            // NEW: Reset LED state
            if (gate instanceof LED) {
                ((LED) gate).computeOutput(); // Recompute based on reset inputs
            }
        }
    }

    // ===== CIRCUIT INPUT/OUTPUT INTERFACE =====
    
    /**
     * Get input value for a circuit component (for hierarchical circuits)
     */
    public Boolean getInputVal(int index) 
    {

        List<GateComponent> inputNodes = findCircuitInputs();
        int currentIndex = 0;
        
        for (GateComponent inputNode : inputNodes) 
        {
            for (int port = 0; port < inputNode.getInputs(); port++) 
            {
                Connector connector = inputNode.getInputWire(port);
                if (connector == null || !gates.contains(connector.getFromGate())) 
                {
                    if (currentIndex == index) 
                    {
                        return inputNode.getInputVal(port);
                    }
                    currentIndex++;
                }
            }
        }
        
        return false;
    }

    /**
     * Set input value for a circuit component (for hierarchical circuits)
     */
    public void setInputVal(int index, Boolean value) 
    {
        List<GateComponent> inputNodes = findCircuitInputs();
        int currentIndex = 0;
        
        for (GateComponent inputNode : inputNodes) 
        {
            
            for (int port = 0; port < inputNode.getInputs(); port++) {
                Connector connector = inputNode.getInputWire(port);
                if (connector == null || !gates.contains(connector.getFromGate())) 
                {
                    if (currentIndex == index) 
                    {
                        inputNode.assignInputDirectly(port, value);
                        // Immediately evaluate to propagate changes
                        inputNode.evaluate();
                        
                        // NEW: Force LED updates if this input affects any LED
                        updateLEDStates();
                        return;
                    }
                    currentIndex++;
                }
            }
        }
    }

    /**
     * Get output value for a circuit component (for hierarchical circuits)
     */
    public Boolean getOutputVal(int index) 
    {
        List<GateComponent> outputNodesStorage = findCircuitOutputs();
        int currentIndex = 0;
        
        for (GateComponent outputNode : outputNodesStorage) 
        {
            for (int port = 0; port < outputNode.getOutputs(); port++) 
            {
                Connector connector = outputNode.getOutputWire(port);
                if (connector == null || !gates.contains(connector.getToGate())) 
                {
                    if (currentIndex == index) 
                    {
                        return outputNode.getOutputVal(port);
                    }
                    currentIndex++;
                }
            }
        }
        
        return false;
    }

    // ===== CIRCUIT ANALYSIS AND IDENTIFICATION =====
    
    /**
     * Count circuit inputs - IMPROVED VERSION
     */
    public int calculateInputCount() 
    {
        List<GateComponent> inputNodes = findCircuitInputs();
        System.out.println("DEBUG: Circuit '" + CircuitName + "' - Input nodes found: " + inputNodes.size());
        
        // Count total input ports across all input nodes
        int totalInputs = 0;
        for (GateComponent inputNode : inputNodes) 
        {
            totalInputs += getFreeInputPortCount(inputNode);
        }
        
        System.out.println("DEBUG: Circuit '" + CircuitName + "' - Total input ports: " + totalInputs);
        return Math.max(1, totalInputs); // Ensure at least 1 input
    }

    /**
     * Count circuit outputs - IMPROVED VERSION  
     */
    public int calculateOutputCount() 
    {
        List<GateComponent> outputNodes = findCircuitOutputs();
        System.out.println("DEBUG: Circuit '" + CircuitName + "' - Output nodes found: " + outputNodes.size());
        
        // Count total output ports across all output nodes
        int totalOutputs = 0;
        for (GateComponent outputNode : outputNodes) 
        {
            totalOutputs += getFreeOutputPortCount(outputNode);
        }
        
        System.out.println("DEBUG: Circuit '" + CircuitName + "' - Total output ports: " + totalOutputs);
        return Math.max(1, totalOutputs); // Ensure at least 1 output
    }

    /**
     * IMPROVED input node identification
     */
    public List<GateComponent> findCircuitInputs() 
    {
        List<GateComponent> inputs = new ArrayList<>();
        
        for (GateComponent gate : gates) 
        {
            if (hasAvailableInputPorts(gate)) 
            {
                inputs.add(gate);
            }
        }
        
        // If no clear input nodes found, use gates with no incoming internal connections
        if (inputs.isEmpty()) 
        {
            for (GateComponent gate : gates) 
            {
                if (!hasInternalInputConnections(gate)) 
                {
                    inputs.add(gate);
                }
            }
        }
        
        return inputs;
    }

    /**
     * IMPROVED output node identification
     */
    public List<GateComponent> findCircuitOutputs() {
        List<GateComponent> outputs = new ArrayList<>();
        
        for (GateComponent gate : gates) {
            if (hasAvailableOutputPorts(gate)) {
                outputs.add(gate);
            }
        }
        
        // If no clear output nodes found, use gates with no outgoing internal connections
        if (outputs.isEmpty()) {
            for (GateComponent gate : gates) {
                if (!hasInternalOutputConnections(gate)) {
                    outputs.add(gate);
                }
            }
        }
        
        return outputs;
    }

    // ===== PORT ANALYSIS HELPERS =====
    
    /**
     * Count available input ports on a gate (ports not connected internally)
     */
    private int getFreeInputPortCount(GateComponent gate)
    {
        int availablePorts = 0;
        for (int i = 0; i < gate.getInputs(); i++) 
        {
            Connector connector = gate.getInputWire(i);
            if (connector == null || !gates.contains(connector.getFromGate())) 
            {
                availablePorts++;
            }
        }
        return availablePorts;
    }

    /**
     * Count available output ports on a gate (ports not connected internally)
     */
    private int getFreeOutputPortCount(GateComponent gate) 
    {
        int availablePorts = 0;
        for (int i = 0; i < gate.getOutputs(); i++) 
        {
            Connector connector = gate.getOutputWire(i);
            if (connector == null || !gates.contains(connector.getToGate())) 
            {
                availablePorts++;
            }
        }
        return availablePorts;
    }

    private boolean hasAvailableInputPorts(GateComponent gate) 
    {
        return getFreeInputPortCount(gate) > 0;
    }

    private boolean hasAvailableOutputPorts(GateComponent gate) 
    {
        return getFreeOutputPortCount(gate) > 0;
    }

    private boolean hasInternalInputConnections(GateComponent gate) 
    {
        for (Connector wire : wires) 
        {
            if (wire.getToGate() == gate && gates.contains(wire.getFromGate())) 
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasInternalOutputConnections(GateComponent gate) 
    {
        for (Connector wire : wires) 
        {
            if (wire.getFromGate() == gate && gates.contains(wire.getToGate())) 
            {
                return true;
            }
        }
        return false;
    }

    // ===== CIRCUIT COPY AND SERIALIZATION =====
    
    /**
     * Create a proper deep copy of the circuit with all internal logic preserved
     */
    public Circuit createCopy(String newName) 
    {
        Circuit copy = new Circuit(newName);
        
        // Create mapping from original gates to copied gates
        Map<GateComponent, GateComponent> gateMap = new HashMap<>();
        
        // First pass: copy all basic gates
        for (GateComponent originalGate : gates) 
        {
            if (!(originalGate instanceof SubcircuitComponent)) 
            {
                GateComponent copiedGate = originalGate.copy();
                if (copiedGate != null) 
                {
                    copiedGate.setPosition(originalGate.getPosition().x, originalGate.getPosition().y);
                    copy.addGateFunc(copiedGate);
                    gateMap.put(originalGate, copiedGate);
                }
            }
        }
        
        // Second pass: copy circuit components (they might reference other circuits)
        for (GateComponent originalGate : gates) 
        {
            if (originalGate instanceof SubcircuitComponent) 
            {
                SubcircuitComponent originalComp = (SubcircuitComponent) originalGate;
                SubcircuitComponent copiedComp = (SubcircuitComponent) originalComp.copy();
                if (copiedComp != null) 
                {
                    copiedComp.setPosition(originalGate.getPosition().x, originalGate.getPosition().y);
                    copy.addGateFunc(copiedComp);
                    gateMap.put(originalGate, copiedComp);
                }
            }
        }
        
        // Copy all wires using the gate mapping
        for (Connector originalWire : wires)
         {
            GateComponent fromGate = originalWire.getFromGate();
            GateComponent toGate = originalWire.getToGate();
            
            GateComponent copiedFromGate = gateMap.get(fromGate);
            GateComponent copiedToGate = gateMap.get(toGate);
            
            if (copiedFromGate != null && copiedToGate != null) 
            {
                int fromPort = originalWire.getSourcePortIndex();
                int toPort = originalWire.getDestinationPortIndex();
                
                copy.gatesConnectorFunc(copiedFromGate, fromPort, copiedToGate, toPort);
            }
        }
        
        return copy;
    }

    // ===== CIRCUIT CLEANUP AND MAINTENANCE =====
    
    // Clear all components and wires
    public void clear() 
    {
        for (Connector wire : wires) 
        {
            wire.disconnectWire();
        }

        wires.clear();
        gates.clear();
        circuitInputNodes.clear();
        circuitOutputNodes.clear();
    }

    // ===== DEBUG AND UTILITY METHODS =====
    
    /**
     * Debug method to print circuit structure
     */
    public void displayCircuitInfo() 
    {
        System.out.println("=== Circuit: " + CircuitName + " ===");
        System.out.println("Gates: " + gates.size());
        System.out.println("Wires: " + wires.size());
        System.out.println("Input nodes: " + findCircuitInputs().size());
        System.out.println("Output nodes: " + findCircuitOutputs().size());
        
        // NEW: Show LED states
        for (GateComponent gate : gates) 
        {
            if (gate instanceof LED) {
                LED led = (LED) gate;
                System.out.println("  LED " + gate.getId() + " at " + gate.getPosition() + " - " + (led.isLit() ? "LIT" : "OFF"));
            } else {
                System.out.println("  " + gate.getId() + " at " + gate.getPosition());
            }
        }
    }

    @Override
    public String toString() 
    {
        return "Circuit{" + "name='" + CircuitName + '\'' + ", gates=" + gates.size() + ", wires=" + wires.size() + '}';
    }
}