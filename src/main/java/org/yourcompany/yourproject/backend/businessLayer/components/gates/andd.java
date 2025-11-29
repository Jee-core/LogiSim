package org.yourcompany.yourproject.backend.businessLayer.components.gates;

import java.awt.Point;

import org.yourcompany.yourproject.backend.businessLayer.components.GateComponent;

public class andd extends GateComponent {
    static int andCounter = 0;

    // ===== CONSTRUCTORS =====

    public andd() {
        super("AND" + (++andCounter), 2, 1); // AND gate has 2 inputs and 1 output
    }

    public andd(andd other) {
        super(other.getName(), other.getInputs(), other.getOutputs());
        setPosition(new Point(other.getPosition()));
        // Copy input/output values
        for (int i = 0; i < other.getInputs(); i++)
            setInputVal(i, other.getInputVal(i));
        for (int i = 0; i < other.getOutputs(); i++)
            setOutputVal(i, other.getOutputVal(i));
    }

    // ===== OBJECT COPY OPERATIONS =====

    @Override
    public GateComponent copy() {

        andd copy = new andd();
        copy.setPosition(this.getPosition().x, this.getPosition().y);
        copy.setName(this.getName() + "_Copy");
        return copy;

    }

    // ===== CORE GATE LOGIC =====

    @Override
    protected void calculateOutput() {

        // AND logic: output is true only if all inputs are true
        Boolean input1 = getInputVal(0);
        Boolean input2 = getInputVal(1);
        Boolean result = input1 && input2;
        setOutputVal(0, result);
    }

    @Override
    public void update() {
        if (getInputs() >= 2) {
            boolean result = getInputVal(0) && getInputVal(1);
            setOutputVal(0, result);
        } else if (getInputs() == 1) {
            setOutputVal(0, getInputVal(0));
        } else {
            setOutputVal(0, false);
        }
    }

    @Override
    public void evaluate() {
        // Simply call computeOutput to update output based on current inputs
        calculateOutput();
    }

}