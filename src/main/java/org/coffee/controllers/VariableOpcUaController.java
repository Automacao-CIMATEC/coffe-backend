package org.coffee.controllers;

import org.coffee.domain.models.VariableOpcUa;
import org.coffee.services.VariableOpcUaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/variables/opcua")
public class VariableOpcUaController {

    private final VariableOpcUaService variableOpcUaService;

    @Autowired
    public VariableOpcUaController(VariableOpcUaService variableOpcUaService) {
        this.variableOpcUaService = variableOpcUaService;
    }

    // Cria uma nova variavel OPC-UA - POST /api/variables/opcua
    @PostMapping
    public ResponseEntity<?> createVariableOpcUa(@RequestBody VariableOpcUa variable) {
        try {
            System.out.println("==> POST /api/variables/opcua - Recebido: " + variable);
            VariableOpcUa createdVariable = variableOpcUaService.createVariableOpcUa(variable);
            System.out.println("==> Variavel OPC-UA criada com sucesso: " + createdVariable.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVariable);
        } catch (RuntimeException e) {
            System.err.println("==> ERRO ao criar variavel OPC-UA: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Retorna todas as variaveis OPC-UA - GET /api/variables/opcua
    @GetMapping
    public ResponseEntity<List<VariableOpcUa>> getAllVariablesOpcUa() {
        List<VariableOpcUa> variables = variableOpcUaService.getAllVariablesOpcUa();
        return ResponseEntity.ok(variables);
    }

    // Busca uma variavel OPC-UA por ID - GET /api/variables/opcua/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getVariableOpcUaById(@PathVariable Long id) {
        return variableOpcUaService.getVariableOpcUaById(id)
                .map(variable -> ResponseEntity.ok((Object) variable))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Variavel OPC-UA nao encontrada")));
    }

    // Busca variaveis OPC-UA por dispositivo - GET /api/variables/opcua/device/{deviceId}
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<VariableOpcUa>> getVariableOpcUaByDeviceId(@PathVariable Long deviceId) {
        List<VariableOpcUa> variables = variableOpcUaService.getVariableOpcUaByDeviceId(deviceId);
        return ResponseEntity.ok(variables);
    }

    // Busca variaveis OPC-UA por namespace index - GET /api/variables/opcua/namespace/{namespaceIndex}
    @GetMapping("/namespace/{namespaceIndex}")
    public ResponseEntity<List<VariableOpcUa>> getVariableOpcUaByNamespaceIndex(@PathVariable Integer namespaceIndex) {
        List<VariableOpcUa> variables = variableOpcUaService.getVariableOpcUaByNamespaceIndex(namespaceIndex);
        return ResponseEntity.ok(variables);
    }

    // Atualiza uma variavel OPC-UA existente - PUT /api/variables/opcua/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVariableOpcUa(@PathVariable Long id, @RequestBody VariableOpcUa variable) {
        try {
            System.out.println("==> PUT /api/variables/opcua/" + id + " - Recebido: " + variable);
            VariableOpcUa updatedVariable = variableOpcUaService.updateVariableOpcUa(id, variable);
            System.out.println("==> Variavel OPC-UA atualizada com sucesso");
            return ResponseEntity.ok(updatedVariable);
        } catch (RuntimeException e) {
            System.err.println("==> ERRO ao atualizar variavel OPC-UA: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Remove uma variavel OPC-UA - DELETE /api/variables/opcua/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVariableOpcUa(@PathVariable Long id) {
        try {
            variableOpcUaService.deleteVariableOpcUa(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}