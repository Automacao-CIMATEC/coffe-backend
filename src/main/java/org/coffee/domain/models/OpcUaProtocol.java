package org.coffee.domain.models;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.coffee.domain.abstracts.AbstractProtocol;

public class OpcUaProtocol extends AbstractProtocol {
    private OpcUaClient client;
    private boolean isConnected = false;
    private final int namespaceIndex;
    private final String nodeIdPrefix;

    public OpcUaProtocol(String ip, int port) {
        this(ip, port, 4, ""); // Default namespace index 4, no prefix
    }

    public OpcUaProtocol(String ip, int port, int namespaceIndex, String nodeIdPrefix) {
        super(ip, port);
        this.namespaceIndex = namespaceIndex;
        this.nodeIdPrefix = nodeIdPrefix != null ? nodeIdPrefix : "";
    }

    @Override
    public void openConnection() {
        try {
            String endpointUrl = "opc.tcp://" + getM_ip() + ":" + getM_port();
            client = OpcUaClient.create(endpointUrl);
            client.connect().get();
            setConnected(true);
            System.out.println("Connected to OPC-UA Server at " + endpointUrl);
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
            setConnected(false);
        }
    }

    @Override
    public void closeConnection() {
        if (client != null) {
            try {
                client.disconnect().get();
                setConnected(false);
                System.out.println("Connection closed");
            } catch (Exception e) {
                System.err.println("Error disconnecting: " + e.getMessage());
            }
        } else {
            System.out.println("Connection already down");
        }
    }

    // READ METHODS ===========================================================

    @Override
    public int readDataInt(int registerAddress) {
        return readDataInt(String.valueOf(registerAddress));
    }

    public int readDataInt(String nodeId) {
        if (!isConnected()) return 0;

        try {
            NodeId node = new NodeId(namespaceIndex, nodeIdPrefix + nodeId);
            DataValue value = client.readValue(0, TimestampsToReturn.Both, node).get();

            Object rawValue = value.getValue().getValue();
            if (rawValue instanceof Number) {
                return ((Number) rawValue).intValue();
            } else {
                System.err.println("Value is not a number: " + rawValue);
                return -1;
            }
        } catch (Exception e) {
            System.err.println("Read int error: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public float readDataFloat(int registerAddress) {
        return readDataFloat(String.valueOf(registerAddress));
    }

    public float readDataFloat(String nodeId) {
        if (!isConnected()) return 0;

        try {
            NodeId node = new NodeId(namespaceIndex, nodeIdPrefix + nodeId);
            DataValue value = client.readValue(0, TimestampsToReturn.Both, node).get();

            Object rawValue = value.getValue().getValue();
            if (rawValue instanceof Number) {
                return ((Number) rawValue).floatValue();
            } else {
                System.err.println("Value is not a number: " + rawValue);
                return -1;
            }
        } catch (Exception e) {
            System.err.println("Read float error: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public boolean readDataBoolean(int coilAddress) {
        return readDataBoolean(String.valueOf(coilAddress));
    }

    public boolean readDataBoolean(String nodeId) {
        if (!isConnected()) return false;

        try {
            NodeId node = new NodeId(namespaceIndex, nodeIdPrefix + nodeId);
            DataValue value = client.readValue(0, TimestampsToReturn.Both, node).get();

            Object rawValue = value.getValue().getValue();
            if (rawValue instanceof Boolean) {
                return (Boolean) rawValue;
            } else {
                System.err.println("Value is not a boolean: " + rawValue);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Read boolean error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String readDataString() {
        return "";
    }

    @Override
    public String readDataString(int startingAddress, int registerCount) {
        return readDataString(String.valueOf(startingAddress));
    }

    public String readDataString(String nodeId) {
        if (!isConnected()) return "";

        try {
            NodeId node = new NodeId(namespaceIndex, nodeIdPrefix + nodeId);
            DataValue value = client.readValue(0, TimestampsToReturn.Both, node).get();

            Object rawValue = value.getValue().getValue();
            return rawValue != null ? rawValue.toString() : "";
        } catch (Exception e) {
            System.err.println("Read string error: " + e.getMessage());
            return "";
        }
    }

    // Generic read method for any data type
    public Object readData(String nodeId) {
        if (!isConnected()) return null;

        try {
            NodeId node = new NodeId(namespaceIndex, nodeIdPrefix + nodeId);
            DataValue value = client.readValue(0, TimestampsToReturn.Both, node).get();
            return value.getValue().getValue();
        } catch (Exception e) {
            System.err.println("Read data error: " + e.getMessage());
            return null;
        }
    }

    // WRITE METHODS ==========================================================

    @Override
    public void writeData(int data) {
        // Not implemented - requires address
    }

    @Override
    public void writeData(float data) {
        // Not implemented - requires address
    }

    @Override
    public void writeData(boolean data) {
        // Not implemented - requires address
    }

    @Override
    public void writeData(String data) {
        // Not implemented - requires address
    }

    @Override
    public void writeData(int address, int data) {
        writeData(String.valueOf(address), data);
    }

    public void writeData(String nodeId, int data) {
        if (!isConnected()) return;

        try {
            NodeId node = new NodeId(namespaceIndex, nodeIdPrefix + nodeId);
            DataValue value = new DataValue(new Variant(data), null, null);
            client.writeValue(node, value).get();
            System.out.println("Successfully wrote INT to node: " + nodeId);
        } catch (Exception e) {
            System.err.println("Write int error: " + e.getMessage());
        }
    }

    @Override
    public void writeData(int address, float data) {
        writeData(String.valueOf(address), data);
    }

    public void writeData(String nodeId, float data) {
        if (!isConnected()) return;

        try {
            NodeId node = new NodeId(namespaceIndex, nodeIdPrefix + nodeId);
            DataValue value = new DataValue(new Variant(data), null, null);
            client.writeValue(node, value).get();
            System.out.println("Successfully wrote FLOAT to node: " + nodeId);
        } catch (Exception e) {
            System.err.println("Write float error: " + e.getMessage());
        }
    }

    @Override
    public void writeData(int address, boolean data) {
        writeData(String.valueOf(address), data);
    }

    public void writeData(String nodeId, boolean data) {
        if (!isConnected()) return;

        try {
            NodeId node = new NodeId(namespaceIndex, nodeIdPrefix + nodeId);
            DataValue value = new DataValue(new Variant(data), null, null);
            client.writeValue(node, value).get();
            System.out.println("Successfully wrote BOOLEAN to node: " + nodeId);
        } catch (Exception e) {
            System.err.println("Write boolean error: " + e.getMessage());
        }
    }

    @Override
    public void writeData(int startingAddress, String data) {
        writeData(String.valueOf(startingAddress), data);
    }

    public void writeData(String nodeId, String data) {
        if (!isConnected()) return;

        try {
            NodeId node = new NodeId(namespaceIndex, nodeIdPrefix + nodeId);
            DataValue value = new DataValue(new Variant(data), null, null);
            client.writeValue(node, value).get();
            System.out.println("Successfully wrote STRING to node: " + nodeId);
        } catch (Exception e) {
            System.err.println("Write string error: " + e.getMessage());
        }
    }

    // Generic write method for any data type
    public void writeData(String nodeId, Object data) {
        if (!isConnected()) return;

        try {
            NodeId node = new NodeId(namespaceIndex, nodeIdPrefix + nodeId);
            DataValue value = new DataValue(new Variant(data), null, null);
            client.writeValue(node, value).get();
            System.out.println("Successfully wrote " + data.getClass().getSimpleName() + " to node: " + nodeId);
        } catch (Exception e) {
            System.err.println("Write data error: " + e.getMessage());
        }
    }

    // GETTERS AND SETTERS ====================================================

    public int getNamespaceIndex() {
        return namespaceIndex;
    }

    public String getNodeIdPrefix() {
        return nodeIdPrefix;
    }

    public boolean isConnected() {
        return isConnected;
    }

    private void setConnected(boolean connected) {
        isConnected = connected;
    }
}