package org.coffee.controllers;

import org.coffee.domain.models.VariableModbus;
import org.coffee.services.VariableModbusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/variables/modbus")
public class VariableModbusController {

    private final VariableModbusService variableModbusService;

    @Autowired
    public VariableModbusController(VariableModbusService variableModbusService) {
        this.variableModbusService = variableModbusService;
    }

    // Cria uma nova variavel Modbus - POST /api/variables/modbus
    @PostMapping
    public ResponseEntity<?> createVariableModbus(@RequestBody VariableModbus variable) {
        try {
            System.out.println("==> POST /api/variables/modbus - Recebido: " + variable);
            VariableModbus createdVariable = variableModbusService.createVariableModbus(variable);
            System.out.println("==> Variavel Modbus criada com sucesso: " + createdVariable.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVariable);
        } catch (RuntimeException e) {
            System.err.println("==> ERRO ao criar variavel Modbus: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Retorna todas as variaveis Modbus - GET /api/variables/modbus
    @GetMapping
    public ResponseEntity<List<VariableModbus>> getAllVariablesModbus() {
        List<VariableModbus> variables = variableModbusService.getAllVariablesModbus();
        return ResponseEntity.ok(variables);
    }

    // Busca uma variavel Modbus por ID - GET /api/variables/modbus/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getVariableModbusById(@PathVariable Long id) {
        return variableModbusService.getVariableModbusById(id)
                .map(variable -> ResponseEntity.ok((Object) variable))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Variavel Modbus nao encontrada")));
    }

    // Busca variaveis Modbus por dispositivo - GET /api/variables/modbus/device/{deviceId}
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<VariableModbus>> getVariableModbusByDeviceId(@PathVariable Long deviceId) {
        List<VariableModbus> variables = variableModbusService.getVariableModbusByDeviceId(deviceId);
        return ResponseEntity.ok(variables);
    }

    // Busca variaveis Modbus por tipo de registrador - GET /api/variables/modbus/register-type/{registerType}
    @GetMapping("/register-type/{registerType}")
    public ResponseEntity<List<VariableModbus>> getVariableModbusByRegisterType(@PathVariable String registerType) {
        List<VariableModbus> variables = variableModbusService.getVariableModbusByRegisterType(registerType);
        return ResponseEntity.ok(variables);
    }

    // Atualiza uma variavel Modbus existente - PUT /api/variables/modbus/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVariableModbus(@PathVariable Long id, @RequestBody VariableModbus variable) {
        try {
            System.out.println("==> PUT /api/variables/modbus/" + id + " - Recebido: " + variable);
            VariableModbus updatedVariable = variableModbusService.updateVariableModbus(id, variable);
            System.out.println("==> Variavel Modbus atualizada com sucesso");
            return ResponseEntity.ok(updatedVariable);
        } catch (RuntimeException e) {
            System.err.println("==> ERRO ao atualizar variavel Modbus: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Remove uma variavel Modbus - DELETE /api/variables/modbus/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVariableModbus(@PathVariable Long id) {
        try {
            variableModbusService.deleteVariableModbus(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}