package org.yourcompany.yourproject.Backend.businessLayer.analysis;

import java.util.HashMap;
import java.util.Map;

import org.yourcompany.yourproject.Backend.businessLayer.components.Circuit;
import org.yourcompany.yourproject.Backend.businessLayer.components.Connector;
import org.yourcompany.yourproject.Backend.businessLayer.components.GateComponent;

public class CircuitLogicSimulator 
{

    // ===== CIRCUIT INITIALIZATION =====
    
    private static void circuitInputInitializer(Circuit circuit, Map<String, Boolean> externalInputs) 
    {
        for (GateComponent gate : circuit.getGates()) 
        {
            for (int i = 0; i < gate.getInputs(); i++) 
            {
                String key = gate.getName() + "_in" + i;
                Boolean value = externalInputs.getOrDefault(key, false);
                gate.setInputVal(i, value);
            }
        }
    }
    
    // ===== SIGNAL PROPAGATION LOGIC =====
    
    private static void runSignalPropagation(Circuit circuit) 
    {
        boolean changed;
        do {
            changed = false;
            changed |= evaluateAllGates(circuit);
            changed |= transferSignalsThroughWires(circuit);
        } while (changed);
    }
    
    private static boolean evaluateAllGates(Circuit circuit) 
    {
        boolean changed = false;
        for (GateComponent gate : circuit.getGates()) 
        {
            Boolean oldOutput = gate.getOutputVal(0);
            gate.evaluate();
            if (!oldOutput.equals(gate.getOutputVal(0))) 
            {
                changed = true;
            }
        }
        return changed;
    }
    
    // ===== MAIN PUBLIC INTERFACE =====
    
    public static Map<String, Boolean> run(Circuit circuit, Map<String, Boolean> externalInputs) 
    {
        circuitInputInitializer(circuit, externalInputs);
        runSignalPropagation(circuit);
        return gatherGateOutputs(circuit);
    }
    
    private static boolean transferSignalsThroughWires(Circuit circuit) 
    {
        boolean changed = false;
        for (Connector conn : circuit.getWires()) {
            GateComponent from = conn.getFromGate();
            GateComponent to   = conn.getToGate();
            int toPin          = conn.getDestinationPortIndex();
            Boolean signal     = from.getOutputVal(conn.getSourcePortIndex());

            if (!signal.equals(to.getInputVal(toPin))) 
            {
                changed = true;
            }
            to.setInputVal(toPin, signal);
        }
        return changed;
    }
    
    // ===== OUTPUT COLLECTION =====
    
    private static Map<String, Boolean> gatherGateOutputs(Circuit circuit) 
    {
        Map<String, Boolean> outputsStorage = new HashMap<>();
        for (GateComponent gate : circuit.getGates()) 
        {
            outputsStorage.put(gate.getName(), gate.getOutputVal(0));
        }
        return outputsStorage;
    }
}