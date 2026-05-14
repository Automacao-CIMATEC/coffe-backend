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

    /**
     * Seleciona e executa a leitura Modbus correta com base no registerType e dataType da variavel.
     *
     * Logica de decisao:
     *   - registerType == "COIL" -> ReadCoils (funcao Modbus 01), retorna booleano
     *   - registerType == "INPUT_REGISTER" ou "HOLDING_REGISTER" -> ReadRegisters,
     *     com dataType determinando a interpretacao dos bytes (INT, FLOAT ou STRING)
     *
     * O campo dataType (herdado de VariableTable) NAO deve ser usado para selecionar
     * a funcao Modbus, pois ele descreve o tipo logico do dado, nao o tipo de registrador.
     * O campo registerType (proprio de VariableModbusTable) e o correto para essa decisao.
     */
    private Object readValue(AbstractProtocol protocol, VariableModbusTable variable) throws Exception {
        // Tipo de registrador Modbus: COIL, INPUT_REGISTER, HOLDING_REGISTER
        String registerType = variable.getRegisterType();

        // Tipo logico do dado: INT, FLOAT, STRING (usado apenas para registradores)
        String dataType = variable.getDataType();

        int address = variable.getAddress();

        if (registerType.equalsIgnoreCase("COIL")) {
            // Funcao Modbus 01 - Read Coils: retorna valor booleano
            return protocol.readDataBoolean(address);

        } else if (registerType.equalsIgnoreCase("INPUT_REGISTER")
                || registerType.equalsIgnoreCase("HOLDING_REGISTER")) {

            // Funcoes Modbus 03/04 - Read Registers: interpretacao depende do dataType
            if (dataType.equalsIgnoreCase("FLOAT") || dataType.equalsIgnoreCase("REAL")) {
                return protocol.readDataFloat(address);
            } else if (dataType.equalsIgnoreCase("STRING")) {
                // Tamanho padrao de 10 registradores para strings sem comprimento definido
                return protocol.readDataString(address, 10);
            } else {
                // Padrao: trata como INT (cobre INT, INTEGER e tipos desconhecidos)
                return protocol.readDataInt(address);
            }

        } else {
            // Tipo de registrador desconhecido: registra aviso e tenta como INT por seguranca
            System.err.println("Tipo de registrador desconhecido: " + registerType
                    + " para variavel id=" + variable.getId() + ". Tentando leitura como INT.");
            return protocol.readDataInt(address);
        }
    }

    /**
     * Seleciona e executa a escrita Modbus correta com base no registerType e dataType da variavel.
     *
     * Mesma logica de decisao do metodo readValue:
     *   - registerType == "COIL" -> WriteSingleCoil (funcao Modbus 05)
     *   - registerType == "HOLDING_REGISTER" -> WriteRegister/WriteMultipleRegisters,
     *     com dataType determinando a conversao do valor recebido como String
     *
     * Nota: INPUT_REGISTER e somente leitura no protocolo Modbus padrao.
     */
    private void writeValue(AbstractProtocol protocol, VariableModbusTable variable, String value) throws Exception {
        // Tipo de registrador Modbus: COIL, INPUT_REGISTER, HOLDING_REGISTER
        String registerType = variable.getRegisterType();

        // Tipo logico do dado: INT, FLOAT, STRING (usado apenas para registradores)
        String dataType = variable.getDataType();

        int address = variable.getAddress();

        if (registerType.equalsIgnoreCase("COIL")) {
            // Funcao Modbus 05 - Write Single Coil
            boolean boolValue = Boolean.parseBoolean(value);
            protocol.writeData(address, boolValue);

        } else if (registerType.equalsIgnoreCase("HOLDING_REGISTER")) {
            // Funcoes Modbus 06/16 - Write Register(s): conversao depende do dataType
            if (dataType.equalsIgnoreCase("FLOAT") || dataType.equalsIgnoreCase("REAL")) {
                float floatValue = Float.parseFloat(value);
                protocol.writeData(address, floatValue);
            } else if (dataType.equalsIgnoreCase("STRING")) {
                protocol.writeData(address, value);
            } else {
                // Padrao: trata como INT
                int intValue = Integer.parseInt(value);
                protocol.writeData(address, intValue);
            }

        } else {
            // INPUT_REGISTER e somente leitura; outros tipos desconhecidos sao rejeitados
            throw new RuntimeException(
                    "Escrita nao suportada para registerType: " + registerType
                            + ". INPUT_REGISTER e somente leitura no protocolo Modbus padrao.");
        }
    }

    // =========================================================================
    // ENDPOINTS
    // =========================================================================

    @GetMapping("/read/device/{deviceId}/variable/{variableId}")
    @Operation(summary = "Le o valor de uma variavel Modbus especifica")
    @ApiResponse(responseCode = "200", description = "Variavel lida com sucesso")
    public ResponseEntity<?> readVariable(
            @Parameter(description = "ID do dispositivo", required = true)
            @PathVariable Long deviceId,

            @Parameter(description = "ID da variavel", required = true)
            @PathVariable Long variableId) {

        try {
            // Busca o dispositivo no repositorio
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado: id=" + deviceId));

            // Busca a variavel no repositorio
            VariableModbusTable variable = variableModbusRepository.findById(variableId)
                    .orElseThrow(() -> new RuntimeException("Variavel nao encontrada: id=" + variableId));

            // Valida se a variavel pertence ao dispositivo informado
            if (!variable.getDevice().getId().equals(deviceId)) {
                throw new RuntimeException(
                        "Variavel id=" + variableId + " nao pertence ao dispositivo id=" + deviceId);
            }

            // Obtem parametros de conexao a partir do PLC e da variavel
            PlcTable plc = device.getPlc();
            String ip = plc.getIp();
            int port = variable.getPort();
            int unitId = variable.getUnitId();

            // Instancia o protocolo Modbus e abre a conexao com o CLP
            AbstractProtocol protocol = new ModbusProtocol(ip, port, unitId);
            protocol.openConnection();

            // Executa a leitura utilizando registerType para selecionar a funcao correta
            Object value = readValue(protocol, variable);

            // Encerra a conexao apos a leitura
            protocol.closeConnection();

            // Monta e retorna a resposta
            Map<String, Object> response = new HashMap<>();
            response.put("deviceId", deviceId);
            response.put("deviceName", device.getName());
            response.put("variableId", variableId);
            response.put("variableName", variable.getName());
            response.put("registerType", variable.getRegisterType());
            response.put("dataType", variable.getDataType());
            response.put("address", variable.getAddress());
            response.put("value", value);
            response.put("unit", variable.getUnit());
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao ler variavel: " + e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

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
            // Busca o dispositivo no repositorio
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado: id=" + deviceId));

            // Busca a variavel no repositorio
            VariableModbusTable variable = variableModbusRepository.findById(variableId)
                    .orElseThrow(() -> new RuntimeException("Variavel nao encontrada: id=" + variableId));

            // Valida se a variavel pertence ao dispositivo informado
            if (!variable.getDevice().getId().equals(deviceId)) {
                throw new RuntimeException(
                        "Variavel id=" + variableId + " nao pertence ao dispositivo id=" + deviceId);
            }

            // Obtem parametros de conexao a partir do PLC e da variavel
            PlcTable plc = device.getPlc();
            String ip = plc.getIp();
            int port = variable.getPort();
            int unitId = variable.getUnitId();

            // Instancia o protocolo Modbus e abre a conexao com o CLP
            AbstractProtocol protocol = new ModbusProtocol(ip, port, unitId);
            protocol.openConnection();

            // Executa a escrita utilizando registerType para selecionar a funcao correta
            writeValue(protocol, variable, value);

            // Encerra a conexao apos a escrita
            protocol.closeConnection();

            // Monta e retorna a resposta
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Valor escrito com sucesso");
            response.put("deviceId", deviceId);
            response.put("deviceName", device.getName());
            response.put("variableId", variableId);
            response.put("variableName", variable.getName());
            response.put("registerType", variable.getRegisterType());
            response.put("dataType", variable.getDataType());
            response.put("address", variable.getAddress());
            response.put("writtenValue", value);
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Formato de valor invalido para o tipo de dado esperado: " + e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao escrever variavel: " + e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/read/device/{deviceId}/all")
    @Operation(summary = "Le todas as variaveis Modbus de um dispositivo")
    @ApiResponse(responseCode = "200", description = "Variaveis lidas com sucesso")
    public ResponseEntity<?> readAllVariables(
            @Parameter(description = "ID do dispositivo", required = true)
            @PathVariable Long deviceId) {

        try {
            // Busca o dispositivo no repositorio
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado: id=" + deviceId));

            // Busca todas as variaveis associadas ao dispositivo
            List<VariableModbusTable> variables = variableModbusRepository.findByDeviceId(deviceId);

            if (variables.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Nenhuma variavel encontrada para este dispositivo");
                response.put("deviceId", deviceId.toString());
                response.put("deviceName", device.getName());
                return ResponseEntity.ok(response);
            }

            // Obtem o IP do PLC associado ao dispositivo
            String ip = device.getPlc().getIp();

            // Itera sobre cada variavel, conecta individualmente e realiza a leitura
            List<Map<String, Object>> results = variables.stream().map(variable -> {
                Map<String, Object> result = new HashMap<>();
                result.put("variableId", variable.getId());
                result.put("variableName", variable.getName());
                result.put("registerType", variable.getRegisterType());
                result.put("dataType", variable.getDataType());
                result.put("unit", variable.getUnit());
                result.put("address", variable.getAddress());

                try {
                    // Cada variavel pode ter porta e unitId proprios
                    int port = variable.getPort();
                    int unitId = variable.getUnitId();

                    // Instancia e conecta o protocolo para esta variavel
                    AbstractProtocol protocol = new ModbusProtocol(ip, port, unitId);
                    protocol.openConnection();

                    // Executa a leitura com base no registerType da variavel
                    Object value = readValue(protocol, variable);

                    // Encerra a conexao apos a leitura
                    protocol.closeConnection();

                    result.put("value", value);
                    result.put("status", "success");

                } catch (Exception e) {
                    // Falha individual nao interrompe a leitura das demais variaveis
                    result.put("value", null);
                    result.put("status", "error");
                    result.put("error", e.getMessage());
                }

                return result;
            }).collect(Collectors.toList());

            // Monta e retorna a resposta consolidada
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