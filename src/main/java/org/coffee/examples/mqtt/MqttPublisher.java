package org.coffee.examples.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttPublisher {
    public static void main(String[] args) {
        String broker = "tcp://localhost:1884"; // MQTT broker address
        String topic = "test";                  // Topic to publish to
        String message = "Hello from Java!";    // Message content

        try {
            // Create an MQTT client
            MqttClient client = new MqttClient(broker, MqttClient.generateClientId());

            // Connect options
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);

            // Connect to broker
            client.connect(options);
            System.out.println("Connected to broker: " + broker);

            // Publish message
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(1); // Quality of Service level 1
            client.publish(topic, mqttMessage);
            System.out.println("Published to '" + topic + "': " + message);

            // Disconnect
            client.disconnect();
            System.out.println("Disconnected");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}