package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.coffee.domain.models.MqttProtocol;
import org.coffee.domain.models.database.DeviceTable;
import org.coffee.domain.models.database.VariableMqttTable;
import org.coffee.repository.DeviceRepository;
import org.coffee.repository.VariableMqttRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/operations/mqtt")
@Tag(name = "MQTT Operations", description = "Endpoints para leitura e escrita de variaveis MQTT configuradas")
public class MqttOperationController {

    private final DeviceRepository deviceRepository;
    private final VariableMqttRepository variableMqttRepository;

    @Autowired
    public MqttOperationController(DeviceRepository deviceRepository,
                                   VariableMqttRepository variableMqttRepository) {
        this.deviceRepository = deviceRepository;
        this.variableMqttRepository = variableMqttRepository;
    }

    // Le o valor de uma variavel especifica
    @GetMapping("/read/device/{deviceId}/variable/{variableId}")
    @Operation(summary = "Le o valor de uma variavel MQTT especifica")
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
            VariableMqttTable variable = variableMqttRepository.findById(variableId)
                    .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

            // Valida se a variavel pertence ao dispositivo
            if (!variable.getDevice().getId().equals(deviceId)) {
                throw new RuntimeException("Variavel nao pertence ao dispositivo especificado");
            }

            // Obtem os dados necessarios do banco
            String brokerIp = variable.getBrokerIp();
            int port = variable.getPort();
            int qos = variable.getQos();
            String clientId = variable.getClientId();
            String topic = variable.getTopic();
            String dataType = variable.getDataType();

            // Cria protocolo MQTT
            MqttProtocol protocol = new MqttProtocol(brokerIp, port, clientId, qos);

            // Conecta ao broker MQTT
            protocol.openConnection();

            // Subscreve ao topico e aguarda mensagem
            String rawValue = protocol.subscribeAndWaitForMessage(topic, 1);

            // Fecha conexao (desinscreve automaticamente)
            protocol.closeConnection();

            // Converte o valor baseado no dataType
            Object value;
            if (rawValue == null || rawValue.isEmpty()) {
                value = null;
            } else if (dataType.equalsIgnoreCase("BOOLEAN") || dataType.equalsIgnoreCase("BOOL")) {
                value = Boolean.parseBoolean(rawValue);
            } else if (dataType.equalsIgnoreCase("INT") || dataType.equalsIgnoreCase("INTEGER")) {
                try {
                    value = Integer.parseInt(rawValue);
                } catch (NumberFormatException e) {
                    value = rawValue; // Retorna como string se nao for possivel converter
                }
            } else if (dataType.equalsIgnoreCase("FLOAT") || dataType.equalsIgnoreCase("REAL")) {
                try {
                    value = Float.parseFloat(rawValue);
                } catch (NumberFormatException e) {
                    value = rawValue; // Retorna como string se nao for possivel converter
                }
            } else {
                // Default: trata como STRING
                value = rawValue;
            }

            // Monta resposta
            Map<String, Object> response = new HashMap<>();
            response.put("deviceId", deviceId);
            response.put("deviceName", device.getName());
            response.put("variableId", variableId);
            response.put("variableName", variable.getName());
            response.put("value", value);
            response.put("unit", variable.getUnit());
            response.put("dataType", variable.getDataType());
            response.put("brokerIp", brokerIp);
            response.put("topic", topic);
            response.put("qos", qos);
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
    @Operation(summary = "Escreve um valor em uma variavel MQTT especifica")
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
            VariableMqttTable variable = variableMqttRepository.findById(variableId)
                    .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

            // Valida se a variavel pertence ao dispositivo
            if (!variable.getDevice().getId().equals(deviceId)) {
                throw new RuntimeException("Variavel nao pertence ao dispositivo especificado");
            }

            // Obtem os dados necessarios do banco
            String brokerIp = variable.getBrokerIp();
            int port = variable.getPort();
            int qos = variable.getQos();
            String clientId = variable.getClientId();
            String topic = variable.getTopic();
            String dataType = variable.getDataType();

            // Cria protocolo MQTT
            MqttProtocol protocol = new MqttProtocol(brokerIp, port, clientId, qos);

            // Conecta ao broker MQTT
            protocol.openConnection();

            // Valida e converte o valor baseado no dataType
            String valueToPublish;
            if (dataType.equalsIgnoreCase("BOOLEAN") || dataType.equalsIgnoreCase("BOOL")) {
                // Valida se e um boolean valido
                boolean boolValue = Boolean.parseBoolean(value);
                valueToPublish = String.valueOf(boolValue);
            } else if (dataType.equalsIgnoreCase("INT") || dataType.equalsIgnoreCase("INTEGER")) {
                // Valida se e um inteiro valido
                int intValue = Integer.parseInt(value);
                valueToPublish = String.valueOf(intValue);
            } else if (dataType.equalsIgnoreCase("FLOAT") || dataType.equalsIgnoreCase("REAL")) {
                // Valida se e um float valido
                float floatValue = Float.parseFloat(value);
                valueToPublish = String.valueOf(floatValue);
            } else {
                // Default: trata como STRING
                valueToPublish = value;
            }

            // Publica a mensagem no topico com retained flag
            boolean retained = variable.getRetained() != null ? variable.getRetained() : true;
            if (retained) {
                protocol.writeDataRetained(topic, valueToPublish);
            } else {
                protocol.writeData(topic, valueToPublish);
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
            response.put("writtenValue", valueToPublish);
            response.put("brokerIp", brokerIp);
            response.put("topic", topic);
            response.put("qos", qos);
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
    @Operation(summary = "Le todas as variaveis MQTT de um dispositivo")
    @ApiResponse(responseCode = "200", description = "Variaveis lidas com sucesso")
    public ResponseEntity<?> readAllVariables(
            @Parameter(description = "ID do dispositivo", required = true)
            @PathVariable Long deviceId) {

        try {
            // Busca o dispositivo
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

            // Busca todas as variaveis do dispositivo
            List<VariableMqttTable> variables = variableMqttRepository.findByDeviceId(deviceId);

            if (variables.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Nenhuma variavel encontrada para este dispositivo");
                response.put("deviceId", deviceId.toString());
                response.put("deviceName", device.getName());
                return ResponseEntity.ok(response);
            }

            // Le todas as variaveis
            List<Map<String, Object>> results = variables.stream().map(variable -> {
                Map<String, Object> result = new HashMap<>();
                result.put("variableId", variable.getId());
                result.put("variableName", variable.getName());
                result.put("unit", variable.getUnit());
                result.put("dataType", variable.getDataType());
                result.put("brokerIp", variable.getBrokerIp());
                result.put("topic", variable.getTopic());
                result.put("qos", variable.getQos());

                try {
                    String dataType = variable.getDataType();
                    String topic = variable.getTopic();
                    String brokerIp = variable.getBrokerIp();
                    int port = variable.getPort();
                    int qos = variable.getQos();
                    String clientId = variable.getClientId();

                    // Cria protocolo MQTT
                    MqttProtocol protocol = new MqttProtocol(brokerIp, port, clientId, qos);

                    // Conecta ao broker MQTT
                    protocol.openConnection();

                    // Subscreve ao topico e aguarda mensagem
                    String rawValue = protocol.subscribeAndWaitForMessage(topic, 1);

                    // Fecha conexao (desinscreve automaticamente)
                    protocol.closeConnection();

                    // Converte o valor baseado no dataType
                    Object value;
                    if (rawValue == null || rawValue.isEmpty()) {
                        value = null;
                    } else if (dataType.equalsIgnoreCase("BOOLEAN") || dataType.equalsIgnoreCase("BOOL")) {
                        value = Boolean.parseBoolean(rawValue);
                    } else if (dataType.equalsIgnoreCase("INT") || dataType.equalsIgnoreCase("INTEGER")) {
                        try {
                            value = Integer.parseInt(rawValue);
                        } catch (NumberFormatException e) {
                            value = rawValue;
                        }
                    } else if (dataType.equalsIgnoreCase("FLOAT") || dataType.equalsIgnoreCase("REAL")) {
                        try {
                            value = Float.parseFloat(rawValue);
                        } catch (NumberFormatException e) {
                            value = rawValue;
                        }
                    } else {
                        value = rawValue;
                    }

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