package org.coffee.controllers;

import org.coffee.domain.models.VariableMqtt;
import org.coffee.services.VariableMqttService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/variables/mqtt")
public class VariableMqttController {

    private final VariableMqttService variableMqttService;

    @Autowired
    public VariableMqttController(VariableMqttService variableMqttService) {
        this.variableMqttService = variableMqttService;
    }

    // Cria uma nova variavel MQTT - POST /api/variables/mqtt
    @PostMapping
    public ResponseEntity<?> createVariableMqtt(@RequestBody VariableMqtt variable) {
        try {
            System.out.println("==> POST /api/variables/mqtt - Recebido: " + variable);
            VariableMqtt createdVariable = variableMqttService.createVariableMqtt(variable);
            System.out.println("==> Variavel MQTT criada com sucesso: " + createdVariable.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVariable);
        } catch (RuntimeException e) {
            System.err.println("==> ERRO ao criar variavel MQTT: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Retorna todas as variaveis MQTT - GET /api/variables/mqtt
    @GetMapping
    public ResponseEntity<List<VariableMqtt>> getAllVariablesMqtt() {
        List<VariableMqtt> variables = variableMqttService.getAllVariablesMqtt();
        return ResponseEntity.ok(variables);
    }

    // Busca uma variavel MQTT por ID - GET /api/variables/mqtt/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getVariableMqttById(@PathVariable Long id) {
        return variableMqttService.getVariableMqttById(id)
                .map(variable -> ResponseEntity.ok((Object) variable))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Variavel MQTT nao encontrada")));
    }

    // Busca variaveis MQTT por dispositivo - GET /api/variables/mqtt/device/{deviceId}
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<VariableMqtt>> getVariableMqttByDeviceId(@PathVariable Long deviceId) {
        List<VariableMqtt> variables = variableMqttService.getVariableMqttByDeviceId(deviceId);
        return ResponseEntity.ok(variables);
    }

    // Busca variavel MQTT por topico - GET /api/variables/mqtt/topic/{topic}
    @GetMapping("/topic/{topic}")
    public ResponseEntity<?> getVariableMqttByTopic(@PathVariable String topic) {
        return variableMqttService.getVariableMqttByTopic(topic)
                .map(variable -> ResponseEntity.ok((Object) variable))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Variavel MQTT nao encontrada")));
    }

    // Busca variaveis MQTT por QoS - GET /api/variables/mqtt/qos/{qos}
    @GetMapping("/qos/{qos}")
    public ResponseEntity<List<VariableMqtt>> getVariableMqttByQos(@PathVariable Integer qos) {
        List<VariableMqtt> variables = variableMqttService.getVariableMqttByQos(qos);
        return ResponseEntity.ok(variables);
    }

    // Atualiza uma variavel MQTT existente - PUT /api/variables/mqtt/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVariableMqtt(@PathVariable Long id, @RequestBody VariableMqtt variable) {
        try {
            System.out.println("==> PUT /api/variables/mqtt/" + id + " - Recebido: " + variable);
            VariableMqtt updatedVariable = variableMqttService.updateVariableMqtt(id, variable);
            System.out.println("==> Variavel MQTT atualizada com sucesso");
            return ResponseEntity.ok(updatedVariable);
        } catch (RuntimeException e) {
            System.err.println("==> ERRO ao atualizar variavel MQTT: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Remove uma variavel MQTT - DELETE /api/variables/mqtt/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVariableMqtt(@PathVariable Long id) {
        try {
            variableMqttService.deleteVariableMqtt(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}