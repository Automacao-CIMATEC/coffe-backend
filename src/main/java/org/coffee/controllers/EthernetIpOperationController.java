package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.coffee.domain.models.EthernetIpProtocol;
import org.coffee.domain.models.database.DeviceTable;
import org.coffee.domain.models.database.PlcTable;
import org.coffee.domain.models.database.VariableEthernetIpTable;
import org.coffee.repository.DeviceRepository;
import org.coffee.repository.VariableEthernetIpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller para leitura e escrita de variaveis Ethernet/IP configuradas no banco de dados.
 *
 * Opera sobre variaveis pre-cadastradas vinculadas a dispositivos do banco.
 * Comunicacao realizada diretamente via EthernetIpProtocol, sem servico externo.
 */
@RestController
@RequestMapping("/api/operations/ethernet-ip")
@Tag(name = "Ethernet/IP Operations", description = "Endpoints para leitura e escrita de variaveis Ethernet/IP configuradas")
public class EthernetIpOperationController {

    private final DeviceRepository deviceRepository;
    private final VariableEthernetIpRepository variableEthernetIpRepository;

    @Autowired
    public EthernetIpOperationController(DeviceRepository deviceRepository,
                                         VariableEthernetIpRepository variableEthernetIpRepository) {
        this.deviceRepository = deviceRepository;
        this.variableEthernetIpRepository = variableEthernetIpRepository;
    }

    // Le o valor de uma variavel especifica
    @GetMapping("/read/device/{deviceId}/variable/{variableId}")
    @Operation(summary = "Le o valor de uma variavel Ethernet/IP especifica")
    @ApiResponse(responseCode = "200", description = "Variavel lida com sucesso")
    public ResponseEntity<?> readVariable(
            @Parameter(description = "ID do dispositivo", required = true)
            @PathVariable Long deviceId,

            @Parameter(description = "ID da variavel", required = true)
            @PathVariable Long variableId) {

        try {
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

            VariableEthernetIpTable variable = variableEthernetIpRepository.findById(variableId)
                    .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

            if (!variable.getDevice().getId().equals(deviceId)) {
                throw new RuntimeException("Variavel nao pertence ao dispositivo especificado");
            }

            PlcTable plc = device.getPlc();
            String ip = plc.getIp();
            String tagName = variable.getTagName();
            String dataType = variable.getDataType();

            System.out.println("==> Leitura Ethernet/IP: ip=" + ip + " tag=" + tagName + " tipo=" + dataType);

            // Executa a leitura via protocolo nativo
            Object value = executarLeitura(ip, tagName, dataType);

            Map<String, Object> response = new HashMap<>();
            response.put("deviceId", deviceId);
            response.put("deviceName", device.getName());
            response.put("variableId", variableId);
            response.put("variableName", variable.getName());
            response.put("tagName", tagName);
            response.put("value", value);
            response.put("unit", variable.getUnit());
            response.put("dataType", variable.getDataType());
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
    @Operation(summary = "Escreve um valor em uma variavel Ethernet/IP especifica")
    @ApiResponse(responseCode = "200", description = "Variavel escrita com sucesso")
    public ResponseEntity<?> writeVariable(
            @Parameter(description = "ID do dispositivo", required = true)
            @PathVariable Long deviceId,

            @Parameter(description = "ID da variavel", required = true)
            @PathVariable Long variableId,

            @Parameter(description = "Valor a ser escrito", required = true)
            @RequestParam String value) {

        try {
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

            VariableEthernetIpTable variable = variableEthernetIpRepository.findById(variableId)
                    .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

            if (!variable.getDevice().getId().equals(deviceId)) {
                throw new RuntimeException("Variavel nao pertence ao dispositivo especificado");
            }

            PlcTable plc = device.getPlc();
            String ip = plc.getIp();
            String tagName = variable.getTagName();
            String dataType = variable.getDataType();

            System.out.println("==> Escrita Ethernet/IP: ip=" + ip + " tag=" + tagName + " tipo=" + dataType + " valor=" + value);

            // Executa a escrita via protocolo nativo
            executarEscrita(ip, tagName, dataType, value);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Valor escrito com sucesso");
            response.put("deviceId", deviceId);
            response.put("deviceName", device.getName());
            response.put("variableId", variableId);
            response.put("variableName", variable.getName());
            response.put("tagName", tagName);
            response.put("writtenValue", value);
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
    @Operation(summary = "Le todas as variaveis Ethernet/IP de um dispositivo")
    @ApiResponse(responseCode = "200", description = "Variaveis lidas com sucesso")
    public ResponseEntity<?> readAllVariables(
            @Parameter(description = "ID do dispositivo", required = true)
            @PathVariable Long deviceId) {

        try {
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

            List<VariableEthernetIpTable> variables = variableEthernetIpRepository.findByDeviceId(deviceId);

            if (variables.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Nenhuma variavel encontrada para este dispositivo");
                response.put("deviceId", deviceId.toString());
                response.put("deviceName", device.getName());
                return ResponseEntity.ok(response);
            }

            PlcTable plc = device.getPlc();
            String ip = plc.getIp();

            // Le todas as variaveis do dispositivo individualmente
            List<Map<String, Object>> results = variables.stream().map(variable -> {
                Map<String, Object> result = new HashMap<>();
                result.put("variableId", variable.getId());
                result.put("variableName", variable.getName());
                result.put("tagName", variable.getTagName());
                result.put("unit", variable.getUnit());
                result.put("dataType", variable.getDataType());

                try {
                    Object value = executarLeitura(ip, variable.getTagName(), variable.getDataType());
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

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao ler variaveis: " + e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // =========================================================================
    // METODOS AUXILIARES PRIVADOS
    // =========================================================================

    /**
     * Executa a leitura de uma tag no CLP.
     * Abre e fecha a conexao a cada leitura para simplicidade e seguranca.
     *
     * @param ip       endereco IP do CLP
     * @param tagName  nome simbolico da tag (suporta bit address ex: "Tag.0")
     * @param dataType tipo de dado esperado (BOOL, INT, REAL, STRING)
     * @return valor lido convertido para o tipo Java correspondente
     * @throws Exception se a conexao ou leitura falhar
     */
    private Object executarLeitura(String ip, String tagName, String dataType) throws Exception {
        // Slot 0 e o padrao para CompactLogix/ControlLogix
        EthernetIpProtocol protocol = new EthernetIpProtocol(ip, 0);
        try {
            protocol.openConnection();

            String tipo = dataType.toUpperCase();
            if (tipo.equals("BOOL") || tipo.equals("BOOLEAN")) {
                return protocol.readBool(tagName);
            } else if (tipo.equals("INT") || tipo.equals("INTEGER") || tipo.equals("DINT") || tipo.equals("SINT")) {
                return protocol.readInt(tagName);
            } else if (tipo.equals("REAL") || tipo.equals("FLOAT")) {
                return protocol.readReal(tagName);
            } else if (tipo.equals("STRING")) {
                return protocol.readString(tagName);
            } else {
                // Tipo nao reconhecido: leitura generica como inteiro
                return protocol.readInt(tagName);
            }
        } finally {
            // Garante fechamento mesmo em caso de excecao
            protocol.closeConnection();
        }
    }

    /**
     * Executa a escrita de um valor em uma tag do CLP.
     * Converte o valor string para o tipo Java adequado antes da escrita.
     *
     * @param ip       endereco IP do CLP
     * @param tagName  nome simbolico da tag (suporta bit address ex: "Tag.0")
     * @param dataType tipo de dado da tag (BOOL, INT, REAL, STRING)
     * @param value    valor a ser escrito em formato string
     * @throws Exception se a conexao, conversao ou escrita falhar
     */
    private void executarEscrita(String ip, String tagName, String dataType, String value) throws Exception {
        EthernetIpProtocol protocol = new EthernetIpProtocol(ip, 0);
        try {
            protocol.openConnection();

            String tipo = dataType.toUpperCase();
            if (tipo.equals("BOOL") || tipo.equals("BOOLEAN")) {
                boolean boolVal = value.equalsIgnoreCase("true") || value.equals("1");
                protocol.writeBool(tagName, boolVal);
            } else if (tipo.equals("INT") || tipo.equals("INTEGER") || tipo.equals("DINT") || tipo.equals("SINT")) {
                protocol.writeInt(tagName, Integer.parseInt(value));
            } else if (tipo.equals("REAL") || tipo.equals("FLOAT")) {
                protocol.writeReal(tagName, Float.parseFloat(value));
            } else if (tipo.equals("STRING")) {
                protocol.writeString(tagName, value);
            } else {
                // Tipo nao reconhecido: tenta como inteiro
                protocol.writeInt(tagName, Integer.parseInt(value));
            }
        } finally {
            protocol.closeConnection();
        }
    }
}
