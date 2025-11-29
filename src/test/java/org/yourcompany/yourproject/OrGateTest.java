package org.yourcompany.yourproject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.yourcompany.yourproject.backend.businessLayer.components.gates.orr;

public class OrGateTest {

    @Test
    void testOrGateLogic() {
        orr gate = new orr();

        // Test all OR logic combinations
        assertTrue(testOr(gate, true, true)); // T|T = T
        assertTrue(testOr(gate, true, false)); // T|F = T
        assertTrue(testOr(gate, false, true)); // F|T = T
        assertFalse(testOr(gate, false, false));// F|F = F
    }

    @Test
    void testOrGateMethods() {
        orr gate = new orr();

        // Basic properties
        assertEquals(2, gate.getInputs());
        assertEquals(1, gate.getOutputs());
        assertTrue(gate.getName().startsWith("Or"));

        // Position and copy
        gate.setPosition(300, 400);
        orr copy = (orr) gate.copy();
        assertEquals(gate.getPosition(), copy.getPosition());
    }

    private boolean testOr(orr gate, boolean in1, boolean in2) {
        gate.setInputVal(0, in1);
        gate.setInputVal(1, in2);
        gate.evaluate();
        return gate.getOutputVal(0);
    }
}