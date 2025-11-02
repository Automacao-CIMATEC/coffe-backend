package org.coffee.examples.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttSubscriber implements MqttCallback {

    private MqttClient client;

    public static void main(String[] args) {
        new MqttSubscriber().subscribe();
    }

    public void subscribe() {
        String broker = "tcp://localhost:1884";
        String topic = "var_1";
        String clientId = "JavaSubscriber_" + System.currentTimeMillis();

        try {
            // Set up persistence for messages
            MemoryPersistence persistence = new MemoryPersistence();

            // Initialize client
            client = new MqttClient(broker, clientId, persistence);

            // Set callback (this class implements MqttCallback)
            client.setCallback(this);

            // Connection options
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            System.out.println("Connecting to broker: " + broker);
            client.connect(options);
            System.out.println("Connected");

            // Subscribe to topic with QoS 1
            client.subscribe(topic, 1);
            System.out.println("Subscribed to topic: " + topic);

            // Keep the program running
            while (true) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.err.println("Connection lost! Attempting to reconnect...");
        while (true) {
            try {
                if (!client.isConnected()) {
                    client.reconnect();
                    System.out.println("Reconnected successfully");
                    break;
                }
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        System.out.println("New message: [" + topic + "] " + new String(message.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Not used for subscribers
    }
}