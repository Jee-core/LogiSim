package org.yourcompany.yourproject;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yourcompany.yourproject.Backend.businessLayer.components.GateComponent;
import org.yourcompany.yourproject.Backend.businessLayer.components.gates.andd;
import org.yourcompany.yourproject.Backend.businessLayer.components.gates.nott;
import org.yourcompany.yourproject.Backend.businessLayer.components.gates.orr;

public class GateComponentTest {
    
    private andd andGate;
    private orr orGate;
    private nott notGate;
    
    @BeforeEach
    void setUp() {
        andGate = new andd();
        orGate = new orr();
        notGate = new nott();
    }
    
    @Test
    void testGateCreation() {
        assertEquals(2, andGate.getInputs());
        assertEquals(1, andGate.getOutputs());
        assertNotNull(andGate.getName());
        assertNotNull(andGate.getId());
    }
    
    @Test
    void testAndGateLogic() {
        andGate.setInputVal(0, true);
        andGate.setInputVal(1, true);
        andGate.evaluate();
        
        assertTrue(andGate.getOutputVal(0));
        
        andGate.setInputVal(0, false);
        andGate.evaluate();
        assertFalse(andGate.getOutputVal(0));
    }
    
    @Test
    void testOrGateLogic() {
        orGate.setInputVal(0, false);
        orGate.setInputVal(1, false);
        orGate.evaluate();
        
        assertFalse(orGate.getOutputVal(0));
        
        orGate.setInputVal(0, true);
        orGate.evaluate();
        assertTrue(orGate.getOutputVal(0));
    }
    
    @Test
    void testNotGateLogic() {
        notGate.setInputVal(0, true);
        notGate.evaluate();
        
        assertFalse(notGate.getOutputVal(0));
        
        notGate.setInputVal(0, false);
        notGate.evaluate();
        assertTrue(notGate.getOutputVal(0));
    }
    
    @Test
    void testPositionManagement() {
        Point position = new Point(100, 200);
        andGate.setPosition(position);
        
        assertEquals(position, andGate.getPosition());
        
        andGate.setPosition(150, 250);
        assertEquals(new Point(150, 250), andGate.getPosition());
    }
    
    @Test
    void testInputOutputValues() {
        andGate.setInputVal(0, true);
        andGate.setInputVal(1, false);
        
        assertTrue(andGate.getInputVal(0));
        assertFalse(andGate.getInputVal(1));
        
        andGate.evaluate();
        assertFalse(andGate.getOutputVal(0));
    }
    
    @Test
    void testGateCopy() {
        andGate.setPosition(100, 200);
        andGate.setInputVal(0, true);
        
        GateComponent copy = andGate.copy();
        
        assertNotNull(copy);
        assertEquals(andGate.getPosition(), copy.getPosition());
        assertNotEquals(andGate.getId(), copy.getId());
    }
}