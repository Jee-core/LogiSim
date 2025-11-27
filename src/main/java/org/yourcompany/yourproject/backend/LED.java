package org.yourcompany.yourproject.Backend.businessLayer.components.gates;

import java.awt.Color;
import java.awt.Point;

import org.yourcompany.yourproject.Backend.businessLayer.components.GateComponent;

public class LED extends GateComponent {
    private boolean isLit;
    private Color ledColor;
    
    public LED() {
        super("LED", 1, 0); // CORRECT: 1 input, 0 outputs
        this.isLit = false;
        this.ledColor = Color.RED;
        setPosition(0, 0);
    }
    
    @Override
    public void computeOutput() {
        // LED lights up when input is true
        Boolean inputVal = getInputVal(0);
        this.isLit = (inputVal != null && inputVal);
        this.ledColor = isLit ? Color.GREEN : Color.RED;
        
        // LED has no outputs to set
    }
    
    @Override
    protected void calculateOutput() {
        computeOutput(); // Delegate to computeOutput
    }
    
    @Override
    public void evaluate() {
        calculateOutput(); // Same as calculateOutput for LED
    }
    
    @Override
    public void update() {
        // Update LED state based on current inputs
        computeOutput();
    }
    
    @Override
    public Boolean getOutputVal(int portIndex) {
        // LED has no outputs, return null
        return null;
    }
    
    public boolean isLit() {
        return isLit;
    }
    
    public Color getLedColor() {
        return ledColor;
    }
    
    @Override
    public GateComponent copy() {
        LED led = new LED();
        led.setPosition(getPosition().x, getPosition().y);
        led.isLit = this.isLit;
        led.ledColor = this.ledColor;
        return led;
    }
}