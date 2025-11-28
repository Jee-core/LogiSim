package org.yourcompany.yourproject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yourcompany.yourproject.Backend.businessLayer.components.Circuit;
import org.yourcompany.yourproject.Backend.businessLayer.components.SubcircuitComponent;
import org.yourcompany.yourproject.Backend.businessLayer.components.gates.andd;


public class SubcircuitComponentTest {
    
    private Circuit innerCircuit;
    private SubcircuitComponent subcircuit;
    
    @BeforeEach
    void setUp() {
        innerCircuit = new Circuit("InnerCircuit");
        andd innerGate = new andd();
        innerCircuit.addGateFunc(innerGate);
        
        subcircuit = new SubcircuitComponent(innerCircuit, "Subcircuit1");
    }
    
    @Test
    void testSubcircuitCreation() {
        assertNotNull(subcircuit);
        assertEquals("Subcircuit1", subcircuit.getCircuitName());
        assertEquals(innerCircuit, subcircuit.getReferencedCircuit());
        assertTrue(subcircuit.getInputs() >= 1);
        assertTrue(subcircuit.getOutputs() >= 1);
    }
    
    @Test
    void testSubcircuitEvaluation() {
        subcircuit.setInputVal(0, true);
        subcircuit.setInputVal(1, true);
        
        subcircuit.evaluate();
        
        // Output should be computed based on inner circuit logic
        assertNotNull(subcircuit.getOutputVal(0));
    }
    
    @Test
    void testSubcircuitCopy() {
        SubcircuitComponent copy = (SubcircuitComponent) subcircuit.copy();
        
        assertNotNull(copy);
        assertEquals(subcircuit.getCircuitName(), copy.getCircuitName());
        assertNotSame(subcircuit.getReferencedCircuit(), copy.getReferencedCircuit());
    }
    
    @Test
    void testSubcircuitWithEmptyCircuit() {
        Circuit emptyCircuit = new Circuit("Empty");
        SubcircuitComponent emptySubcircuit = new SubcircuitComponent(emptyCircuit, "EmptySub");
        
        assertNotNull(emptySubcircuit);
        emptySubcircuit.evaluate(); // Should not throw exception
    }
}