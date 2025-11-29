
package org.yourcompany.yourproject.backend.businessLayer.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yourcompany.yourproject.backend.businessLayer.components.Circuit;
import org.yourcompany.yourproject.backend.businessLayer.components.Connector;
import org.yourcompany.yourproject.backend.businessLayer.components.GateComponent;
import org.yourcompany.yourproject.backend.businessLayer.components.SubcircuitComponent;

public class BooleanEquationGenerator {

    private final Map<GateComponent, String> memo = new HashMap<>();
    private Circuit topLevelCircuit;

    /**
     * Public entry point
     */
    public String getExpression(GateComponent gate, Circuit circuit) {
        memo.clear();
        topLevelCircuit = circuit; // Store the top-level circuit
        return buildExpression(gate, 0, circuit);
    }

    /**
     * Main recursive expression builder
     */
    private String buildExpression(GateComponent gate, int port, Circuit currentCircuit) {

        String key = gate.getName() + "_" + System.identityHashCode(gate);
        if (memo.containsKey(gate)) {
            return memo.get(gate);
        }

        String gateType = gate.getClass().getSimpleName();
        String expr;

        // External input (no inputs) - this is a primary input
        if (gate.getInputs() == 0 && !(gate instanceof SubcircuitComponent)) {
            expr = gate.getName();
            memo.put(gate, expr);
            return expr;
        }

        // Handle CircuitComponent specially
        if (gate instanceof SubcircuitComponent) {
            SubcircuitComponent cc = (SubcircuitComponent) gate;
            Circuit inner = cc.getReferencedCircuit();

            // Get inner circuit output
            List<GateComponent> outputs = inner.findCircuitOutputs();
            if (outputs.isEmpty()) {
                expr = "[" + cc.getCircuitName() + "_EMPTY]";
                memo.put(gate, expr);
                return expr;
            }

            GateComponent innerOut = outputs.get(0);

            // Build expression for inner circuit output
            String innerExpr = buildExpression(innerOut, 0, inner);
            expr = "[" + cc.getCircuitName() + ": " + innerExpr + "]";
            memo.put(gate, expr);
            return expr;
        }

        // For regular gates, resolve inputs recursively
        List<String> inputExprs = new ArrayList<>();
        for (int i = 0; i < gate.getInputs(); i++) {
            String srcExpr = findInputSourceExpression(gate, i, currentCircuit);
            inputExprs.add(srcExpr);
        }

        // Gate type expression building
        switch (gateType) {
            case "AndGate":
                expr = "(" + inputExprs.get(0) + " · " + inputExprs.get(1) + ")";
                break;
            case "OrGate":
                expr = "(" + inputExprs.get(0) + " + " + inputExprs.get(1) + ")";
                break;
            case "NotGate":
                expr = "¬" + inputExprs.get(0);
                break;
            default:
                expr = gate.getName() + "(" + String.join(", ", inputExprs) + ")";
        }

        memo.put(gate, expr);
        return expr;
    }

    /**
     * Finds the source boolean expression of an input pin of a gate.
     */
    private String findInputSourceExpression(GateComponent gate, int port, Circuit currentCircuit) {

        // 1️⃣ Search for wires inside CURRENT circuit
        for (Connector wire : currentCircuit.getWires()) {
            if (wire.getToGate() == gate && wire.getDestinationPortIndex() == port) {
                return buildExpression(wire.getFromGate(), wire.getSourcePortIndex(), currentCircuit);
            }
        }

        // 2️⃣ If this is a CircuitComponent input, we need to look in the parent
        // circuit
        if (currentCircuit != topLevelCircuit) {
            // This means we're inside a nested circuit
            // The inputs to this nested circuit come from the parent circuit
            // We need to find which CircuitComponent in the parent circuit corresponds to
            // this nested circuit
            // and then trace its inputs

            Circuit parentCircuit = findParentCircuit(currentCircuit, topLevelCircuit);
            if (parentCircuit != null) {
                // Find the CircuitComponent in parent that references currentCircuit
                SubcircuitComponent parentComponent = findCircuitComponentForCircuit(parentCircuit, currentCircuit);
                if (parentComponent != null) {
                    // The input to this nested circuit corresponds to an input of the parent
                    // CircuitComponent
                    // We need to trace where that input comes from in the parent circuit
                    return findInputSourceExpression(parentComponent, port, parentCircuit);
                }
            }
        }

        // 3️⃣ No wire found → treat as external input
        return gate.getName() + "_IN" + port;
    }

    /**
     * Find the parent circuit that contains a reference to the given nested circuit
     */
    private Circuit findParentCircuit(Circuit nestedCircuit, Circuit searchCircuit) {
        for (GateComponent component : searchCircuit.getGates()) {
            if (component instanceof SubcircuitComponent) {
                SubcircuitComponent cc = (SubcircuitComponent) component;
                if (cc.getReferencedCircuit() == nestedCircuit) {
                    return searchCircuit;
                }
                // Recursively search in nested circuits
                Circuit result = findParentCircuit(nestedCircuit, cc.getReferencedCircuit());
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Find the CircuitComponent in a circuit that references the given nested
     * circuit
     */
    private SubcircuitComponent findCircuitComponentForCircuit(Circuit parentCircuit, Circuit nestedCircuit) {
        for (GateComponent component : parentCircuit.getGates()) {
            if (component instanceof SubcircuitComponent) {
                SubcircuitComponent cc = (SubcircuitComponent) component;
                if (cc.getReferencedCircuit() == nestedCircuit) {
                    return cc;
                }
            }
        }
        return null;
    }
}