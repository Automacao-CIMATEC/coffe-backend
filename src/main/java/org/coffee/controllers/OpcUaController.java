package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.coffee.examples.api.ExampleMessageDto;
import org.coffee.domain.dtos.PlcValueDto;
import org.coffee.domain.enums.PlcDataType;
import org.coffee.domain.abstracts.AbstractProtocol;
import org.coffee.domain.models.OpcUaProtocol;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/opc-ua")
@Tag(name = "OPC-UA", description = "Endpoints para comunicação através do protocolo OPC-UA")
public class OpcUaController {

    // Default OPC-UA port
    private static final int OPCUA_PORT = 4840;
    // Default namespace index
    private static final int DEFAULT_NAMESPACE_INDEX = 4;

    @GetMapping("/read")
    @Operation(summary = "Leitura de uma variável através do protocolo OPC-UA")
    @ApiResponse(responseCode = "200", description = "Variável lida com sucesso")
    public ResponseEntity<PlcValueDto> readOpcUa(
            @Parameter(description = "IP do servidor OPC-UA", required = true)
            @RequestParam String ip,

            @Parameter(description = "Node ID da variável", required = true)
            @RequestParam String nodeId,

            @Parameter(description = "Tipo de dado a ser lido", required = true)
            @RequestParam PlcDataType data_type,

            @Parameter(description = "Porta do servidor OPC-UA", required = false)
            @RequestParam(required = false, defaultValue = "4840") Integer port,

            @Parameter(description = "Namespace index", required = false)
            @RequestParam(required = false, defaultValue = "4") Integer namespaceIndex,

            @Parameter(description = "Prefixo do Node ID", required = false)
            @RequestParam(required = false, defaultValue = "") String nodeIdPrefix) {

        try {
            // Create OPC-UA protocol instance
            OpcUaProtocol protocol = new OpcUaProtocol(
                    ip,
                    port != null ? port : OPCUA_PORT,
                    namespaceIndex != null ? namespaceIndex : DEFAULT_NAMESPACE_INDEX,
                    nodeIdPrefix
            );

            // Connect to OPC-UA server
            protocol.openConnection();

            Object value;
            switch (data_type) {
                case BOOLEAN:
                    value = protocol.readDataBoolean(nodeId);
                    break;
                case INT:
                    value = protocol.readDataInt(nodeId);
                    break;
                case FLOAT:
                    value = protocol.readDataFloat(nodeId);
                    break;
                case STRING:
                    value = protocol.readDataString(nodeId);
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
            errorResponse.setValue("Error reading from OPC-UA server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

//    @GetMapping("/read-generic")
//    @Operation(summary = "Leitura genérica de uma variável OPC-UA (retorna o tipo detectado automaticamente)")
//    @ApiResponse(responseCode = "200", description = "Variável lida com sucesso")
//    public ResponseEntity<PlcValueDto> readOpcUaGeneric(
//            @Parameter(description = "IP do servidor OPC-UA", required = true)
//            @RequestParam String ip,
//
//            @Parameter(description = "Node ID da variável", required = true)
//            @RequestParam String nodeId,
//
//            @Parameter(description = "Porta do servidor OPC-UA", required = false)
//            @RequestParam(required = false, defaultValue = "4840") Integer port,
//
//            @Parameter(description = "Namespace index", required = false)
//            @RequestParam(required = false, defaultValue = "4") Integer namespaceIndex,
//
//            @Parameter(description = "Prefixo do Node ID", required = false)
//            @RequestParam(required = false, defaultValue = "") String nodeIdPrefix) {
//
//        try {
//            // Create OPC-UA protocol instance
//            OpcUaProtocol protocol = new OpcUaProtocol(
//                    ip,
//                    port != null ? port : OPCUA_PORT,
//                    namespaceIndex != null ? namespaceIndex : DEFAULT_NAMESPACE_INDEX,
//                    nodeIdPrefix
//            );
//
//            // Connect to OPC-UA server
//            protocol.openConnection();
//
//            // Read generic value
//            Object value = protocol.readData(nodeId);
//
//            // Determine data type based on value
//            PlcDataType dataType;
//            if (value instanceof Boolean) {
//                dataType = PlcDataType.BOOLEAN;
//            } else if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
//                dataType = PlcDataType.INT;
//            } else if (value instanceof Float || value instanceof Double) {
//                dataType = PlcDataType.FLOAT;
//            } else {
//                dataType = PlcDataType.STRING;
//            }
//
//            // Construct the response DTO
//            PlcValueDto response = new PlcValueDto();
//            response.setType(dataType);
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
//            errorResponse.setValue("Error reading from OPC-UA server: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//        }
//    }

    @PostMapping("/write")
    @Operation(summary = "Escrita de uma variável através do protocolo OPC-UA")
    @ApiResponse(responseCode = "200", description = "Variável escrita com sucesso")
    public ResponseEntity<ExampleMessageDto> writeOpcUa(
            @Parameter(description = "IP do servidor OPC-UA", required = true)
            @RequestParam String ip,

            @Parameter(description = "Node ID da variável", required = true)
            @RequestParam String nodeId,

            @Parameter(description = "Tipo de dado a ser escrito", required = true)
            @RequestParam PlcDataType data_type,

            @Parameter(description = "Valor a ser escrito", required = true)
            @RequestParam String value,

            @Parameter(description = "Porta do servidor OPC-UA", required = false)
            @RequestParam(required = false, defaultValue = "4840") Integer port,

            @Parameter(description = "Namespace index", required = false)
            @RequestParam(required = false, defaultValue = "4") Integer namespaceIndex,

            @Parameter(description = "Prefixo do Node ID", required = false)
            @RequestParam(required = false, defaultValue = "") String nodeIdPrefix) {

        try {
            // Create OPC-UA protocol instance
            OpcUaProtocol protocol = new OpcUaProtocol(
                    ip,
                    port != null ? port : OPCUA_PORT,
                    namespaceIndex != null ? namespaceIndex : DEFAULT_NAMESPACE_INDEX,
                    nodeIdPrefix
            );

            // Connect to OPC-UA server
            protocol.openConnection();

            // Write value based on data type
            switch (data_type) {
                case BOOLEAN:
                    boolean boolValue = Boolean.parseBoolean(value);
                    protocol.writeData(nodeId, boolValue);
                    break;
                case INT:
                    int intValue = Integer.parseInt(value);
                    protocol.writeData(nodeId, intValue);
                    break;
                case FLOAT:
                    float floatValue = Float.parseFloat(value);
                    protocol.writeData(nodeId, floatValue);
                    break;
                case STRING:
                    protocol.writeData(nodeId, value);
                    break;
                default:
                    return ResponseEntity.badRequest().body(
                            new ExampleMessageDto("Unsupported data type: " + data_type));
            }

            // Close connection
            protocol.closeConnection();

            return ResponseEntity.ok(new ExampleMessageDto("Value written successfully to node: " + nodeId));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    new ExampleMessageDto("Invalid value format for data type: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ExampleMessageDto("Error writing to OPC-UA server: " + e.getMessage()));
        }
    }

//    @PostMapping("/write-generic")
//    @Operation(summary = "Escrita genérica de uma variável OPC-UA (detecta tipo automaticamente)")
//    @ApiResponse(responseCode = "200", description = "Variável escrita com sucesso")
//    public ResponseEntity<ExampleMessageDto> writeOpcUaGeneric(
//            @Parameter(description = "IP do servidor OPC-UA", required = true)
//            @RequestParam String ip,
//
//            @Parameter(description = "Node ID da variável", required = true)
//            @RequestParam String nodeId,
//
//            @Parameter(description = "Valor a ser escrito", required = true)
//            @RequestParam String value,
//
//            @Parameter(description = "Porta do servidor OPC-UA", required = false)
//            @RequestParam(required = false, defaultValue = "4840") Integer port,
//
//            @Parameter(description = "Namespace index", required = false)
//            @RequestParam(required = false, defaultValue = "4") Integer namespaceIndex,
//
//            @Parameter(description = "Prefixo do Node ID", required = false)
//            @RequestParam(required = false, defaultValue = "") String nodeIdPrefix) {
//
//        try {
//            // Create OPC-UA protocol instance
//            OpcUaProtocol protocol = new OpcUaProtocol(
//                    ip,
//                    port != null ? port : OPCUA_PORT,
//                    namespaceIndex != null ? namespaceIndex : DEFAULT_NAMESPACE_INDEX,
//                    nodeIdPrefix
//            );
//
//            // Connect to OPC-UA server
//            protocol.openConnection();
//
//            // Try to detect and convert the value type
//            Object convertedValue;
//            try {
//                // Try boolean first
//                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
//                    convertedValue = Boolean.parseBoolean(value);
//                }
//                // Try integer
//                else if (value.matches("-?\\d+")) {
//                    convertedValue = Integer.parseInt(value);
//                }
//                // Try float
//                else if (value.matches("-?\\d*\\.\\d+")) {
//                    convertedValue = Float.parseFloat(value);
//                }
//                // Default to string
//                else {
//                    convertedValue = value;
//                }
//            } catch (NumberFormatException e) {
//                convertedValue = value; // Fall back to string
//            }
//
//            // Write the converted value
//            protocol.writeData(nodeId, convertedValue);
//
//            // Close connection
//            protocol.closeConnection();
//
//            return ResponseEntity.ok(new ExampleMessageDto(
//                    "Value written successfully to node: " + nodeId +
//                            " (detected type: " + convertedValue.getClass().getSimpleName() + ")"));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ExampleMessageDto("Error writing to OPC-UA server: " + e.getMessage()));
//        }
//    }
}