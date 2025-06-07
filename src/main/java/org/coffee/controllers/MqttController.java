package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.coffee.examples.api.ExampleMessageDto;
import org.coffee.domain.dtos.PlcValueDto;
import org.coffee.domain.enums.PlcDataType;
import org.coffee.domain.models.MqttProtocol;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/mqtt")
@Tag(name = "MQTT", description = "Endpoints para comunicação através do protocolo MQTT")
public class MqttController {

    // Default MQTT port
    private static final int MQTT_PORT = 1883;
    // Default QoS level
    private static final int DEFAULT_QOS = 1;

    @GetMapping("/subscribe")
    @Operation(summary = "Assina um tópico MQTT")
    @ApiResponse(responseCode = "200", description = "Mensagem recebida com sucesso")
    public ResponseEntity<PlcValueDto> subscribeMqtt(
            @Parameter(description = "IP/hostname do broker MQTT", required = true)
            @RequestParam String broker,

            @Parameter(description = "Tópico MQTT", required = true)
            @RequestParam String topic,

            @Parameter(description = "Tipo de dado esperado", required = true)
            @RequestParam PlcDataType data_type,

            @Parameter(description = "Porta do broker MQTT", required = false)
            @RequestParam(required = false, defaultValue = "1883") Integer port,

            @Parameter(description = "Nível de QoS (0, 1, 2)", required = false)
            @RequestParam(required = false, defaultValue = "1") Integer qos,

            @Parameter(description = "Timeout em segundos para receber mensagem", required = false)
            @RequestParam(required = false, defaultValue = "10") Integer timeoutSeconds) {

        try {
            // Create MQTT protocol instance
            MqttProtocol protocol = new MqttProtocol(
                    broker,
                    port != null ? port : MQTT_PORT,
                    qos != null ? qos : DEFAULT_QOS
            );

            // Connect to MQTT broker
            protocol.openConnection();

            Object value;
            switch (data_type) {
                case BOOLEAN:
                    String boolStr = protocol.subscribeAndWaitForMessage(topic, timeoutSeconds != null ? timeoutSeconds : 10);
                    value = Boolean.parseBoolean(boolStr);
                    break;
                case INT:
                    String intStr = protocol.subscribeAndWaitForMessage(topic, timeoutSeconds != null ? timeoutSeconds : 10);
                    try {
                        value = Integer.parseInt(intStr);
                    } catch (NumberFormatException e) {
                        value = -1;
                    }
                    break;
                case FLOAT:
                    String floatStr = protocol.subscribeAndWaitForMessage(topic, timeoutSeconds != null ? timeoutSeconds : 10);
                    try {
                        value = Float.parseFloat(floatStr);
                    } catch (NumberFormatException e) {
                        value = -1.0f;
                    }
                    break;
                case STRING:
                    value = protocol.subscribeAndWaitForMessage(topic, timeoutSeconds != null ? timeoutSeconds : 10);
                    break;
                default:
                    return ResponseEntity.badRequest().body(
                            new PlcValueDto("Unsupported data type: " + data_type));
            }

            // Construct the response DTO
            PlcValueDto response = new PlcValueDto();
            response.setType(data_type);
            response.setValue(value);

            // Close connection
            protocol.closeConnection();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Create error response
            PlcValueDto errorResponse = new PlcValueDto();
            errorResponse.setValue("Error subscribing to MQTT topic: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

//    @GetMapping("/read-last")
//    @Operation(summary = "Leitura da última mensagem recebida de um tópico (sem nova subscrição)")
//    @ApiResponse(responseCode = "200", description = "Última mensagem obtida com sucesso")
//    public ResponseEntity<PlcValueDto> readLastMessage(
//            @Parameter(description = "IP/hostname do broker MQTT", required = true)
//            @RequestParam String broker,
//
//            @Parameter(description = "Tópico MQTT", required = true)
//            @RequestParam String topic,
//
//            @Parameter(description = "Tipo de dado esperado", required = true)
//            @RequestParam PlcDataType data_type,
//
//            @Parameter(description = "Porta do broker MQTT", required = false)
//            @RequestParam(required = false, defaultValue = "1883") Integer port,
//
//            @Parameter(description = "Nível de QoS (0, 1, 2)", required = false)
//            @RequestParam(required = false, defaultValue = "1") Integer qos) {
//
//        try {
//            // Create MQTT protocol instance
//            MqttProtocol protocol = new MqttProtocol(
//                    broker,
//                    port != null ? port : MQTT_PORT,
//                    qos != null ? qos : DEFAULT_QOS
//            );
//
//            // Connect to MQTT broker
//            protocol.openConnection();
//
//            // Subscribe to topic first to ensure we can receive messages
//            protocol.subscribeTopic(topic);
//
//            // Wait a moment for any retained messages
//            Thread.sleep(1000);
//
//            String lastMessage = protocol.getLastMessage(topic);
//            if (lastMessage == null || lastMessage.isEmpty()) {
//                PlcValueDto errorResponse = new PlcValueDto();
//                errorResponse.setValue("No message found for topic: " + topic);
//                protocol.closeConnection();
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
//            }
//
//            Object value;
//            switch (data_type) {
//                case BOOLEAN:
//                    value = Boolean.parseBoolean(lastMessage);
//                    break;
//                case INT:
//                    try {
//                        value = Integer.parseInt(lastMessage);
//                    } catch (NumberFormatException e) {
//                        value = -1;
//                    }
//                    break;
//                case FLOAT:
//                    try {
//                        value = Float.parseFloat(lastMessage);
//                    } catch (NumberFormatException e) {
//                        value = -1.0f;
//                    }
//                    break;
//                case STRING:
//                    value = lastMessage;
//                    break;
//                default:
//                    return ResponseEntity.badRequest().body(
//                            new PlcValueDto("Unsupported data type: " + data_type));
//            }
//
//            // Construct the response DTO
//            PlcValueDto response = new PlcValueDto();
//            response.setType(data_type);
//            response.setValue(value);
//
//            // Close connection
//            protocol.closeConnection();
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            // Create error response
//            PlcValueDto errorResponse = new PlcValueDto();
//            errorResponse.setValue("Error reading last message: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//        }
//    }

    @PostMapping("/publish")
    @Operation(summary = "Publicação de uma mensagem em um tópico MQTT")
    @ApiResponse(responseCode = "200", description = "Mensagem publicada com sucesso")
    public ResponseEntity<ExampleMessageDto> publishMqtt(
            @Parameter(description = "IP/hostname do broker MQTT", required = true)
            @RequestParam String broker,

            @Parameter(description = "Tópico MQTT", required = true)
            @RequestParam String topic,

            @Parameter(description = "Tipo de dado a ser publicado", required = true)
            @RequestParam PlcDataType data_type,

            @Parameter(description = "Valor a ser publicado", required = true)
            @RequestParam String value,

            @Parameter(description = "Porta do broker MQTT", required = false)
            @RequestParam(required = false, defaultValue = "1883") Integer port,

            @Parameter(description = "Nível de QoS (0, 1, 2)", required = false)
            @RequestParam(required = false, defaultValue = "1") Integer qos,

            @Parameter(description = "Mensagem retida (retained)", required = false)
            @RequestParam(required = false, defaultValue = "false") Boolean retained) {

        try {
            // Create MQTT protocol instance
            MqttProtocol protocol = new MqttProtocol(
                    broker,
                    port != null ? port : MQTT_PORT,
                    qos != null ? qos : DEFAULT_QOS
            );

            // Connect to MQTT broker
            protocol.openConnection();

            // Validate and publish value based on data type
            switch (data_type) {
                case BOOLEAN:
                    boolean boolValue = Boolean.parseBoolean(value);
                    if (retained != null && retained) {
                        protocol.writeDataRetained(topic, String.valueOf(boolValue));
                    } else {
                        protocol.writeData(topic, boolValue);
                    }
                    break;
                case INT:
                    int intValue = Integer.parseInt(value);
                    if (retained != null && retained) {
                        protocol.writeDataRetained(topic, String.valueOf(intValue));
                    } else {
                        protocol.writeData(topic, intValue);
                    }
                    break;
                case FLOAT:
                    float floatValue = Float.parseFloat(value);
                    if (retained != null && retained) {
                        protocol.writeDataRetained(topic, String.valueOf(floatValue));
                    } else {
                        protocol.writeData(topic, floatValue);
                    }
                    break;
                case STRING:
                    if (retained != null && retained) {
                        protocol.writeDataRetained(topic, value);
                    } else {
                        protocol.writeData(topic, value);
                    }
                    break;
                default:
                    return ResponseEntity.badRequest().body(
                            new ExampleMessageDto("Unsupported data type: " + data_type));
            }

            // Close connection
            protocol.closeConnection();

            String retainedMsg = (retained != null && retained) ? " (retained)" : "";
            return ResponseEntity.ok(new ExampleMessageDto(
                    "Message published successfully to topic: " + topic + retainedMsg));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    new ExampleMessageDto("Invalid value format for data type: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ExampleMessageDto("Error publishing to MQTT broker: " + e.getMessage()));
        }
    }

//    @GetMapping("/topics")
//    @Operation(summary = "Lista todas as últimas mensagens recebidas por tópico")
//    @ApiResponse(responseCode = "200", description = "Lista de tópicos e mensagens obtida com sucesso")
//    public ResponseEntity<Map<String, String>> listTopics(
//            @Parameter(description = "IP/hostname do broker MQTT", required = true)
//            @RequestParam String broker,
//
//            @Parameter(description = "Porta do broker MQTT", required = false)
//            @RequestParam(required = false, defaultValue = "1883") Integer port,
//
//            @Parameter(description = "Nível de QoS (0, 1, 2)", required = false)
//            @RequestParam(required = false, defaultValue = "1") Integer qos) {
//
//        try {
//            // Create MQTT protocol instance
//            MqttProtocol protocol = new MqttProtocol(
//                    broker,
//                    port != null ? port : MQTT_PORT,
//                    qos != null ? qos : DEFAULT_QOS
//            );
//
//            // Connect to MQTT broker
//            protocol.openConnection();
//
//            // Get all last messages
//            Map<String, String> messages = protocol.getAllLastMessages();
//
//            // Close connection
//            protocol.closeConnection();
//
//            return ResponseEntity.ok(messages);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Error connecting to MQTT broker: " + e.getMessage()));
//        }
//    }

//    @PostMapping("/subscribe-permanent")
//    @Operation(summary = "Subscrição permanente a um tópico (mantém conexão ativa)")
//    @ApiResponse(responseCode = "200", description = "Subscrição realizada com sucesso")
//    public ResponseEntity<ExampleMessageDto> subscribePermanent(
//            @Parameter(description = "IP/hostname do broker MQTT", required = true)
//            @RequestParam String broker,
//
//            @Parameter(description = "Tópico MQTT", required = true)
//            @RequestParam String topic,
//
//            @Parameter(description = "Porta do broker MQTT", required = false)
//            @RequestParam(required = false, defaultValue = "1883") Integer port,
//
//            @Parameter(description = "Nível de QoS (0, 1, 2)", required = false)
//            @RequestParam(required = false, defaultValue = "1") Integer qos) {
//
//        try {
//            // Create MQTT protocol instance
//            MqttProtocol protocol = new MqttProtocol(
//                    broker,
//                    port != null ? port : MQTT_PORT,
//                    qos != null ? qos : DEFAULT_QOS
//            );
//
//            // Connect to MQTT broker
//            protocol.openConnection();
//
//            // Subscribe to topic
//            protocol.subscribeTopic(topic);
//
//            // Note: In a real application, you'd want to manage these connections
//            // This is just for demonstration purposes
//            return ResponseEntity.ok(new ExampleMessageDto(
//                    "Permanently subscribed to topic: " + topic +
//                            ". Use /mqtt/read-last to get messages."));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ExampleMessageDto("Error subscribing to MQTT topic: " + e.getMessage()));
//        }
//    }
}