package org.yourcompany.yourproject.Backend.businessLayer.components.gates;
import java.awt.Point;

import org.yourcompany.yourproject.Backend.businessLayer.components.GateComponent;

public class nott extends GateComponent 
{
    private boolean inputValueNott;
    private static int notCounter = 0;

    // ===== CONSTRUCTORS =====
    
    public nott() 
    {
        super("NOT" + (++notCounter), 1, 1);
    }
    
    public nott(nott other) 
    {
        super(other.getName(), other.getInputs(), other.getOutputs());
        setPosition(new Point(other.getPosition()));
        // Copy input/output values
        for (int i = 0; i < other.getInputs(); i++) 
        {
            setInputVal(i, other.getInputVal(i));
        }
        for (int i = 0; i < other.getOutputs(); i++) 
        {
            setOutputVal(i, other.getOutputVal(i));
        }
        // Also copy internal inputValue field
        this.inputValueNott = other.getInputValue();
    }

    // ===== OBJECT COPY OPERATIONS =====
    
    @Override
    public GateComponent copy() 
    {
        nott copy = new nott();
        copy.setPosition(this.getPosition().x, this.getPosition().y);
        copy.setName(this.getName() + "_Copy");
        // Copy any other relevant properties
        return copy;
    }

    // ===== CORE GATE LOGIC =====
    
    @Override
    protected void calculateOutput() 
    {
        // NOT logic: output is the inverse of the input
        Boolean input = getInputVal(0);
        Boolean result = !input;
        setOutputVal(0, result);
    }

    @Override
    public void update() 
    {
        if (getInputs() >= 1) 
        {
            boolean result = !getInputVal(0);
            setOutputVal(0, result);
        } 
        else 
        {
            setOutputVal(0, true); // NOT gate with no input defaults to true
        }
    }
    
    @Override
    public void evaluate() 
    {
        Boolean input = getInputVal(0); // get the value from the input pin
        setOutputVal(0, !input);        // correctly invert it
    }

    // ===== INPUT VALUE MANAGEMENT =====
    
    public void setInputValue(boolean input) 
    { 
        this.inputValueNott = input; 
    }
    
    public boolean getInputValue() 
    { 
        return inputValueNott; 
    }
}