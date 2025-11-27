package org.yourcompany.yourproject.Backend.businessLayer.components;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public abstract class GateComponent 
{
    private String id; // Unique identifier for the component
    private String name;
    private List<Boolean> inputValues;
    private List<Boolean> outputValues;
    private int input;
    private int output;
    private Point posi;
    private List<Connector> inputConnectorsList; // Wires connected to inputs
    private List<Connector> outputConnectorsList; // Wires connected to outputs

    // ===== CONSTRUCTOR AND INITIALIZATION =====
    
    public GateComponent(String name, int inputs, int outputs) 
    {
        this.name = name;
        this.id = generateId();
        this.posi = new Point(0, 0);

        this.input = inputs;
        this.output = outputs;

        this.inputValues = new ArrayList<>();
        this.outputValues = new ArrayList<>();
        
        this.inputConnectorsList = new ArrayList<>();
        this.outputConnectorsList = new ArrayList<>();
        
        // Initialize input/output values to false
        for (int i = 0; i < inputs; i++) 
        {
            this.inputValues.add(false);
            this.inputConnectorsList.add(null);
        }
        
        
        
        for (int i = 0; i < outputs; i++) 
        {
            this.outputValues.add(false);
            this.outputConnectorsList.add(null);
        }
    }

    private String generateId() 
    {
        return name + "_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    // ===== ABSTRACT METHODS =====
    
    protected abstract void calculateOutput();
    public abstract void evaluate();
    public abstract GateComponent copy();
    public abstract void update();

    // ===== BASIC PROPERTY ACCESSORS =====
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getInputs() { return input; }
    public void setInputs(int inputs) { this.input = inputs; }

    public int getOutputs() { return output; }
    public void setOutputs(int outputs) { this.output = outputs; }

    public Point getPosition() { return posi; }
    public void setPosition(Point position) { this.posi = position; }
    public void setPosition(int x, int y) { this.posi = new Point(x, y); }

    public String getId() { return id; }

    // ===== INPUT VALUE MANAGEMENT =====
    
    public Boolean getInputVal(int index) 
    {
        if (index >= 0 && index < inputValues.size()) 
        {
            return inputValues.get(index);
        }
        return false;
    }

    public void setInputVal(int index, Boolean value) 
    {
        if (index >= 0 && index < inputValues.size()) 
        {
            inputValues.set(index, value);
            // When input changes, recompute output
            calculateOutput();
        }
    }

    /**
     * Set input value directly without triggering computeOutput (for copying state)
     */
    public void assignInputDirectly(int index, Boolean value) 
    {
        if (index >= 0 && index < inputValues.size()) 
        {
            inputValues.set(index, value);
            // Don't call computeOutput here - that's the key difference
        }
    }

    public List<Boolean> getInputVal() 
    {
         return new ArrayList<>(inputValues); 
    }

    /**
     * Get direct access to input values list (package-private)
     */
    public List<Boolean> getInputDirectly() 
    {
        return inputValues;
    }

    // ===== OUTPUT VALUE MANAGEMENT =====
    
    public Boolean getOutputVal(int index) 
    {
        if (index >= 0 && index < outputValues.size()) 
        {
            return outputValues.get(index);
        }
        return false;
    }

    public void setOutputVal(int index, Boolean value) 
    {
        if (index >= 0 && index < outputValues.size()) 
        {
            outputValues.set(index, value);
            // Propagate output signal to connected wires
            transmitOutputSignal(index);
        }
    }

    /**
     * Set output value directly without triggering propagation (for copying state)
     */
    public void assignOutputDirectly(int index, Boolean value) 
    {
        if (index >= 0 && index < outputValues.size()) 
        {
            outputValues.set(index, value);
            // Don't call propagateOutput here
        }
    }

    public List<Boolean> getOutputValues()
    {
        return new ArrayList<>(outputValues); 
    }

    /**
     * Get direct access to output values list (package-private)  
     */
    public List<Boolean> getOutputDirectly() 
    {
        return outputValues;
    }

    // ===== CONNECTOR MANAGEMENT =====
    
    public void attachInputConnector(int portIndex, Connector wire) 
    {
        if (portIndex >= 0 && portIndex < inputConnectorsList.size()) 
        {
            inputConnectorsList.set(portIndex, wire);
        }
    }

    public void attachOutputConnector(int portIndex, Connector wire) 
    {
        if (portIndex >= 0 && portIndex < outputConnectorsList.size()) 
        {
            outputConnectorsList.set(portIndex, wire);
        }
    }

    public Connector getInputWire(int portIndex) 
    {
        if (portIndex >= 0 && portIndex < inputConnectorsList.size()) 
        {
            return inputConnectorsList.get(portIndex);
        }
        return null;
    }

    public Connector getOutputWire(int portIndex) 
    {
        if (portIndex >= 0 && portIndex < outputConnectorsList.size()) 
        {
            return outputConnectorsList.get(portIndex);
        }
        return null;
    }

    public List<Connector> getInputConnectors() { return new ArrayList<>(inputConnectorsList); }
    public List<Connector> getOutputConnectors() { return new ArrayList<>(outputConnectorsList); }

    // ===== SIGNAL PROPAGATION =====
    
    // Propagate output signal to connected wires
    private void transmitOutputSignal(int outputIndex) 
    {
        Connector wire = getOutputWire(outputIndex);
        
        
        if (wire != null && wire.getToGate() != null) 
        {
            Boolean outputValue = getOutputVal(outputIndex);
            int inputPortIndex = wire.getDestinationPortIndex();
            wire.getToGate().setInputVal(inputPortIndex, outputValue);
        }
    }

    // Update input from connected wire
    public void updateInputFromWire(int inputIndex) 
    {
        Connector wire = getInputWire(inputIndex);
        
        
        if (wire != null && wire.getFromGate() != null) 
        {
            int outputPortIndex = wire.getSourcePortIndex();
            Boolean inputValue = wire.getFromGate().getOutputVal(outputPortIndex);
            setInputVal(inputIndex, inputValue);
        }
    }

    // ===== VISUAL POSITIONING =====
    
    // Get port positions for visual rendering (relative to component position)
    public Point calculateInputPortLocation(int portIndex) 
    {

        if (input == 0) return posi;
        int gateHeight = 70;
        int spacing = gateHeight / (input + 1);
        int y = posi.y + spacing * (portIndex + 1);
        
        return new Point(posi.x, y);
    }

    public Point calculateOutputPortLocation(int portIndex) 
    {
        if (output == 0) return posi;
        int gateHeight = 70;
        int gateWidth = 100;
        int spacing = gateHeight / (output + 1);
        int y = posi.y + spacing * (portIndex + 1);
    
        return new Point(posi.x + gateWidth, y);
    }

    // ===== CONVENIENCE METHODS =====
    // Add a helper to get single output (for 1-output gates)
    public Boolean getOutput() 
    {
        return output > 0 ? outputValues.get(0) : false;
    }

    public void setOutput(Boolean value) 
    {
        
        if (output > 0) 
        {
            outputValues.set(0, value);
            transmitOutputSignal(0);
        }
    }

    public void computeOutput() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'computeOutput'");
    }
}