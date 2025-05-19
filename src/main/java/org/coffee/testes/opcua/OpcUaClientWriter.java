package org.coffee.testes.opcua;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import java.util.Scanner;

public class OpcUaClientWriter {
    public static void main(String[] args) {
        final String BASE_ID = "|var|XP325.Application.OPCUA_VARS.";
        OpcUaClient client = null;
        Scanner scanner = new Scanner(System.in);

        try {
            // 1. Initialize connection
            client = OpcUaClient.create("opc.tcp://192.168.1.25:4840");
            client.connect().get();
            System.out.println("Connected to OPC UA server");

            // 2. Get user input
            System.out.print("Enter variable name (without prefix): ");
            String varName = scanner.nextLine().trim();
            System.out.print("Enter boolean value (true/false): ");
            boolean boolValue = scanner.nextBoolean();

            // 3. Construct full node ID
            String fullNodeId = BASE_ID + varName;
            NodeId nodeId = new NodeId(4, fullNodeId);

            // 4. Prepare and write value
            DataValue value = new DataValue(
                    new Variant(boolValue),
                    null,  // status
                    null   // timestamp
            );

            client.writeValue(nodeId, value).get();
            System.out.println("Successfully wrote " + boolValue + " to " + varName);

            // 5. Verify write
            DataValue readBack = client.readValue(0, TimestampsToReturn.Both, nodeId).get();
            System.out.println("Verified value: " + readBack.getValue().getValue());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            if (client != null) {
                try {
                    client.disconnect().get();
                    System.out.println("Disconnected from server");
                } catch (Exception e) {
                    System.err.println("Error disconnecting: " + e.getMessage());
                }
            }
            scanner.close();
        }
    }
}