package org.yourcompany.yourproject.backend.businessLayer.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yourcompany.yourproject.backend.businessLayer.components.Circuit;
import org.yourcompany.yourproject.backend.businessLayer.components.Connector;
import org.yourcompany.yourproject.backend.businessLayer.components.GateComponent;
import org.yourcompany.yourproject.backend.businessLayer.components.SubcircuitComponent;

/**
 * Enhanced truth table generator that handles hierarchical circuits
 */
public class TruthTableGen {

    // ===== MAIN PUBLIC INTERFACE =====

    /**
     * Generates truth table by directly simulating the circuit for all input
     * combinations
     */
    public List<TruthTableRow> truthTableGenFun(Circuit circuit) {
        System.out.println("=== ENHANCED TRUTH TABLE GENERATION (Hierarchical Support) ===");

        // 1. Find all input ports (unconnected gate inputs)
        List<CircuitInputTerminal> inputPorts = locateInputPorts(circuit);
        System.out.println("Input ports: " + inputPorts);

        if (inputPorts.isEmpty()) {
            System.out.println("No input ports found!");
            return new ArrayList<>();
        }

        // 2. Get all gate names for outputs (show everything)
        List<String> allGateNames = getAllGates(circuit);
        System.out.println("All gates: " + allGateNames);

        // 3. Generate all input combinations
        List<TruthTableRow> table = new ArrayList<>();
        int inputCount = inputPorts.size();
        int combinations = (int) Math.pow(2, inputCount);

        System.out.println("Generating " + combinations + " combinations");

        for (int i = 0; i < combinations; i++) {
            System.out.println("\n--- Combination " + i + " ---");

            // Reset circuit (including hierarchical circuits)
            clearAllGateStates(circuit);

            // Apply inputs
            Map<String, Boolean> inputs = assignInputCombo(inputPorts, i);
            System.out.println("Inputs: " + inputs);

            // DEBUG: Show state after input application
            debugCircuitState(circuit, "AFTER INPUT APPLICATION");

            // Simulate circuit (with hierarchical support)
            simulateCircuit(circuit);

            // DEBUG: Show final state after simulation
            debugCircuitState(circuit, "FINAL STATE");

            // Collect outputs from all gates
            Map<String, Boolean> outputs = storeAllOutput(circuit, allGateNames);
            System.out.println("Outputs: " + outputs);

            table.add(new TruthTableRow(inputs, outputs));
        }

        // Print with circuit order
        showTablle(table, circuit);

        return table;
    }

    // ===== CIRCUIT SIMULATION LOGIC =====

    /**
     * Enhanced circuit simulation that handles hierarchical circuits
     */
    private void simulateCircuit(Circuit circuit) {
        boolean changed;
        int maxIterations = 100; // Increased for complex circuits
        int iterations = 0;

        do {
            changed = false;

            // 1. First propagate signals through wires to update gate inputs
            changed |= propagateSignals(circuit);

            // 2. Then evaluate all gates to compute new outputs
            for (GateComponent gate : circuit.getGates()) {
                boolean[] oldOutputs = new boolean[gate.getOutputs()];
                for (int i = 0; i < gate.getOutputs(); i++) {
                    oldOutputs[i] = gate.getOutputVal(i);
                }

                evaluateGate(gate);

                // Check if outputs changed
                for (int i = 0; i < gate.getOutputs(); i++) {
                    if (oldOutputs[i] != gate.getOutputVal(i)) {
                        changed = true;
                    }
                }
            }

            iterations++;
            if (iterations > maxIterations) {
                System.out.println("Warning: Simulation exceeded maximum iterations");
                break;
            }

        } while (changed);

        System.out.println("Circuit simulation completed in " + iterations + " iterations");
    }

    /**
     * Enhanced gate evaluation that handles CircuitComponent
     */
    private void evaluateGate(GateComponent gate) {
        String gateType = gate.getClass().getSimpleName();

        if (gate instanceof SubcircuitComponent) {
            // Handle CircuitComponent - let it evaluate its internal circuit
            SubcircuitComponent circuitComp = (SubcircuitComponent) gate;
            System.out.println("  Evaluating CircuitComponent: " + circuitComp.getCircuitName());

            // The CircuitComponent will handle its own internal evaluation
            gate.evaluate();

        } else {
            // Handle basic gates - USE THE GATE'S OWN EVALUATE METHOD
            gate.evaluate();

            System.out.println("  " + gate.getName() + " (" + gateType + ") -> " + gate.getOutputVal(0) +
                    " [inputs: " + formatInputValues(gate) + "]");
        }
    }

    /**
     * Propagate signals through all wires in the circuit
     */
    private boolean propagateSignals(Circuit circuit) {
        boolean changed = false;
        for (Connector wire : circuit.getWires()) {
            GateComponent fromGate = wire.getFromGate();
            GateComponent toGate = wire.getToGate();
            int fromPort = wire.getSourcePortIndex();
            int toPort = wire.getDestinationPortIndex();

            if (fromGate != null && toGate != null) {
                boolean signalValue = fromGate.getOutputVal(fromPort);
                boolean oldValue = toGate.getInputVal(toPort);

                // Only update if value changed
                if (oldValue != signalValue) {
                    toGate.setInputVal(toPort, signalValue);
                    changed = true;
                    System.out.println("  Propagating: " + fromGate.getName() + "[" + fromPort + "](" + signalValue +
                            ") -> " + toGate.getName() + "[" + toPort + "]");
                }
            }
        }
        return changed;
    }

    // ===== CIRCUIT ANALYSIS UTILITIES =====

    private List<CircuitInputTerminal> locateInputPorts(Circuit circuit) {
        List<CircuitInputTerminal> inputPorts = new ArrayList<>();

        for (GateComponent gate : circuit.getGates()) {
            for (int port = 0; port < gate.getInputs(); port++) {
                if (!isInputConnected(circuit, gate, port)) {
                    inputPorts.add(new CircuitInputTerminal(gate, port));
                }
            }
        }

        return inputPorts;
    }

    private boolean isInputConnected(Circuit circuit, GateComponent gate, int port) {
        for (Connector wire : circuit.getWires()) {
            if (wire.getToGate() == gate && wire.getDestinationPortIndex() == port) {
                return true;
            }
        }
        return false;
    }

    private List<String> getAllGates(Circuit circuit) {
        List<String> names = new ArrayList<>();
        for (GateComponent gate : circuit.getGates()) {
            names.add(gate.getName());
        }
        return names;
    }

    // ===== CIRCUIT STATE MANAGEMENT =====

    /**
     * Enhanced reset that handles hierarchical circuits
     */
    private void clearAllGateStates(Circuit circuit) {
        for (GateComponent gate : circuit.getGates()) {
            // Reset inputs
            for (int i = 0; i < gate.getInputs(); i++) {
                gate.setInputVal(i, false);
            }
            // Reset outputs
            for (int i = 0; i < gate.getOutputs(); i++) {
                gate.setOutputVal(i, false);
            }

            // If it's a CircuitComponent, reset its internal circuit too
            if (gate instanceof SubcircuitComponent) {
                SubcircuitComponent circuitComp = (SubcircuitComponent) gate;
                Circuit internalCircuit = circuitComp.getReferencedCircuit();
                if (internalCircuit != null) {
                    resetInternalCircuit(internalCircuit);
                }
            }
        }
    }

    /**
     * Recursively reset internal circuits
     */
    private void resetInternalCircuit(Circuit circuit) {
        for (GateComponent gate : circuit.getGates()) {
            // Reset inputs
            for (int i = 0; i < gate.getInputs(); i++) {
                gate.setInputVal(i, false);
            }
            // Reset outputs
            for (int i = 0; i < gate.getOutputs(); i++) {
                gate.setOutputVal(i, false);
            }

            // Recursively reset any nested CircuitComponents
            if (gate instanceof SubcircuitComponent) {
                SubcircuitComponent circuitComp = (SubcircuitComponent) gate;
                Circuit internalCircuit = circuitComp.getReferencedCircuit();
                if (internalCircuit != null) {
                    resetInternalCircuit(internalCircuit);
                }
            }
        }
    }

    private Map<String, Boolean> assignInputCombo(List<CircuitInputTerminal> inputPorts, int combination) {
        Map<String, Boolean> inputs = new HashMap<>();

        for (int i = 0; i < inputPorts.size(); i++) {
            CircuitInputTerminal port = inputPorts.get(i);
            boolean value = ((combination >> i) & 1) == 1;

            // Set the input value on the gate - USE setInputValue NOT setInputValueDirect
            port.gate.setInputVal(port.portIndex, value);
            inputs.put(port.name, value);
        }

        return inputs;
    }

    private Map<String, Boolean> storeAllOutput(Circuit circuit, List<String> gateNames) {
        Map<String, Boolean> outputs = new HashMap<>();

        for (String gateName : gateNames) {
            GateComponent gate = findGateByName(circuit, gateName);
            if (gate != null && gate.getOutputs() > 0) {
                outputs.put(gateName, gate.getOutputVal(0));
            }
        }

        return outputs;
    }

    // ===== OUTPUT AND DISPLAY METHODS =====

    public static class TruthTableRow {
        private final Map<String, Boolean> inputs;
        private final Map<String, Boolean> outputs;

        public TruthTableRow(Map<String, Boolean> inputs, Map<String, Boolean> outputs) {
            this.inputs = inputs;
            this.outputs = outputs;
        }

        public Map<String, Boolean> getInputs() {
            return new HashMap<>(inputs);
        }

        public Map<String, Boolean> getOutputs() {
            return new HashMap<>(outputs);
        }

        public boolean getInput(String name) {
            return inputs.getOrDefault(name, false);
        }

        public boolean getOutput(String name) {
            return outputs.getOrDefault(name, false);
        }
    }

    /**
     * Print formatted truth table in circuit order
     */
    public static void showTablle(List<TruthTableRow> table, Circuit circuit) {
        if (table.isEmpty())
            return;

        // Get all input names (alphabetical order is fine for inputs)
        List<String> inputNames = new ArrayList<>(table.get(0).getInputs().keySet());
        Collections.sort(inputNames);

        // Get output names in CIRCUIT ORDER (the order gates appear in the circuit)
        List<String> outputNames = getGateNamesInCircuitOrder(circuit);

        // Print header
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TRUTH TABLE (Hierarchical Circuits Supported)");
        System.out.println("=".repeat(80));

        // Input headers
        System.out.print("| ");
        for (String input : inputNames) {
            System.out.printf("%-8s | ", input);
        }

        // Output headers - in circuit order
        System.out.print("| ");
        for (String output : outputNames) {
            System.out.printf("%-8s | ", output);
        }
        System.out.println();

        // Separator
        System.out.print("|");
        for (int i = 0; i < inputNames.size(); i++) {
            System.out.print("----------|");
        }
        System.out.print("|");
        for (int i = 0; i < outputNames.size(); i++) {
            System.out.print("----------|");
        }
        System.out.println();

        // Rows
        for (TruthTableRow row : table) {
            // Input values
            System.out.print("| ");
            for (String input : inputNames) {
                String value = row.getInput(input) ? "1" : "0";
                System.out.printf("%-8s | ", value);
            }

            // Output values - in circuit order
            System.out.print("| ");
            for (String output : outputNames) {
                String value = row.getOutput(output) ? "1" : "0";
                System.out.printf("%-8s | ", value);
            }
            System.out.println();
        }

        System.out.println("=".repeat(80));
    }

    /**
     * Get output names in the order they appear in the circuit (not alphabetical)
     */
    private static List<String> getGateNamesInCircuitOrder(Circuit circuit) {
        List<String> outputNames = new ArrayList<>();
        for (GateComponent gate : circuit.getGates()) {
            outputNames.add(gate.getName());
        }
        return outputNames;
    }

    // ===== HELPER METHODS =====

    private String formatInputValues(GateComponent gate) {
        List<String> inputValues = new ArrayList<>();
        for (int i = 0; i < gate.getInputs(); i++) {
            inputValues.add(gate.getInputVal(i) ? "1" : "0");
        }
        return String.join(",", inputValues);
    }

    private GateComponent findGateByName(Circuit circuit, String name) {
        for (GateComponent gate : circuit.getGates()) {
            if (gate.getName().equals(name)) {
                return gate;
            }
        }
        return null;
    }

    // ===== DEBUG METHODS =====

    private void debugCircuitState(Circuit circuit, String combinationInfo) {
        System.out.println("=== DEBUG: " + combinationInfo + " ===");
        for (GateComponent gate : circuit.getGates()) {
            System.out.print(gate.getName() + " (" + gate.getClass().getSimpleName() + ") - ");
            System.out.print("Inputs: [");
            for (int i = 0; i < gate.getInputs(); i++) {
                System.out.print(gate.getInputVal(i) ? "1" : "0");
                if (i < gate.getInputs() - 1)
                    System.out.print(", ");
            }
            System.out.print("] Outputs: [");
            for (int i = 0; i < gate.getOutputs(); i++) {
                System.out.print(gate.getOutputVal(i) ? "1" : "0");
                if (i < gate.getOutputs() - 1)
                    System.out.print(", ");
            }
            System.out.println("]");
        }
        System.out.println("=====================");
    }

    // ===== INNER CLASSES =====

    private static class CircuitInputTerminal {
        final GateComponent gate;
        final int portIndex;
        final String name;

        CircuitInputTerminal(GateComponent gate, int Index) {
            this.gate = gate;
            this.portIndex = Index;
            this.name = gate.getName() + "_IN" + Index;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}