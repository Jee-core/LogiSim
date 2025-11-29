package org.yourcompany.yourproject.Backend.businessLayer.components;

import org.yourcompany.yourproject.Backend.businessLayer.components.Circuit;
import org.yourcompany.yourproject.Backend.businessLayer.components.GateComponent;
import org.yourcompany.yourproject.Backend.businessLayer.components.Connector;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private String name;
    private List<Circuit> circuits = new ArrayList<>(); // must be Frontend.components.Circuit

    public Project(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public List<Circuit> getCircuits() { return circuits; }

    @Override
    public String toString() { return name; }
}