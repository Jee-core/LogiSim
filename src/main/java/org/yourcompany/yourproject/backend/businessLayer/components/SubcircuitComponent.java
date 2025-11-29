package org.yourcompany.yourproject.backend.businessLayer.components;

import java.util.List;

public class SubcircuitComponent extends GateComponent {
    private Circuit referencedCircuit;
    private String circuitName;

    // ===== CONSTRUCTOR AND INITIALIZATION =====

    public SubcircuitComponent(Circuit circuit, String name) {
        super("CIRCUIT:" + name, circuitInputCount(circuit), circuitOutputCount(circuit));
        this.referencedCircuit = circuit;
        this.circuitName = name;

        // DEBUG: Print detailed information
        System.out.println("=== CircuitComponent Created ===");
        System.out.println("Name: " + name);
        System.out.println("Inputs: " + getInputs());
        System.out.println("Outputs: " + getOutputs());
        if (circuit != null) {
            System.out.println("Referenced Circuit: " + circuit.getName());
            circuit.displayCircuitInfo();
        }
        System.out.println("===============================");
    }

    // ===== ABSTRACT METHOD IMPLEMENTATIONS =====

    @Override
    public void evaluate() {
        calculateOutput();
    }

    @Override
    public void update() {
        calculateOutput();
    }

    @Override
    protected void calculateOutput() {
        if (referencedCircuit != null) {
            try {
                // DEBUG
                System.out.println("Evaluating CircuitComponent: " + circuitName);

                // Set the inputs from this component to the referenced circuit
                assignSubCircuitInputs();

                // Evaluate the entire referenced circuit
                referencedCircuit.evaluate();

                // Get the outputs from the referenced circuit
                retrieveSubcircuitOutputs();

            } catch (Exception e) {
                System.err.println("Error evaluating circuit component '" + circuitName + "': " + e.getMessage());
                e.printStackTrace();
                // Fallback: set all outputs to false
                for (int i = 0; i < getOutputs(); i++) {
                    setOutputVal(i, false);
                }
            }
        }
    }

    @Override
    public GateComponent copy() {
        // Create a deep copy of the referenced circuit
        Circuit circuitCopy = referencedCircuit.createCopy(circuitName + "_Copy");
        return new SubcircuitComponent(circuitCopy, circuitName);
    }

    // ===== CIRCUIT INPUT/OUTPUT MAPPING =====

    /**
     * IMPROVED: Set the inputs from this component to the referenced circuit
     */
    private void assignSubCircuitInputs() {
        List<GateComponent> inputNodes = referencedCircuit.findCircuitInputs();
        System.out.println("Setting inputs - Found " + inputNodes.size() + " input nodes");

        int globalInputIndex = 0;

        for (GateComponent inputNode : inputNodes) {
            for (int port = 0; port < inputNode.getInputs(); port++) {
                // Check if this port is available (not connected internally)
                Connector connector = inputNode.getInputWire(port);
                if (connector == null || !referencedCircuit.getGates().contains(connector.getFromGate())) {
                    if (globalInputIndex < getInputs()) {
                        Boolean inputValue = getInputVal(globalInputIndex);
                        System.out.println("Setting input " + globalInputIndex + " to node " +
                                inputNode.getId() + " port " + port + " = " + inputValue);
                        inputNode.assignInputDirectly(port, inputValue);
                        globalInputIndex++;
                    }
                }
            }
        }
    }

    /**
     * IMPROVED: Get the outputs from the referenced circuit and set them as our
     * outputs
     */
    private void retrieveSubcircuitOutputs() {
        List<GateComponent> outputNodes = referencedCircuit.findCircuitOutputs();
        System.out.println("Getting outputs - Found " + outputNodes.size() + " output nodes");

        int globalOutputIndex = 0;

        for (GateComponent outputNode : outputNodes) {
            for (int port = 0; port < outputNode.getOutputs(); port++) {
                // Check if this port is available (not connected internally)
                Connector connector = outputNode.getOutputWire(port);
                if (connector == null || !referencedCircuit.getGates().contains(connector.getToGate())) {
                    if (globalOutputIndex < getOutputs()) {
                        Boolean outputValue = outputNode.getOutputVal(port);
                        System.out.println("Getting output " + globalOutputIndex + " from node " +
                                outputNode.getId() + " port " + port + " = " + outputValue);
                        setOutputVal(globalOutputIndex, outputValue);
                        globalOutputIndex++;
                    }
                }
            }
        }
    }

    // ===== CIRCUIT PORT COUNTING =====

    // Helper methods - IMPROVED
    private static int circuitInputCount(Circuit circuit) {
        if (circuit == null) {
            System.err.println("WARNING: Circuit is null in countCircuitInputs");
            return 1;
        }

        int inputs = circuit.calculateInputCount();
        System.out.println("Circuit '" + circuit.getName() + "' has " + inputs + " inputs");
        return Math.max(1, inputs);
    }

    private static int circuitOutputCount(Circuit circuit) {
        if (circuit == null) {
            System.err.println("WARNING: Circuit is null in countCircuitOutputs");
            return 1;
        }

        int outputs = circuit.calculateOutputCount();
        System.out.println("Circuit '" + circuit.getName() + "' has " + outputs + " outputs");
        return Math.max(1, outputs);
    }

    // ===== PROPERTY ACCESSORS =====

    public String getCircuitName() {
        return circuitName;
    }

    public Circuit getReferencedCircuit() {
        return referencedCircuit;
    }

    // Override to ensure minimum ports
    @Override
    public int getInputs() {
        int inputs = super.getInputs();
        return inputs <= 0 ? 1 : inputs;
    }

    @Override
    public int getOutputs() {
        int outputs = super.getOutputs();
        return outputs <= 0 ? 1 : outputs;
    }

    // ===== DEBUG AND UTILITY METHODS =====

    /**
     * Get detailed information about this circuit component
     */
    public void displayInfo() {

        System.out.println("CircuitComponent: " + circuitName);
        System.out.println("  Inputs: " + getInputs());
        System.out.println("  Outputs: " + getOutputs());

        if (referencedCircuit != null) {
            System.out.println("  Referenced Circuit: " + referencedCircuit.getName());
            System.out.println("  Gates in circuit: " + referencedCircuit.getGates().size());
            System.out.println("  Wires in circuit: " + referencedCircuit.getWires().size());
        }
    }
}