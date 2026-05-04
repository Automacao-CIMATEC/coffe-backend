package org.coffee.controllers;

import org.coffee.domain.models.VariableEthernetIp;
import org.coffee.services.VariableEthernetIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/variables/ethernet-ip")
public class VariableEthernetIpController {

    private final VariableEthernetIpService variableEthernetIpService;

    @Autowired
    public VariableEthernetIpController(VariableEthernetIpService variableEthernetIpService) {
        this.variableEthernetIpService = variableEthernetIpService;
    }

    // Cria uma nova variavel Ethernet/IP - POST /api/variables/ethernet-ip
    @PostMapping
    public ResponseEntity<?> createVariableEthernetIp(@RequestBody VariableEthernetIp variable) {
        try {
            System.out.println("==> POST /api/variables/ethernet-ip - Recebido: " + variable);
            VariableEthernetIp createdVariable = variableEthernetIpService.createVariableEthernetIp(variable);
            System.out.println("==> Variavel Ethernet/IP criada com sucesso: " + createdVariable.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVariable);
        } catch (RuntimeException e) {
            System.err.println("==> ERRO ao criar variavel Ethernet/IP: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Retorna todas as variaveis Ethernet/IP - GET /api/variables/ethernet-ip
    @GetMapping
    public ResponseEntity<List<VariableEthernetIp>> getAllVariablesEthernetIp() {
        List<VariableEthernetIp> variables = variableEthernetIpService.getAllVariablesEthernetIp();
        return ResponseEntity.ok(variables);
    }

    // Busca uma variavel Ethernet/IP por ID - GET /api/variables/ethernet-ip/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getVariableEthernetIpById(@PathVariable Long id) {
        return variableEthernetIpService.getVariableEthernetIpById(id)
                .map(variable -> ResponseEntity.ok((Object) variable))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Variavel Ethernet/IP nao encontrada")));
    }

    // Busca variaveis Ethernet/IP por dispositivo - GET /api/variables/ethernet-ip/device/{deviceId}
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<VariableEthernetIp>> getVariableEthernetIpByDeviceId(@PathVariable Long deviceId) {
        List<VariableEthernetIp> variables = variableEthernetIpService.getVariableEthernetIpByDeviceId(deviceId);
        return ResponseEntity.ok(variables);
    }

    // Busca variaveis Ethernet/IP por tipo de dados - GET /api/variables/ethernet-ip/data-type/{dataType}
    @GetMapping("/data-type/{dataType}")
    public ResponseEntity<List<VariableEthernetIp>> getVariableEthernetIpByDataType(@PathVariable String dataType) {
        List<VariableEthernetIp> variables = variableEthernetIpService.getVariableEthernetIpByDataType(dataType);
        return ResponseEntity.ok(variables);
    }

    // Busca variavel por nome da tag - GET /api/variables/ethernet-ip/tag/{tagName}
    @GetMapping("/tag/{tagName}")
    public ResponseEntity<?> getVariableEthernetIpByTagName(@PathVariable String tagName) {
        return variableEthernetIpService.getVariableEthernetIpByTagName(tagName)
                .map(variable -> ResponseEntity.ok((Object) variable))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Variavel com tag name nao encontrada")));
    }

    // Atualiza uma variavel Ethernet/IP existente - PUT /api/variables/ethernet-ip/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVariableEthernetIp(@PathVariable Long id, @RequestBody VariableEthernetIp variable) {
        try {
            System.out.println("==> PUT /api/variables/ethernet-ip/" + id + " - Recebido: " + variable);
            VariableEthernetIp updatedVariable = variableEthernetIpService.updateVariableEthernetIp(id, variable);
            System.out.println("==> Variavel Ethernet/IP atualizada com sucesso");
            return ResponseEntity.ok(updatedVariable);
        } catch (RuntimeException e) {
            System.err.println("==> ERRO ao atualizar variavel Ethernet/IP: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Remove uma variavel Ethernet/IP - DELETE /api/variables/ethernet-ip/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVariableEthernetIp(@PathVariable Long id) {
        try {
            variableEthernetIpService.deleteVariableEthernetIp(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}