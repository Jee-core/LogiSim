package org.yourcompany.yourproject.backend.businessLayer.components;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private String name;
    private List<Circuit> circuits = new ArrayList<>(); // must be Frontend.components.Circuit

    public Project(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Circuit> getCircuits() {
        return circuits;
    }

    @Override
    public String toString() {
        return name;
    }
}