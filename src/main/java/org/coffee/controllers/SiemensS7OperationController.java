package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.coffee.domain.models.SiemensS7Protocol;
import org.coffee.domain.models.database.DeviceTable;
import org.coffee.domain.models.database.PlcSiemensS7ConfigTable;
import org.coffee.domain.models.database.PlcTable;
import org.coffee.domain.models.database.VariableSiemensS7Table;
import org.coffee.repository.DeviceRepository;
import org.coffee.repository.VariableSiemensS7Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Endpoints para leitura e escrita de variaveis Siemens S7 cadastradas.
 *
 * Esta versao substitui a comunicacao HTTP com o microservico Python pela
 * implementacao nativa Java via Apache PLC4X. A interface publica (URLs,
 * payloads) e o comportamento dos endpoints foram preservados para nao
 * quebrar consumidores existentes.
 */
@RestController
@RequestMapping("/api/operations/siemens-s7")
@Tag(name = "Siemens S7 Operations",
     description = "Endpoints para leitura e escrita de variaveis Siemens S7 configuradas")
public class SiemensS7OperationController {

    private final DeviceRepository deviceRepository;
    private final VariableSiemensS7Repository variableSiemensS7Repository;

    @Autowired
    public SiemensS7OperationController(DeviceRepository deviceRepository,
                                        VariableSiemensS7Repository variableSiemensS7Repository) {
        this.deviceRepository = deviceRepository;
        this.variableSiemensS7Repository = variableSiemensS7Repository;
    }

    // Le o valor de uma variavel especifica
    @GetMapping("/read/device/{deviceId}/variable/{variableId}")
    @Operation(summary = "Le o valor de uma variavel Siemens S7 especifica")
    @ApiResponse(responseCode = "200", description = "Variavel lida com sucesso")
    public ResponseEntity<?> readVariable(
            @Parameter(description = "ID do dispositivo", required = true)
            @PathVariable Long deviceId,

            @Parameter(description = "ID da variavel", required = true)
            @PathVariable Long variableId) {

        SiemensS7Protocol protocol = null;
        try {
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

            VariableSiemensS7Table variable = variableSiemensS7Repository.findById(variableId)
                    .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

            if (!variable.getDevice().getId().equals(deviceId)) {
                throw new RuntimeException("Variavel nao pertence ao dispositivo especificado");
            }

            PlcTable plc = device.getPlc();
            PlcSiemensS7ConfigTable s7Config = requireS7Config(plc);

            protocol = new SiemensS7Protocol(plc.getIp(), s7Config.getRack(), s7Config.getSlot());
            protocol.openConnection();

            Object value = readByDataType(
                    protocol,
                    variable.getDataType(),
                    variable.getDbNumber(),
                    variable.getOffset(),
                    variable.getBitOffset()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("deviceId", deviceId);
            response.put("deviceName", device.getName());
            response.put("variableId", variableId);
            response.put("variableName", variable.getName());
            response.put("dbNumber", variable.getDbNumber());
            response.put("offset", variable.getOffset());
            response.put("bitOffset", variable.getBitOffset());
            response.put("value", value);
            response.put("unit", variable.getUnit());
            response.put("dataType", variable.getDataType());
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (ConfigMissingException e) {
            // Caso especifico: PLC nao tem config S7 cadastrada.
            // 400 e mais informativo que 500 - o cliente sabe que precisa cadastrar.
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao ler variavel: " + e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } finally {
            if (protocol != null) {
                protocol.closeConnection();
            }
        }
    }

    // Escreve um valor em uma variavel especifica
    @PostMapping("/write/device/{deviceId}/variable/{variableId}")
    @Operation(summary = "Escreve um valor em uma variavel Siemens S7 especifica")
    @ApiResponse(responseCode = "200", description = "Variavel escrita com sucesso")
    public ResponseEntity<?> writeVariable(
            @Parameter(description = "ID do dispositivo", required = true)
            @PathVariable Long deviceId,

            @Parameter(description = "ID da variavel", required = true)
            @PathVariable Long variableId,

            @Parameter(description = "Valor a ser escrito", required = true)
            @RequestParam String value) {

        SiemensS7Protocol protocol = null;
        try {
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

            VariableSiemensS7Table variable = variableSiemensS7Repository.findById(variableId)
                    .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

            if (!variable.getDevice().getId().equals(deviceId)) {
                throw new RuntimeException("Variavel nao pertence ao dispositivo especificado");
            }

            PlcTable plc = device.getPlc();
            PlcSiemensS7ConfigTable s7Config = requireS7Config(plc);

            protocol = new SiemensS7Protocol(plc.getIp(), s7Config.getRack(), s7Config.getSlot());
            protocol.openConnection();

            writeByDataType(
                    protocol,
                    variable.getDataType(),
                    variable.getDbNumber(),
                    variable.getOffset(),
                    variable.getBitOffset(),
                    value
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Valor escrito com sucesso");
            response.put("deviceId", deviceId);
            response.put("deviceName", device.getName());
            response.put("variableId", variableId);
            response.put("variableName", variable.getName());
            response.put("dbNumber", variable.getDbNumber());
            response.put("offset", variable.getOffset());
            response.put("bitOffset", variable.getBitOffset());
            response.put("writtenValue", value);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.ok(response);

        } catch (ConfigMissingException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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
        } finally {
            if (protocol != null) {
                protocol.closeConnection();
            }
        }
    }

    // Le todas as variaveis de um dispositivo
    // Otimizacao importante: abre UMA UNICA conexao S7 para todas as
    // variaveis em vez de uma conexao por variavel (que era o que o
    // codigo via HTTP fazia implicitamente).
    @GetMapping("/read/device/{deviceId}/all")
    @Operation(summary = "Le todas as variaveis Siemens S7 de um dispositivo")
    @ApiResponse(responseCode = "200", description = "Variaveis lidas com sucesso")
    public ResponseEntity<?> readAllVariables(
            @Parameter(description = "ID do dispositivo", required = true)
            @PathVariable Long deviceId) {

        SiemensS7Protocol protocol = null;
        try {
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

            List<VariableSiemensS7Table> variables =
                    variableSiemensS7Repository.findByDeviceId(deviceId);

            if (variables.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Nenhuma variavel encontrada para este dispositivo");
                response.put("deviceId", deviceId);
                response.put("deviceName", device.getName());
                return ResponseEntity.ok(response);
            }

            PlcTable plc = device.getPlc();
            PlcSiemensS7ConfigTable s7Config = requireS7Config(plc);

            protocol = new SiemensS7Protocol(plc.getIp(), s7Config.getRack(), s7Config.getSlot());
            protocol.openConnection();

            // Conexao ja aberta - basta efetivar a final para usar dentro do lambda
            final SiemensS7Protocol activeProtocol = protocol;

            List<Map<String, Object>> results = variables.stream().map(variable -> {
                Map<String, Object> result = new HashMap<>();
                result.put("variableId", variable.getId());
                result.put("variableName", variable.getName());
                result.put("dbNumber", variable.getDbNumber());
                result.put("offset", variable.getOffset());
                result.put("bitOffset", variable.getBitOffset());
                result.put("unit", variable.getUnit());
                result.put("dataType", variable.getDataType());

                try {
                    Object value = readByDataType(
                            activeProtocol,
                            variable.getDataType(),
                            variable.getDbNumber(),
                            variable.getOffset(),
                            variable.getBitOffset()
                    );
                    result.put("value", value);
                    result.put("status", "success");
                } catch (Exception e) {
                    result.put("value", null);
                    result.put("status", "error");
                    result.put("error", e.getMessage());
                }
                return result;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("deviceId", deviceId);
            response.put("deviceName", device.getName());
            response.put("totalVariables", variables.size());
            response.put("variables", results);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.ok(response);

        } catch (ConfigMissingException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao ler variaveis: " + e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } finally {
            if (protocol != null) {
                protocol.closeConnection();
            }
        }
    }

    // HELPERS PRIVADOS
    /**
     * Garante que o PLC tem configuracao S7 cadastrada.
     * Lanca ConfigMissingException se nao tiver - tratada como HTTP 400.
     */
    private PlcSiemensS7ConfigTable requireS7Config(PlcTable plc) {
        PlcSiemensS7ConfigTable config = plc.getSiemensS7Config();
        if (config == null) {
            throw new ConfigMissingException(
                    "PLC '" + plc.getName() + "' (id=" + plc.getId()
                    + ") nao possui configuracao Siemens S7. "
                    + "Cadastre rack e slot via POST /api/plc/" + plc.getId()
                    + "/siemens-s7-config antes de ler ou escrever variaveis S7.");
        }
        return config;
    }

    /**
     * Despacha leitura para o metodo correto do SiemensS7Protocol com base no dataType.
     * Tipos aceitos: BOOL/BOOLEAN, BYTE, WORD, DWORD, INT, DINT, REAL/FLOAT, STRING.
     */
    private Object readByDataType(SiemensS7Protocol protocol,
                                   String dataType,
                                   Integer dbNumber,
                                   Integer offset,
                                   Integer bitOffset) {
        String t = dataType == null ? "" : dataType.toUpperCase();
        switch (t) {
            case "BOOL":
            case "BOOLEAN":
                return protocol.readBool(dbNumber, offset, bitOffset != null ? bitOffset : 0);
            case "BYTE":
                return protocol.readByte(dbNumber, offset);
            case "WORD":
                return protocol.readWord(dbNumber, offset);
            case "DWORD":
                return protocol.readDWord(dbNumber, offset);
            case "INT":
            case "INTEGER":
                return protocol.readInt(dbNumber, offset);
            case "DINT":
                return protocol.readDInt(dbNumber, offset);
            case "REAL":
            case "FLOAT":
                return protocol.readReal(dbNumber, offset);
            case "STRING":
                return protocol.readString(dbNumber, offset);
            default:
                throw new IllegalArgumentException(
                        "Tipo de dado nao suportado para Siemens S7: " + dataType);
        }
    }

    /**
     * Despacha escrita para o metodo correto do SiemensS7Protocol com base no dataType.
     * Converte a String recebida pelo HTTP para o tipo nativo apropriado.
     */
    private void writeByDataType(SiemensS7Protocol protocol,
                                  String dataType,
                                  Integer dbNumber,
                                  Integer offset,
                                  Integer bitOffset,
                                  String value) {
        String t = dataType == null ? "" : dataType.toUpperCase();
        int bit = bitOffset != null ? bitOffset : 0;
        switch (t) {
            case "BOOL":
            case "BOOLEAN":
                protocol.writeBool(dbNumber, offset, bit, Boolean.parseBoolean(value));
                break;
            case "BYTE":
                protocol.writeByte(dbNumber, offset, Byte.parseByte(value));
                break;
            case "WORD":
                protocol.writeWord(dbNumber, offset, Integer.parseInt(value));
                break;
            case "DWORD":
                protocol.writeDWord(dbNumber, offset, Long.parseLong(value));
                break;
            case "INT":
            case "INTEGER":
                protocol.writeInt(dbNumber, offset, Integer.parseInt(value));
                break;
            case "DINT":
                protocol.writeDInt(dbNumber, offset, Integer.parseInt(value));
                break;
            case "REAL":
            case "FLOAT":
                protocol.writeReal(dbNumber, offset, Float.parseFloat(value));
                break;
            case "STRING":
                protocol.writeString(dbNumber, offset, value);
                break;
            default:
                throw new IllegalArgumentException(
                        "Tipo de dado nao suportado para Siemens S7: " + dataType);
        }
    }

    /**
     * Excecao interna para sinalizar config S7 ausente.
     * Mantida private static para nao vazar do escopo deste controller.
     */
    private static class ConfigMissingException extends RuntimeException {
        ConfigMissingException(String message) {
            super(message);
        }
    }
}
