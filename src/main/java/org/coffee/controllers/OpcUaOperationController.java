package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.coffee.domain.abstracts.AbstractProtocol;
import org.coffee.domain.models.OpcUaProtocol;
import org.coffee.domain.models.database.DeviceTable;
import org.coffee.domain.models.database.PlcTable;
import org.coffee.domain.models.database.VariableOpcUaTable;
import org.coffee.repository.DeviceRepository;
import org.coffee.repository.VariableOpcUaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/operations/opcua")
@Tag(name = "OPC-UA Operations", description = "Endpoints para leitura e escrita de variaveis OPC-UA configuradas")
public class OpcUaOperationController {

    private final DeviceRepository deviceRepository;
    private final VariableOpcUaRepository variableOpcUaRepository;

    @Autowired
    public OpcUaOperationController(DeviceRepository deviceRepository,
                                    VariableOpcUaRepository variableOpcUaRepository) {
        this.deviceRepository = deviceRepository;
        this.variableOpcUaRepository = variableOpcUaRepository;
    }

    // Le o valor de uma variavel especifica
    @GetMapping("/read/device/{deviceId}/variable/{variableId}")
    @Operation(summary = "Le o valor de uma variavel OPC-UA especifica")
    @ApiResponse(responseCode = "200", description = "Variavel lida com sucesso")
    public ResponseEntity<?> readVariable(
            @Parameter(description = "ID do dispositivo", required = true)
            @PathVariable Long deviceId,

            @Parameter(description = "ID da variavel", required = true)
            @PathVariable Long variableId) {

        try {
            // Busca o dispositivo
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

            // Busca a variavel
            VariableOpcUaTable variable = variableOpcUaRepository.findById(variableId)
                    .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

            // Valida se a variavel pertence ao dispositivo
            if (!variable.getDevice().getId().equals(deviceId)) {
                throw new RuntimeException("Variavel nao pertence ao dispositivo especificado");
            }

            // Obtem os dados necessarios do banco
            PlcTable plc = device.getPlc();
            String ip = plc.getIp();
            int port = variable.getPort();
            int namespaceIndex = variable.getNamespaceIndex();
            String nodeIdPrefix = variable.getNodeIdPrefix() != null ? variable.getNodeIdPrefix() : "";
            String nodeId = variable.getNodeId();
            String dataType = variable.getDataType();

            // Cria protocolo OPC-UA
            OpcUaProtocol protocol = new OpcUaProtocol(ip, port, namespaceIndex, nodeIdPrefix);

            // Conecta ao servidor OPC-UA
            protocol.openConnection();

            // Le o valor baseado no dataType
            Object value;

            if (dataType.equalsIgnoreCase("BOOLEAN") || dataType.equalsIgnoreCase("BOOL")) {
                value = protocol.readDataBoolean(nodeId);
            } else if (dataType.equalsIgnoreCase("INT") || dataType.equalsIgnoreCase("INTEGER")) {
                value = protocol.readDataInt(nodeId);
            } else if (dataType.equalsIgnoreCase("FLOAT") || dataType.equalsIgnoreCase("REAL")) {
                value = protocol.readDataFloat(nodeId);
            } else if (dataType.equalsIgnoreCase("STRING")) {
                value = protocol.readDataString(nodeId);
            } else {
                // Default: trata como INT
                value = protocol.readDataInt(nodeId);
            }

            // Fecha conexao
            protocol.closeConnection();

            // Monta resposta
            Map<String, Object> response = new HashMap<>();
            response.put("deviceId", deviceId);
            response.put("deviceName", device.getName());
            response.put("variableId", variableId);
            response.put("variableName", variable.getName());
            response.put("value", value);
            response.put("unit", variable.getUnit());
            response.put("dataType", variable.getDataType());
            response.put("nodeId", nodeId);
            response.put("namespaceIndex", namespaceIndex);
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao ler variavel: " + e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Escreve um valor em uma variavel especifica
    @PostMapping("/write/device/{deviceId}/variable/{variableId}")
    @Operation(summary = "Escreve um valor em uma variavel OPC-UA especifica")
    @ApiResponse(responseCode = "200", description = "Variavel escrita com sucesso")
    public ResponseEntity<?> writeVariable(
            @Parameter(description = "ID do dispositivo", required = true)
            @PathVariable Long deviceId,

            @Parameter(description = "ID da variavel", required = true)
            @PathVariable Long variableId,

            @Parameter(description = "Valor a ser escrito", required = true)
            @RequestParam String value) {

        try {
            // Busca o dispositivo
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

            // Busca a variavel
            VariableOpcUaTable variable = variableOpcUaRepository.findById(variableId)
                    .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

            // Valida se a variavel pertence ao dispositivo
            if (!variable.getDevice().getId().equals(deviceId)) {
                throw new RuntimeException("Variavel nao pertence ao dispositivo especificado");
            }

            // Obtem os dados necessarios do banco
            PlcTable plc = device.getPlc();
            String ip = plc.getIp();
            int port = variable.getPort();
            int namespaceIndex = variable.getNamespaceIndex();
            String nodeIdPrefix = variable.getNodeIdPrefix() != null ? variable.getNodeIdPrefix() : "";
            String nodeId = variable.getNodeId();
            String dataType = variable.getDataType();

            // Cria protocolo OPC-UA
            OpcUaProtocol protocol = new OpcUaProtocol(ip, port, namespaceIndex, nodeIdPrefix);

            // Conecta ao servidor OPC-UA
            protocol.openConnection();

            // Escreve o valor baseado no dataType
            if (dataType.equalsIgnoreCase("BOOLEAN") || dataType.equalsIgnoreCase("BOOL")) {
                boolean boolValue = Boolean.parseBoolean(value);
                protocol.writeData(nodeId, boolValue);
            } else if (dataType.equalsIgnoreCase("INT") || dataType.equalsIgnoreCase("INTEGER")) {
                int intValue = Integer.parseInt(value);
                protocol.writeData(nodeId, intValue);
            } else if (dataType.equalsIgnoreCase("FLOAT") || dataType.equalsIgnoreCase("REAL")) {
                float floatValue = Float.parseFloat(value);
                protocol.writeData(nodeId, floatValue);
            } else if (dataType.equalsIgnoreCase("STRING")) {
                protocol.writeData(nodeId, value);
            } else {
                // Default: trata como INT
                int intValue = Integer.parseInt(value);
                protocol.writeData(nodeId, intValue);
            }

            // Fecha conexao
            protocol.closeConnection();

            // Monta resposta
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Valor escrito com sucesso");
            response.put("deviceId", deviceId);
            response.put("deviceName", device.getName());
            response.put("variableId", variableId);
            response.put("variableName", variable.getName());
            response.put("writtenValue", value);
            response.put("nodeId", nodeId);
            response.put("namespaceIndex", namespaceIndex);
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Formato de valor invalido: " + e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao escrever variavel: " + e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Le todas as variaveis de um dispositivo
    @GetMapping("/read/device/{deviceId}/all")
    @Operation(summary = "Le todas as variaveis OPC-UA de um dispositivo")
    @ApiResponse(responseCode = "200", description = "Variaveis lidas com sucesso")
    public ResponseEntity<?> readAllVariables(
            @Parameter(description = "ID do dispositivo", required = true)
            @PathVariable Long deviceId) {

        try {
            // Busca o dispositivo
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

            // Busca todas as variaveis do dispositivo
            List<VariableOpcUaTable> variables = variableOpcUaRepository.findByDeviceId(deviceId);

            if (variables.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Nenhuma variavel encontrada para este dispositivo");
                response.put("deviceId", deviceId.toString());
                response.put("deviceName", device.getName());
                return ResponseEntity.ok(response);
            }

            // Obtem dados do PLC
            PlcTable plc = device.getPlc();
            String ip = plc.getIp();

            // Le todas as variaveis
            List<Map<String, Object>> results = variables.stream().map(variable -> {
                Map<String, Object> result = new HashMap<>();
                result.put("variableId", variable.getId());
                result.put("variableName", variable.getName());
                result.put("unit", variable.getUnit());
                result.put("dataType", variable.getDataType());
                result.put("nodeId", variable.getNodeId());
                result.put("namespaceIndex", variable.getNamespaceIndex());

                try {
                    String dataType = variable.getDataType();
                    String nodeId = variable.getNodeId();
                    int port = variable.getPort();
                    int namespaceIndex = variable.getNamespaceIndex();
                    String nodeIdPrefix = variable.getNodeIdPrefix() != null ? variable.getNodeIdPrefix() : "";

                    // Cria protocolo OPC-UA
                    OpcUaProtocol protocol = new OpcUaProtocol(ip, port, namespaceIndex, nodeIdPrefix);

                    // Conecta ao servidor OPC-UA
                    protocol.openConnection();

                    // Le o valor baseado no dataType
                    Object value;

                    if (dataType.equalsIgnoreCase("BOOLEAN") || dataType.equalsIgnoreCase("BOOL")) {
                        value = protocol.readDataBoolean(nodeId);
                    } else if (dataType.equalsIgnoreCase("INT") || dataType.equalsIgnoreCase("INTEGER")) {
                        value = protocol.readDataInt(nodeId);
                    } else if (dataType.equalsIgnoreCase("FLOAT") || dataType.equalsIgnoreCase("REAL")) {
                        value = protocol.readDataFloat(nodeId);
                    } else if (dataType.equalsIgnoreCase("STRING")) {
                        value = protocol.readDataString(nodeId);
                    } else {
                        value = protocol.readDataInt(nodeId);
                    }

                    // Fecha conexao
                    protocol.closeConnection();

                    result.put("value", value);
                    result.put("status", "success");

                } catch (Exception e) {
                    result.put("value", null);
                    result.put("status", "error");
                    result.put("error", e.getMessage());
                }

                return result;
            }).collect(Collectors.toList());

            // Monta resposta final
            Map<String, Object> response = new HashMap<>();
            response.put("deviceId", deviceId);
            response.put("deviceName", device.getName());
            response.put("totalVariables", variables.size());
            response.put("variables", results);
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao ler variaveis: " + e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}