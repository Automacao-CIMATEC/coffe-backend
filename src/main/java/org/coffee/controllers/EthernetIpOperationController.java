package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.coffee.domain.models.database.DeviceTable;
import org.coffee.domain.models.database.PlcTable;
import org.coffee.domain.models.database.VariableEthernetIpTable;
import org.coffee.repository.DeviceRepository;
import org.coffee.repository.VariableEthernetIpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/operations/ethernet-ip")
@Tag(name = "Ethernet/IP Operations", description = "Endpoints para leitura e escrita de variaveis Ethernet/IP configuradas")
public class EthernetIpOperationController {

    private final DeviceRepository deviceRepository;
    private final VariableEthernetIpRepository variableEthernetIpRepository;
    private final RestTemplate restTemplate;

    // URL base da API externa que faz as operacoes de leitura/escrita Ethernet/IP
    // Configuravel via application.properties: ethernetip.api.url
    @Value("${ethernetip.api.url:http://localhost:8002}")
    private String ethernetIpApiUrl;

    @Autowired
    public EthernetIpOperationController(DeviceRepository deviceRepository,
                                         VariableEthernetIpRepository variableEthernetIpRepository,
                                         RestTemplate restTemplate) {
        this.deviceRepository = deviceRepository;
        this.variableEthernetIpRepository = variableEthernetIpRepository;
        this.restTemplate = restTemplate;
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
            // Busca o dispositivo
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

            // Busca a variavel
            VariableEthernetIpTable variable = variableEthernetIpRepository.findById(variableId)
                    .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

            // Valida se a variavel pertence ao dispositivo
            if (!variable.getDevice().getId().equals(deviceId)) {
                throw new RuntimeException("Variavel nao pertence ao dispositivo especificado");
            }

            // Obtem os dados necessarios do banco
            PlcTable plc = device.getPlc();
            String ip = plc.getIp();
            String tagName = variable.getTagName();
            String dataType = variable.getDataType();

            // Converte o dataType para o formato esperado pela API (uppercase)
            String dataTypeFormatted = dataType.toUpperCase();

            // Monta a URL com query parameters
            // IMPORTANTE: tag_name nao deve ter encode dos colchetes [] para funcionar corretamente
            String readUrl = String.format("%s/plc/rockwell/read?ip=%s&data_type=%s&tag_name=%s",
                    ethernetIpApiUrl, ip, dataTypeFormatted, tagName);

            System.out.println("==> Chamando API externa Ethernet/IP");
            System.out.println("==> URL: " + readUrl);

            // Chama a API externa para fazer a leitura (POST sem body)
            ResponseEntity<Map> apiResponse = restTemplate.exchange(
                    readUrl,
                    HttpMethod.POST,
                    null,
                    Map.class
            );

            System.out.println("==> Resposta API: " + apiResponse.getBody());

            // Extrai o valor da resposta da API externa
            Map<String, Object> apiResponseBody = apiResponse.getBody();
            Object value = apiResponseBody != null ? apiResponseBody.get("value") : null;

            // Monta resposta
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

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro na API externa: " + e.getMessage());
            error.put("statusCode", String.valueOf(e.getStatusCode().value()));
            error.put("responseBody", e.getResponseBodyAsString());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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
            // Busca o dispositivo
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

            // Busca a variavel
            VariableEthernetIpTable variable = variableEthernetIpRepository.findById(variableId)
                    .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

            // Valida se a variavel pertence ao dispositivo
            if (!variable.getDevice().getId().equals(deviceId)) {
                throw new RuntimeException("Variavel nao pertence ao dispositivo especificado");
            }

            // Obtem os dados necessarios do banco
            PlcTable plc = device.getPlc();
            String ip = plc.getIp();
            String tagName = variable.getTagName();
            String dataType = variable.getDataType();

            // Converte o dataType para o formato esperado pela API (uppercase)
            String dataTypeFormatted = dataType.toUpperCase();

            // Monta a URL com query parameters
            // IMPORTANTE: tag_name nao deve ter encode dos colchetes [] para funcionar corretamente
            String writeUrl = String.format("%s/plc/rockwell/write?ip=%s&data_type=%s&value=%s&tag_name=%s",
                    ethernetIpApiUrl, ip, dataTypeFormatted, value, tagName);

            System.out.println("==> Chamando API externa Ethernet/IP para escrita");
            System.out.println("==> URL: " + writeUrl);

            // Chama a API externa para fazer a escrita (POST sem body)
            ResponseEntity<Map> apiResponse = restTemplate.exchange(
                    writeUrl,
                    HttpMethod.POST,
                    null,
                    Map.class
            );

            System.out.println("==> Resposta API: " + apiResponse.getBody());

            // Monta resposta
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

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro na API externa: " + e.getMessage());
            error.put("statusCode", String.valueOf(e.getStatusCode().value()));
            error.put("responseBody", e.getResponseBodyAsString());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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
            // Busca o dispositivo
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

            // Busca todas as variaveis do dispositivo
            List<VariableEthernetIpTable> variables = variableEthernetIpRepository.findByDeviceId(deviceId);

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
                result.put("tagName", variable.getTagName());
                result.put("unit", variable.getUnit());
                result.put("dataType", variable.getDataType());

                try {
                    String tagName = variable.getTagName();
                    String dataType = variable.getDataType();

                    // Converte o dataType para o formato esperado pela API (uppercase)
                    String dataTypeFormatted = dataType.toUpperCase();

                    // Monta a URL com query parameters
                    // IMPORTANTE: tag_name nao deve ter encode dos colchetes [] para funcionar corretamente
                    String readUrl = String.format("%s/plc/rockwell/read?ip=%s&data_type=%s&tag_name=%s",
                            ethernetIpApiUrl, ip, dataTypeFormatted, tagName);

                    // Chama a API externa para fazer a leitura (POST sem body)
                    ResponseEntity<Map> apiResponse = restTemplate.exchange(
                            readUrl,
                            HttpMethod.POST,
                            null,
                            Map.class
                    );

                    // Extrai o valor da resposta da API externa
                    Map<String, Object> apiResponseBody = apiResponse.getBody();
                    Object value = apiResponseBody != null ? apiResponseBody.get("value") : null;

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