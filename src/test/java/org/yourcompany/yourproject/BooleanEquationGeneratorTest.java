package org.yourcompany.yourproject;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yourcompany.yourproject.backend.businessLayer.analysis.BooleanEquationGenerator;
import org.yourcompany.yourproject.backend.businessLayer.components.Circuit;
import org.yourcompany.yourproject.backend.businessLayer.components.gates.andd;
import org.yourcompany.yourproject.backend.businessLayer.components.gates.nott;
import org.yourcompany.yourproject.backend.businessLayer.components.gates.orr;

public class BooleanEquationGeneratorTest {

    private BooleanEquationGenerator generator;
    private Circuit circuit;

    @BeforeEach
    void setUp() {
        generator = new BooleanEquationGenerator();
        circuit = new Circuit("TestCircuit");
    }

    @Test
    void testBasicAndGateExpression() {
        andd andGate = new andd();
        circuit.addGateFunc(andGate);

        String expression = generator.getExpression(andGate, circuit);

        // Test function-style output: "AND1(AND1_IN0, AND1_IN1)"
        assertTrue(expression.contains("AND"));
        assertTrue(expression.contains("("));
        assertTrue(expression.contains(")"));
        assertTrue(expression.contains(","));
    }

    @Test
    void testBasicOrGateExpression() {
        orr orGate = new orr();
        circuit.addGateFunc(orGate);

        String expression = generator.getExpression(orGate, circuit);

        // Test function-style output: "Or1(Or1_IN0, Or1_IN1)"
        assertTrue(expression.contains("Or"));
        assertTrue(expression.contains("("));
        assertTrue(expression.contains(")"));
        assertTrue(expression.contains(","));
    }

    @Test
    void testBasicNotGateExpression() {
        nott notGate = new nott();
        circuit.addGateFunc(notGate);

        String expression = generator.getExpression(notGate, circuit);

        // Test function-style output: "NOT1(NOT1_IN0)"
        assertTrue(expression.contains("NOT"));
        assertTrue(expression.contains("("));
        assertTrue(expression.contains(")"));
    }

    @Test
    void testConnectedGatesExpression() {
        andd andGate = new andd();
        nott notGate = new nott();

        circuit.addGateFunc(andGate);
        circuit.addGateFunc(notGate);

        // Connect AND output to NOT input
        circuit.gatesConnectorFunc(andGate, 0, notGate, 0);

        String expression = generator.getExpression(notGate, circuit);

        // Should be something like: "NOT1(AND1(AND1_IN0, AND1_IN1))"
        assertTrue(expression.contains("NOT"));
        assertTrue(expression.contains("AND"));
        assertTrue(expression.contains("("));
        assertTrue(expression.contains(")"));
    }

    @Test
    void testComplexCircuitExpression() {
        andd andGate = new andd();
        orr orGate = new orr();
        nott notGate = new nott();

        circuit.addGateFunc(andGate);
        circuit.addGateFunc(orGate);
        circuit.addGateFunc(notGate);

        // Connect AND to OR's first input
        circuit.gatesConnectorFunc(andGate, 0, orGate, 0);
        // Connect NOT to OR's second input
        circuit.gatesConnectorFunc(notGate, 0, orGate, 1);

        String expression = generator.getExpression(orGate, circuit);

        // Should be something like: "Or1(AND1(AND1_IN0, AND1_IN1), NOT1(NOT1_IN0))"
        assertTrue(expression.contains("Or"));
        assertTrue(expression.contains("AND"));
        assertTrue(expression.contains("NOT"));
        assertTrue(expression.contains("("));
        assertTrue(expression.contains(")"));
        assertTrue(expression.contains(","));
    }

    @Test
    void testEmptyCircuit() {
        Circuit emptyCircuit = new Circuit("Empty");
        andd gate = new andd();

        String expression = generator.getExpression(gate, emptyCircuit);

        // Should still produce function-style expression
        assertNotNull(expression);
        assertFalse(expression.isEmpty());
        assertTrue(expression.contains("AND"));
    }

    @Test
    void testExpressionNotNull() {
        andd andGate = new andd();
        circuit.addGateFunc(andGate);

        String expression = generator.getExpression(andGate, circuit);

        assertNotNull(expression);
        assertFalse(expression.isEmpty());
    }

    @Test
    void testExpressionFormat() {
        andd andGate = new andd();
        circuit.addGateFunc(andGate);

        String expression = generator.getExpression(andGate, circuit);

        // Should follow pattern: "GATENAME(GATENAME_IN0, GATENAME_IN1, ...)"
        assertTrue(expression.matches("^[A-Za-z0-9]+\\([A-Za-z0-9_, ]+\\)$"));
    }

    @Test
    void testMultipleGatesInCircuit() {
        andd andGate1 = new andd();
        andd andGate2 = new andd();
        orr orGate = new orr();

        circuit.addGateFunc(andGate1);
        circuit.addGateFunc(andGate2);
        circuit.addGateFunc(orGate);

        // Connect both AND gates to OR gate
        circuit.gatesConnectorFunc(andGate1, 0, orGate, 0);
        circuit.gatesConnectorFunc(andGate2, 0, orGate, 1);

        String expression = generator.getExpression(orGate, circuit);

        // Should contain both AND gates and OR gate
        assertTrue(expression.contains("Or"));
        assertTrue(expression.contains("AND"));
        assertTrue(expression.contains("("));
        assertTrue(expression.contains(")"));
        assertTrue(expression.contains(","));
    }

    @Test
    void testExternalInputs() {
        andd andGate = new andd();
        circuit.addGateFunc(andGate);

        String expression = generator.getExpression(andGate, circuit);

        // Should contain input placeholders like "_IN0", "_IN1"
        assertTrue(expression.contains("_IN0"));
        assertTrue(expression.contains("_IN1"));
    }
}