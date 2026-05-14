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
import org.coffee.services.SiemensS7ConnectionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Endpoints para leitura, escrita e gerenciamento de conexao Siemens S7.
 *
 * MUDANCA DE DESIGN (v2): conexao agora e explicita. O fluxo correto e:
 *   1. POST /connect/device/{id}                    abre a conexao
 *   2. GET  /read/device/{id}/variable/{varId}      le (varias vezes)
 *   3. POST /write/device/{id}/variable/{varId}     escreve (varias vezes)
 *   4. POST /disconnect/device/{id}                 fecha quando nao precisar mais
 *
 * Tentar ler/escrever sem ter aberto retorna HTTP 409 com mensagem clara.
 */
@RestController
@RequestMapping("/api/operations/siemens-s7")
@Tag(name = "Siemens S7 Operations",
        description = "Conexao, leitura e escrita de variaveis Siemens S7 (PLC4X nativo)")
public class SiemensS7OperationController {

    private final DeviceRepository deviceRepository;
    private final VariableSiemensS7Repository variableSiemensS7Repository;
    private final SiemensS7ConnectionRegistry connectionRegistry;

    @Autowired
    public SiemensS7OperationController(DeviceRepository deviceRepository,
                                        VariableSiemensS7Repository variableSiemensS7Repository,
                                        SiemensS7ConnectionRegistry connectionRegistry) {
        this.deviceRepository = deviceRepository;
        this.variableSiemensS7Repository = variableSiemensS7Repository;
        this.connectionRegistry = connectionRegistry;
    }

    // ====================================================================
    // GERENCIAMENTO DE CONEXAO
    // ====================================================================

    @PostMapping("/connect/device/{deviceId}")
    @Operation(summary = "Abre a conexao S7 com o dispositivo. Idempotente.")
    @ApiResponse(responseCode = "200", description = "Conexao aberta ou ja estava aberta")
    @ApiResponse(responseCode = "400", description = "Device, PLC ou config S7 invalido")
    @ApiResponse(responseCode = "503", description = "Falha ao conectar ao CLP")
    public ResponseEntity<?> connect(
            @Parameter(description = "ID do dispositivo", required = true)
            @PathVariable Long deviceId) {
        try {
            SiemensS7ConnectionRegistry.ConnectionResult result =
                    connectionRegistry.openConnection(deviceId);

            Map<String, Object> response = new HashMap<>();
            response.put("deviceId", deviceId);
            response.put("status", "OPEN");
            response.put("alreadyOpen", result.isAlreadyOpen());
            response.put("message", result.isAlreadyOpen()
                    ? "Conexao ja estava aberta"
                    : "Conexao aberta com sucesso");
            response.put("timestamp", now());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Erros de configuracao (device inexistente, sem config S7) - 400
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            // Falha de rede/CLP - 503 sinaliza que o servico externo (CLP) esta indisponivel
            return error(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        }
    }

    @PostMapping("/disconnect/device/{deviceId}")
    @Operation(summary = "Fecha a conexao S7 com o dispositivo. Idempotente.")
    @ApiResponse(responseCode = "200", description = "Conexao fechada ou nao havia conexao aberta")
    public ResponseEntity<?> disconnect(
            @Parameter(description = "ID do dispositivo", required = true)
            @PathVariable Long deviceId) {
        boolean wasOpen = connectionRegistry.closeConnection(deviceId);
        Map<String, Object> response = new HashMap<>();
        response.put("deviceId", deviceId);
        response.put("status", "CLOSED");
        response.put("wasOpen", wasOpen);
        response.put("message", wasOpen
                ? "Conexao fechada com sucesso"
                : "Nenhuma conexao estava aberta para este device");
        response.put("timestamp", now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/device/{deviceId}")
    @Operation(summary = "Consulta o status da conexao S7 do dispositivo")
    @ApiResponse(responseCode = "200", description = "Status retornado")
    public ResponseEntity<?> status(
            @Parameter(description = "ID do dispositivo", required = true)
            @PathVariable Long deviceId) {
        SiemensS7ConnectionRegistry.ConnectionStatus status =
                connectionRegistry.getStatus(deviceId);

        Map<String, Object> response = new HashMap<>();
        response.put("deviceId", deviceId);
        response.put("status", status.name());
        response.put("connected", status == SiemensS7ConnectionRegistry.ConnectionStatus.OPEN);
        // STALE significa que a conexao caiu - util para o front saber que precisa reconectar
        if (status == SiemensS7ConnectionRegistry.ConnectionStatus.STALE) {
            response.put("message", "Conexao existe no registry mas caiu. Reabra com /connect.");
        }
        response.put("timestamp", now());
        return ResponseEntity.ok(response);
    }

    // LEITURA E ESCRITA

    @GetMapping("/read/device/{deviceId}/variable/{variableId}")
    @Operation(summary = "Le o valor de uma variavel Siemens S7 especifica")
    @ApiResponse(responseCode = "200", description = "Variavel lida com sucesso")
    @ApiResponse(responseCode = "409", description = "Conexao nao esta aberta para o device")
    public ResponseEntity<?> readVariable(
            @PathVariable Long deviceId,
            @PathVariable Long variableId) {
        try {
            VariableSiemensS7Table variable = variableSiemensS7Repository.findById(variableId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Variavel " + variableId + " nao encontrada"));

            if (!variable.getDevice().getId().equals(deviceId)) {
                throw new IllegalArgumentException(
                        "Variavel nao pertence ao dispositivo informado");
            }

            SiemensS7Protocol protocol = buildProtocol(deviceId);

            Object value = readByDataType(
                    protocol,
                    variable.getDataType(),
                    variable.getDbNumber(),
                    variable.getOffset(),
                    variable.getBitOffset()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("deviceId", deviceId);
            response.put("variableId", variableId);
            response.put("variableName", variable.getName());
            response.put("dbNumber", variable.getDbNumber());
            response.put("offset", variable.getOffset());
            response.put("bitOffset", variable.getBitOffset());
            response.put("value", value);
            response.put("unit", variable.getUnit());
            response.put("dataType", variable.getDataType());
            response.put("timestamp", now());
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            // Conexao nao aberta - 409 Conflict (estado invalido do recurso)
            return error(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao ler variavel: " + e.getMessage());
        }
    }

    @PostMapping("/write/device/{deviceId}/variable/{variableId}")
    @Operation(summary = "Escreve um valor em uma variavel Siemens S7 especifica")
    @ApiResponse(responseCode = "200", description = "Variavel escrita com sucesso")
    @ApiResponse(responseCode = "409", description = "Conexao nao esta aberta para o device")
    public ResponseEntity<?> writeVariable(
            @PathVariable Long deviceId,
            @PathVariable Long variableId,
            @RequestParam String value) {
        try {
            VariableSiemensS7Table variable = variableSiemensS7Repository.findById(variableId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Variavel " + variableId + " nao encontrada"));

            if (!variable.getDevice().getId().equals(deviceId)) {
                throw new IllegalArgumentException(
                        "Variavel nao pertence ao dispositivo informado");
            }

            SiemensS7Protocol protocol = buildProtocol(deviceId);

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
            response.put("variableId", variableId);
            response.put("variableName", variable.getName());
            response.put("dbNumber", variable.getDbNumber());
            response.put("offset", variable.getOffset());
            response.put("bitOffset", variable.getBitOffset());
            response.put("writtenValue", value);
            response.put("timestamp", now());
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return error(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao escrever variavel: " + e.getMessage());
        }
    }

    @GetMapping("/read/device/{deviceId}/all")
    @Operation(summary = "Le todas as variaveis Siemens S7 de um dispositivo")
    @ApiResponse(responseCode = "200", description = "Variaveis lidas com sucesso")
    @ApiResponse(responseCode = "409", description = "Conexao nao esta aberta para o device")
    public ResponseEntity<?> readAllVariables(@PathVariable Long deviceId) {
        try {
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Device " + deviceId + " nao encontrado"));

            List<VariableSiemensS7Table> variables =
                    variableSiemensS7Repository.findByDeviceId(deviceId);

            if (variables.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Nenhuma variavel encontrada para este dispositivo");
                response.put("deviceId", deviceId);
                response.put("deviceName", device.getName());
                return ResponseEntity.ok(response);
            }

            SiemensS7Protocol protocol = buildProtocol(deviceId);

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
                            protocol,
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
            response.put("timestamp", now());
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return error(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao ler variaveis: " + e.getMessage());
        }
    }

    // HELPERS PRIVADOS

    /**
     * Monta um SiemensS7Protocol usando a conexao ja aberta do registry.
     * Lanca IllegalStateException se nao houver conexao aberta - sera
     * mapeada para HTTP 409 no controller.
     */
    private SiemensS7Protocol buildProtocol(Long deviceId) {
        // Resolve rack/slot/ip via banco apenas para anotar no protocol.
        // A conexao real ja foi aberta com esses valores no /connect.
        DeviceTable device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Device " + deviceId + " nao encontrado"));
        PlcTable plc = device.getPlc();
        PlcSiemensS7ConfigTable cfg = plc.getSiemensS7Config();
        // cfg pode ser null aqui SO se alguem deletou a config DEPOIS do /connect.
        // Nesse caso a conexao existe e funciona, mas nao temos como anotar rack/slot.
        int rack = cfg != null ? cfg.getRack() : 0;
        int slot = cfg != null ? cfg.getSlot() : 1;

        return new SiemensS7Protocol(
                plc.getIp(), rack, slot,
                connectionRegistry.getActiveConnection(deviceId)
        );
    }

    private Object readByDataType(SiemensS7Protocol protocol, String dataType,
                                  Integer dbNumber, Integer offset, Integer bitOffset) {
        String t = dataType == null ? "" : dataType.toUpperCase();
        switch (t) {
            case "BOOL": case "BOOLEAN":
                return protocol.readBool(dbNumber, offset, bitOffset != null ? bitOffset : 0);
            case "BYTE":    return protocol.readByte(dbNumber, offset);
            case "WORD":    return protocol.readWord(dbNumber, offset);
            case "DWORD":   return protocol.readDWord(dbNumber, offset);
            case "INT": case "INTEGER":  return protocol.readInt(dbNumber, offset);
            case "DINT":    return protocol.readDInt(dbNumber, offset);
            case "REAL": case "FLOAT":   return protocol.readReal(dbNumber, offset);
            case "STRING":  return protocol.readString(dbNumber, offset);
            default: throw new IllegalArgumentException(
                    "Tipo de dado nao suportado para Siemens S7: " + dataType);
        }
    }

    private void writeByDataType(SiemensS7Protocol protocol, String dataType,
                                 Integer dbNumber, Integer offset, Integer bitOffset,
                                 String value) {
        String t = dataType == null ? "" : dataType.toUpperCase();
        int bit = bitOffset != null ? bitOffset : 0;
        switch (t) {
            case "BOOL": case "BOOLEAN":
                protocol.writeBool(dbNumber, offset, bit, Boolean.parseBoolean(value)); break;
            case "BYTE":    protocol.writeByte(dbNumber, offset, Byte.parseByte(value)); break;
            case "WORD":    protocol.writeWord(dbNumber, offset, Integer.parseInt(value)); break;
            case "DWORD":   protocol.writeDWord(dbNumber, offset, Long.parseLong(value)); break;
            case "INT": case "INTEGER":
                protocol.writeInt(dbNumber, offset, Integer.parseInt(value)); break;
            case "DINT":    protocol.writeDInt(dbNumber, offset, Integer.parseInt(value)); break;
            case "REAL": case "FLOAT":
                protocol.writeReal(dbNumber, offset, Float.parseFloat(value)); break;
            case "STRING":  protocol.writeString(dbNumber, offset, value); break;
            default: throw new IllegalArgumentException(
                    "Tipo de dado nao suportado para Siemens S7: " + dataType);
        }
    }

    private static String now() {
        return java.time.LocalDateTime.now().toString();
    }

    private ResponseEntity<?> error(HttpStatus status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", message);
        body.put("timestamp", now());
        return ResponseEntity.status(status).body(body);
    }
}
