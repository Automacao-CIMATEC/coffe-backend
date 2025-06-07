package org.coffee.domain.models;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.coffee.domain.abstracts.AbstractProtocol;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class MqttProtocol extends AbstractProtocol implements MqttCallback {
    private MqttClient client;
    private boolean isConnected = false;
    private final String clientId;
    private final int qos;
    private final Map<String, String> lastMessages = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<String>> pendingReads = new ConcurrentHashMap<>();

    public MqttProtocol(String brokerHost, int brokerPort) {
        this(brokerHost, brokerPort, 1); // Default QoS 1
    }

    public MqttProtocol(String brokerHost, int brokerPort, int qos) {
        super(brokerHost, brokerPort);
        this.clientId = "JavaMqttClient_" + System.currentTimeMillis();
        this.qos = qos;
    }

    public MqttProtocol(String brokerHost, int brokerPort, String clientId, int qos) {
        super(brokerHost, brokerPort);
        this.clientId = clientId != null ? clientId : "JavaMqttClient_" + System.currentTimeMillis();
        this.qos = qos;
    }

    @Override
    public void openConnection() {
        try {
            String brokerUrl = "tcp://" + getM_ip() + ":" + getM_port();
            MemoryPersistence persistence = new MemoryPersistence();

            client = new MqttClient(brokerUrl, clientId, persistence);
            client.setCallback(this);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(20);

            client.connect(options);
            setConnected(true);
            System.out.println("Connected to MQTT Broker at " + brokerUrl);
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
            setConnected(false);
        }
    }

    @Override
    public void closeConnection() {
        if (client != null) {
            try {
                if (client.isConnected()) {
                    client.disconnect();
                }
                client.close();
                setConnected(false);
                System.out.println("Connection closed");
            } catch (Exception e) {
                System.err.println("Error disconnecting: " + e.getMessage());
            }
        } else {
            System.out.println("Connection already down");
        }
    }

    // MQTT Callback Methods
    @Override
    public void connectionLost(Throwable cause) {
        System.err.println("MQTT Connection lost: " + cause.getMessage());
        setConnected(false);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        System.out.println("Message received: [" + topic + "] " + payload);

        // Store the last message for each topic
        lastMessages.put(topic, payload);

        // Complete any pending read operations for this topic
        CompletableFuture<String> pendingRead = pendingReads.remove(topic);
        if (pendingRead != null) {
            pendingRead.complete(payload);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("Message delivery completed for topic: " + token.getTopics()[0]);
    }

    // READ METHODS (Subscribe and get messages) ===============================

    @Override
    public int readDataInt(int registerAddress) {
        return readDataInt(String.valueOf(registerAddress));
    }

    public int readDataInt(String topic) {
        String value = readDataString(topic);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("Cannot convert to int: " + value);
            return -1;
        }
    }

    @Override
    public float readDataFloat(int registerAddress) {
        return readDataFloat(String.valueOf(registerAddress));
    }

    public float readDataFloat(String topic) {
        String value = readDataString(topic);
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            System.err.println("Cannot convert to float: " + value);
            return -1.0f;
        }
    }

    @Override
    public boolean readDataBoolean(int coilAddress) {
        return readDataBoolean(String.valueOf(coilAddress));
    }

    public boolean readDataBoolean(String topic) {
        String value = readDataString(topic);
        return Boolean.parseBoolean(value);
    }

    @Override
    public String readDataString() {
        return "";
    }

    @Override
    public String readDataString(int startingAddress, int registerCount) {
        return readDataString(String.valueOf(startingAddress));
    }

    public String readDataString(String topic) {
        if (!isConnected()) return "";

        try {
            // Check if we already have a message for this topic
            String lastMessage = lastMessages.get(topic);
            if (lastMessage != null) {
                return lastMessage;
            }

            // Subscribe to the topic and wait for a message
            return subscribeAndWaitForMessage(topic, 10); // 10 second timeout
        } catch (Exception e) {
            System.err.println("Read string error: " + e.getMessage());
            return "";
        }
    }

    public String subscribeAndWaitForMessage(String topic, int timeoutSeconds) {
        if (!isConnected()) return "";

        try {
            // Create a future to wait for the message
            CompletableFuture<String> future = new CompletableFuture<>();
            pendingReads.put(topic, future);

            // Subscribe to the topic
            client.subscribe(topic, qos);
            System.out.println("Subscribed to topic: " + topic);

            // Wait for the message with timeout
            String message = future.get(timeoutSeconds, TimeUnit.SECONDS);
            return message;
        } catch (Exception e) {
            System.err.println("Error waiting for message: " + e.getMessage());
            pendingReads.remove(topic);
            return "";
        }
    }

    public String getLastMessage(String topic) {
        return lastMessages.get(topic);
    }

    public void subscribeTopic(String topic) {
        if (!isConnected()) return;

        try {
            client.subscribe(topic, qos);
            System.out.println("Subscribed to topic: " + topic);
        } catch (Exception e) {
            System.err.println("Subscribe error: " + e.getMessage());
        }
    }

    public void unsubscribeTopic(String topic) {
        if (!isConnected()) return;

        try {
            client.unsubscribe(topic);
            lastMessages.remove(topic);
            System.out.println("Unsubscribed from topic: " + topic);
        } catch (Exception e) {
            System.err.println("Unsubscribe error: " + e.getMessage());
        }
    }

    // WRITE METHODS (Publish messages) ========================================

    @Override
    public void writeData(int data) {
        // Not implemented - requires topic
    }

    @Override
    public void writeData(float data) {
        // Not implemented - requires topic
    }

    @Override
    public void writeData(boolean data) {
        // Not implemented - requires topic
    }

    @Override
    public void writeData(String data) {
        // Not implemented - requires topic
    }

    @Override
    public void writeData(int address, int data) {
        writeData(String.valueOf(address), String.valueOf(data));
    }

    public void writeData(String topic, int data) {
        writeData(topic, String.valueOf(data));
    }

    @Override
    public void writeData(int address, float data) {
        writeData(String.valueOf(address), String.valueOf(data));
    }

    public void writeData(String topic, float data) {
        writeData(topic, String.valueOf(data));
    }

    @Override
    public void writeData(int address, boolean data) {
        writeData(String.valueOf(address), String.valueOf(data));
    }

    public void writeData(String topic, boolean data) {
        writeData(topic, String.valueOf(data));
    }

    @Override
    public void writeData(int startingAddress, String data) {
        writeData(String.valueOf(startingAddress), data);
    }

    public void writeData(String topic, String data) {
        if (!isConnected()) return;

        try {
            MqttMessage message = new MqttMessage(data.getBytes());
            message.setQos(qos);
            message.setRetained(false);

            client.publish(topic, message);
            System.out.println("Published to '" + topic + "': " + data);
        } catch (Exception e) {
            System.err.println("Publish error: " + e.getMessage());
        }
    }

    public void writeDataRetained(String topic, String data) {
        if (!isConnected()) return;

        try {
            MqttMessage message = new MqttMessage(data.getBytes());
            message.setQos(qos);
            message.setRetained(true);

            client.publish(topic, message);
            System.out.println("Published retained message to '" + topic + "': " + data);
        } catch (Exception e) {
            System.err.println("Publish retained error: " + e.getMessage());
        }
    }

    // GETTERS AND SETTERS ====================================================

    public String getClientId() {
        return clientId;
    }

    public int getQos() {
        return qos;
    }

    public boolean isConnected() {
        return isConnected && (client != null && client.isConnected());
    }

    private void setConnected(boolean connected) {
        isConnected = connected;
    }

    public Map<String, String> getAllLastMessages() {
        return new ConcurrentHashMap<>(lastMessages);
    }

    public void clearLastMessages() {
        lastMessages.clear();
    }
}