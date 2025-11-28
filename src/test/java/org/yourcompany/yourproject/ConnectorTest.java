package org.yourcompany.yourproject;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yourcompany.yourproject.Backend.businessLayer.components.Connector;
import org.yourcompany.yourproject.Backend.businessLayer.components.gates.andd;
import org.yourcompany.yourproject.Backend.businessLayer.components.gates.nott;
import org.yourcompany.yourproject.Backend.businessLayer.components.gates.orr;

public class ConnectorTest {
    
    private andd sourceGate;
    private orr destGate;
    private Connector connector;
    
    @BeforeEach
    void setUp() {
        sourceGate = new andd();
        destGate = new orr();
        connector = new Connector(sourceGate, 0, destGate, 0);
    }
    
    @Test
    void testConnectorCreation() {
        assertNotNull(connector);
        assertEquals(sourceGate, connector.getFromGate());
        assertEquals(destGate, connector.getToGate());
        assertEquals(0, connector.getSourcePortIndex());
        assertEquals(0, connector.getDestinationPortIndex());
        assertNotNull(connector.getId());
    }
    
    @Test
    void testSignalTransmission() {
        sourceGate.setInputVal(0, true);
        sourceGate.setInputVal(1, true);
        sourceGate.evaluate();
        
        connector.transmitSig();
        
        assertTrue(destGate.getInputVal(0));
    }
    
    @Test
    void testConnectionStatus() {
        assertTrue(connector.isConnected());
        
        connector.disconnectWire();
        
        assertFalse(connector.isConnected());
        assertNull(connector.getFromGate());
        assertNull(connector.getToGate());
    }
    
    @Test
    void testWireReconnection() {
        nott newDestGate = new nott();
        
        connector.setToGate(newDestGate);
        connector.setDestinationPortIndex(0);
        
        assertEquals(newDestGate, connector.getToGate());
        assertEquals(0, connector.getDestinationPortIndex());
    }
    
    @Test
    void testSignalValueStorage() {
        sourceGate.setInputVal(0, true);
        sourceGate.setInputVal(1, true);
        sourceGate.evaluate();
        
        connector.transmitSig();
        
        assertTrue(connector.getSignalValue());
        
        connector.setSignalValue(false);
        assertFalse(connector.getSignalValue());
    }
}