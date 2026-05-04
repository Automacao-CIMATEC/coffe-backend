package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.coffee.domain.abstracts.AbstractProtocol;
import org.coffee.domain.models.ModbusProtocol;
import org.coffee.domain.models.database.DeviceTable;
import org.coffee.domain.models.database.PlcTable;
import org.coffee.domain.models.database.VariableModbusTable;
import org.coffee.repository.DeviceRepository;
import org.coffee.repository.VariableModbusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/operations/modbus")
@Tag(name = "Modbus Operations", description = "Endpoints para leitura e escrita de variaveis Modbus configuradas")
public class ModbusOperationController {

    private final DeviceRepository deviceRepository;
    private final VariableModbusRepository variableModbusRepository;

    @Autowired
    public ModbusOperationController(DeviceRepository deviceRepository,
                                     VariableModbusRepository variableModbusRepository) {
        this.deviceRepository = deviceRepository;
        this.variableModbusRepository = variableModbusRepository;
    }

    // Le o valor de uma variavel especifica
    @GetMapping("/read/device/{deviceId}/variable/{variableId}")
    @Operation(summary = "Le o valor de uma variavel Modbus especifica")
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
            VariableModbusTable variable = variableModbusRepository.findById(variableId)
                    .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

            // Valida se a variavel pertence ao dispositivo
            if (!variable.getDevice().getId().equals(deviceId)) {
                throw new RuntimeException("Variavel nao pertence ao dispositivo especificado");
            }

            // Obtem os dados necessarios do banco
            PlcTable plc = device.getPlc();
            String ip = plc.getIp();
            int port = variable.getPort();  // Port agora vem da variavel
            int unitId = variable.getUnitId();
            String dataType = variable.getDataType();
            int address = variable.getAddress();

            // Cria protocolo Modbus (igual ao ModbusController)
            AbstractProtocol protocol = new ModbusProtocol(ip, port, unitId);

            // Conecta ao PLC
            protocol.openConnection();

            // Le o valor baseado no dataType (EXATAMENTE como no ModbusController)
            Object value;

            if (dataType.equalsIgnoreCase("BOOLEAN") || dataType.equalsIgnoreCase("BOOL")) {
                value = protocol.readDataBoolean(address);
            } else if (dataType.equalsIgnoreCase("INT") || dataType.equalsIgnoreCase("INTEGER")) {
                value = protocol.readDataInt(address);
            } else if (dataType.equalsIgnoreCase("FLOAT") || dataType.equalsIgnoreCase("REAL")) {
                value = protocol.readDataFloat(address);
            } else if (dataType.equalsIgnoreCase("STRING")) {
                // Para string, usa tamanho padrao de 10 registros
                value = protocol.readDataString(address, 10);
            } else {
                // Default: trata como INT
                value = protocol.readDataInt(address);
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
            response.put("address", address);
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
    @Operation(summary = "Escreve um valor em uma variavel Modbus especifica")
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
            VariableModbusTable variable = variableModbusRepository.findById(variableId)
                    .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

            // Valida se a variavel pertence ao dispositivo
            if (!variable.getDevice().getId().equals(deviceId)) {
                throw new RuntimeException("Variavel nao pertence ao dispositivo especificado");
            }

            // Obtem os dados necessarios do banco
            PlcTable plc = device.getPlc();
            String ip = plc.getIp();
            int port = variable.getPort();
            int unitId = variable.getUnitId();
            String dataType = variable.getDataType();
            int address = variable.getAddress();

            // Cria protocolo Modbus (igual ao ModbusController)
            AbstractProtocol protocol = new ModbusProtocol(ip, port, unitId);

            // Conecta ao PLC
            protocol.openConnection();

            // Escreve o valor baseado no dataType (EXATAMENTE como no ModbusController)
            if (dataType.equalsIgnoreCase("BOOLEAN") || dataType.equalsIgnoreCase("BOOL")) {
                boolean boolValue = Boolean.parseBoolean(value);
                protocol.writeData(address, boolValue);
            } else if (dataType.equalsIgnoreCase("INT") || dataType.equalsIgnoreCase("INTEGER")) {
                int intValue = Integer.parseInt(value);
                protocol.writeData(address, intValue);
            } else if (dataType.equalsIgnoreCase("FLOAT") || dataType.equalsIgnoreCase("REAL")) {
                float floatValue = Float.parseFloat(value);
                protocol.writeData(address, floatValue);
            } else if (dataType.equalsIgnoreCase("STRING")) {
                protocol.writeData(address, value);
            } else {
                // Default: trata como INT
                int intValue = Integer.parseInt(value);
                protocol.writeData(address, intValue);
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
            response.put("address", address);
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

    /// Le todas as variaveis de um dispositivo
    @GetMapping("/read/device/{deviceId}/all")
    @Operation(summary = "Le todas as variaveis Modbus de um dispositivo")
    @ApiResponse(responseCode = "200", description = "Variaveis lidas com sucesso")
    public ResponseEntity<?> readAllVariables(
            @Parameter(description = "ID do dispositivo", required = true)
            @PathVariable Long deviceId) {

        try {
            // Busca o dispositivo
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

            // Busca todas as variaveis do dispositivo
            List<VariableModbusTable> variables = variableModbusRepository.findByDeviceId(deviceId);

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
                result.put("address", variable.getAddress());

                try {
                    String dataType = variable.getDataType();
                    int address = variable.getAddress();
                    int unitId = variable.getUnitId();
                    int port = variable.getPort(); // Agora definido dentro da lambda onde 'variable' existe

                    // Cria protocolo Modbus
                    AbstractProtocol protocol = new ModbusProtocol(ip, port, unitId);

                    // Conecta ao PLC
                    protocol.openConnection();

                    // Le o valor baseado no dataType
                    Object value;

                    if (dataType.equalsIgnoreCase("BOOLEAN") || dataType.equalsIgnoreCase("BOOL")) {
                        value = protocol.readDataBoolean(address);
                    } else if (dataType.equalsIgnoreCase("INT") || dataType.equalsIgnoreCase("INTEGER")) {
                        value = protocol.readDataInt(address);
                    } else if (dataType.equalsIgnoreCase("FLOAT") || dataType.equalsIgnoreCase("REAL")) {
                        value = protocol.readDataFloat(address);
                    } else if (dataType.equalsIgnoreCase("STRING")) {
                        value = protocol.readDataString(address, 10);
                    } else {
                        value = protocol.readDataInt(address);
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