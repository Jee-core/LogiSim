package org.yourcompany.yourproject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.yourcompany.yourproject.backend.businessLayer.components.gates.nott;

public class NotGateTest {

    @Test
    void testNotGateLogic() {
        nott gate = new nott();

        // Test NOT logic
        assertFalse(testNot(gate, true)); // !T = F
        assertTrue(testNot(gate, false)); // !F = T
    }

    @Test
    void testNotGateMethods() {
        nott gate = new nott();

        // Basic properties
        assertEquals(1, gate.getInputs());
        assertEquals(1, gate.getOutputs());
        assertTrue(gate.getName().startsWith("NOT"));

        // Special NOT methods
        gate.setInputValue(true);
        assertEquals(true, gate.getInputValue());

        // Position and copy
        gate.setPosition(500, 600);
        nott copy = (nott) gate.copy();
        assertEquals(gate.getPosition(), copy.getPosition());
    }

    private boolean testNot(nott gate, boolean input) {
        gate.setInputVal(0, input);
        gate.evaluate();
        return gate.getOutputVal(0);
    }
}