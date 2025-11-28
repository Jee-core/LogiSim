package org.yourcompany.yourproject;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.yourcompany.yourproject.Backend.businessLayer.components.gates.andd;

public class AndGateTest {
    
    @Test
    void testAndGateLogic() {
        andd gate = new andd();
        
        // Test all AND logic combinations
        assertTrue(testAnd(gate, true, true));   // T&T = T
        assertFalse(testAnd(gate, true, false)); // T&F = F
        assertFalse(testAnd(gate, false, true)); // F&T = F
        assertFalse(testAnd(gate, false, false));// F&F = F
    }
    
    @Test
    void testAndGateMethods() {
        andd gate = new andd();
        
        // Basic properties
        assertEquals(2, gate.getInputs());
        assertEquals(1, gate.getOutputs());
        assertTrue(gate.getName().startsWith("AND"));
        
        // Position
        gate.setPosition(100, 200);
        assertEquals(new Point(100, 200), gate.getPosition());
        
        // Copy
        andd copy = (andd) gate.copy();
        assertEquals(gate.getPosition(), copy.getPosition());
        assertNotEquals(gate.getId(), copy.getId());
    }
    
    private boolean testAnd(andd gate, boolean in1, boolean in2) {
        gate.setInputVal(0, in1);
        gate.setInputVal(1, in2);
        gate.evaluate();
        return gate.getOutputVal(0);
    }
}