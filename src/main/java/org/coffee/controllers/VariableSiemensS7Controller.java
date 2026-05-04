package org.coffee.controllers;

import org.coffee.domain.models.VariableSiemensS7;
import org.coffee.services.VariableSiemensS7Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/variables/siemens-s7")
public class VariableSiemensS7Controller {

    private final VariableSiemensS7Service variableSiemensS7Service;

    @Autowired
    public VariableSiemensS7Controller(VariableSiemensS7Service variableSiemensS7Service) {
        this.variableSiemensS7Service = variableSiemensS7Service;
    }

    // Cria uma nova variavel Siemens S7 - POST /api/variables/siemens-s7
    @PostMapping
    public ResponseEntity<?> createVariableSiemensS7(@RequestBody VariableSiemensS7 variable) {
        try {
            System.out.println("==> POST /api/variables/siemens-s7 - Recebido: " + variable);
            VariableSiemensS7 createdVariable = variableSiemensS7Service.createVariableSiemensS7(variable);
            System.out.println("==> Variavel Siemens S7 criada com sucesso: " + createdVariable.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVariable);
        } catch (RuntimeException e) {
            System.err.println("==> ERRO ao criar variavel Siemens S7: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Retorna todas as variaveis Siemens S7 - GET /api/variables/siemens-s7
    @GetMapping
    public ResponseEntity<List<VariableSiemensS7>> getAllVariablesSiemensS7() {
        List<VariableSiemensS7> variables = variableSiemensS7Service.getAllVariablesSiemensS7();
        return ResponseEntity.ok(variables);
    }

    // Busca uma variavel Siemens S7 por ID - GET /api/variables/siemens-s7/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getVariableSiemensS7ById(@PathVariable Long id) {
        return variableSiemensS7Service.getVariableSiemensS7ById(id)
                .map(variable -> ResponseEntity.ok((Object) variable))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Variavel Siemens S7 nao encontrada")));
    }

    // Busca variaveis Siemens S7 por dispositivo - GET /api/variables/siemens-s7/device/{deviceId}
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<VariableSiemensS7>> getVariableSiemensS7ByDeviceId(@PathVariable Long deviceId) {
        List<VariableSiemensS7> variables = variableSiemensS7Service.getVariableSiemensS7ByDeviceId(deviceId);
        return ResponseEntity.ok(variables);
    }

    // Busca variaveis Siemens S7 por tipo de dados - GET /api/variables/siemens-s7/data-type/{dataType}
    @GetMapping("/data-type/{dataType}")
    public ResponseEntity<List<VariableSiemensS7>> getVariableSiemensS7ByDataType(@PathVariable String dataType) {
        List<VariableSiemensS7> variables = variableSiemensS7Service.getVariableSiemensS7ByDataType(dataType);
        return ResponseEntity.ok(variables);
    }

    // Busca variaveis por numero da DB - GET /api/variables/siemens-s7/db/{dbNumber}
    @GetMapping("/db/{dbNumber}")
    public ResponseEntity<List<VariableSiemensS7>> getVariableSiemensS7ByDbNumber(@PathVariable Integer dbNumber) {
        List<VariableSiemensS7> variables = variableSiemensS7Service.getVariableSiemensS7ByDbNumber(dbNumber);
        return ResponseEntity.ok(variables);
    }

    // Atualiza uma variavel Siemens S7 existente - PUT /api/variables/siemens-s7/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVariableSiemensS7(@PathVariable Long id, @RequestBody VariableSiemensS7 variable) {
        try {
            System.out.println("==> PUT /api/variables/siemens-s7/" + id + " - Recebido: " + variable);
            VariableSiemensS7 updatedVariable = variableSiemensS7Service.updateVariableSiemensS7(id, variable);
            System.out.println("==> Variavel Siemens S7 atualizada com sucesso");
            return ResponseEntity.ok(updatedVariable);
        } catch (RuntimeException e) {
            System.err.println("==> ERRO ao atualizar variavel Siemens S7: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Remove uma variavel Siemens S7 - DELETE /api/variables/siemens-s7/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVariableSiemensS7(@PathVariable Long id) {
        try {
            variableSiemensS7Service.deleteVariableSiemensS7(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}