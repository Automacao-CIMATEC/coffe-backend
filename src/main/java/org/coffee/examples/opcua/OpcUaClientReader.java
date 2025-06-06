package org.coffee.examples.opcua;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;

public class OpcUaClientReader {
    public static void main(String[] args) {
        final String NODE_ID = "|var|XP325.Application.OPCUA_VARS.testeAut";
        final int UPDATE_INTERVAL_MS = 1000; // 1 second

        OpcUaClient client = null;
        try {
            client = OpcUaClient.create("opc.tcp://192.168.1.25:4840");
            client.connect().get();
            System.out.println("Monitoring OPC UA variable... (Ctrl+C to stop)");

            while (true) {
                try {
                    DataValue value = client.readValue(0, TimestampsToReturn.Both,
                            new NodeId(4, NODE_ID)).get();

                    // Clear console and show simple output
                    System.out.print("\r"); // Return to line start
                    System.out.print("testeAut: " + value.getValue().getValue());
                    System.out.flush();

                    Thread.sleep(UPDATE_INTERVAL_MS);
                } catch (Exception e) {
                    System.err.println("\nError reading value: " + e.getMessage());
                    Thread.sleep(5000); // Wait 5s before retrying
                }
            }
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
        } finally {
            if (client != null) {
                try {
                    client.disconnect().get();
                    System.out.println("\nDisconnected from OPC UA server");
                } catch (Exception e) {
                    System.err.println("Error disconnecting: " + e.getMessage());
                }
            }
        }
    }
}