package org.yourcompany.yourproject;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yourcompany.yourproject.Backend.businessLayer.components.gates.LED;

public class LEDTest {
    
    private LED led;
    
    @BeforeEach
    void setUp() {
        led = new LED();
    }
    
    @Test
    void testLEDCreation() {
        assertEquals(1, led.getInputs());
        assertEquals(0, led.getOutputs());
        assertFalse(led.isLit());
        assertEquals(Color.RED, led.getLedColor());
    }
    
    @Test
    void testLEDLitState() {
        led.setInputVal(0, true);
        led.computeOutput();
        
        assertTrue(led.isLit());
        assertEquals(Color.GREEN, led.getLedColor());
    }
    
    @Test
    void testLEDOffState() {
        led.setInputVal(0, false);
        led.computeOutput();
        
        assertFalse(led.isLit());
        assertEquals(Color.RED, led.getLedColor());
    }
    
    @Test
    void testLEDNoOutputs() {
        assertNull(led.getOutputVal(0));
    }
    
    @Test
    void testLEDCopy() {
        led.setInputVal(0, true);
        led.computeOutput();
        
        LED copy = (LED) led.copy();
        
        assertNotNull(copy);
        assertEquals(led.getPosition(), copy.getPosition());
        assertEquals(led.isLit(), copy.isLit());
    }
}