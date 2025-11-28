package org.yourcompany.yourproject;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yourcompany.yourproject.Backend.businessLayer.components.Circuit;
import org.yourcompany.yourproject.Backend.businessLayer.components.Connector;
import org.yourcompany.yourproject.Backend.businessLayer.components.GateComponent;
import org.yourcompany.yourproject.Backend.businessLayer.components.gates.andd;
import org.yourcompany.yourproject.Backend.businessLayer.components.gates.orr;

public class CircuitTest {
    
    private Circuit circuit;
    
    @BeforeEach
    void setUp() {
        circuit = new Circuit("TestCircuit");
    }
    
    @Test
    void testCircuitCreation() {
        assertEquals("TestCircuit", circuit.getName());
        assertTrue(circuit.getGates().isEmpty());
        assertTrue(circuit.getWires().isEmpty());
    }
    
    @Test
    void testAddGate() {
        andd gate = new andd();
        
        circuit.addGateFunc(gate);
        
        assertEquals(1, circuit.getGates().size());
        assertTrue(circuit.getGates().contains(gate));
    }
    
    @Test
    void testRemoveGate() {
        andd gate = new andd();
        circuit.addGateFunc(gate);
        
        circuit.removeGate(gate);
        
        assertTrue(circuit.getGates().isEmpty());
    }
    
    @Test
    void testCreateValidConnection() {
        andd gate1 = new andd();
        orr gate2 = new orr();
        
        circuit.addGateFunc(gate1);
        circuit.addGateFunc(gate2);
        
        Connector wire = circuit.gatesConnectorFunc(gate1, 0, gate2, 0);
        
        assertNotNull(wire);
        assertEquals(1, circuit.getWires().size());
        assertEquals(gate1, wire.getFromGate());
        assertEquals(gate2, wire.getToGate());
    }
    
    @Test
    void testInvalidConnection() {
        andd gate1 = new andd();
        orr gate2 = new orr();
        
        circuit.addGateFunc(gate1);
        circuit.addGateFunc(gate2);
        
        // Invalid port index
        Connector wire = circuit.gatesConnectorFunc(gate1, 5, gate2, 0);
        assertNull(wire);
    }
    
    @Test
    void testFindInputsOutputs() {
        andd gate1 = new andd();
        orr gate2 = new orr();
        
        circuit.addGateFunc(gate1);
        circuit.addGateFunc(gate2);
        
        List<GateComponent> inputs = circuit.findCircuitInputs();
        List<GateComponent> outputs = circuit.findCircuitOutputs();
        
        assertFalse(inputs.isEmpty());
        assertFalse(outputs.isEmpty());
    }
    
    @Test
    void testInputOutputCount() {
        andd gate1 = new andd();
        orr gate2 = new orr();
        
        circuit.addGateFunc(gate1);
        circuit.addGateFunc(gate2);
        
        int inputCount = circuit.calculateInputCount();
        int outputCount = circuit.calculateOutputCount();
        
        assertTrue(inputCount >= 1);
        assertTrue(outputCount >= 1);
    }
    
    @Test
    void testCircuitCopy() {
        andd gate = new andd();
        circuit.addGateFunc(gate);
        
        Circuit copy = circuit.createCopy("CopyCircuit");
        
        assertNotNull(copy);
        assertEquals("CopyCircuit", copy.getName());
        assertEquals(1, copy.getGates().size());
    }
    
    @Test
    void testClearCircuit() {
        andd gate = new andd();
        circuit.addGateFunc(gate);
        
        circuit.clear();
        
        assertTrue(circuit.getGates().isEmpty());
        assertTrue(circuit.getWires().isEmpty());
    }
}