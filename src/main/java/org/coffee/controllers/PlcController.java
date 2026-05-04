package org.coffee.controllers;

import org.coffee.domain.models.Plc;
import org.coffee.services.PlcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plcs")
public class PlcController {

    private final PlcService plcService;

    @Autowired
    public PlcController(PlcService plcService) {
        this.plcService = plcService;
    }

    // Cria um novo PLC - POST /api/plcs
    @PostMapping
    public ResponseEntity<?> createPlc(@RequestBody Plc plc) {
        try {
            System.out.println("==> POST /api/plcs - Recebido: " + plc);
            Plc createdPlc = plcService.createPlc(plc);
            System.out.println("==> PLC criado com sucesso: " + createdPlc.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPlc);
        } catch (RuntimeException e) {
            System.err.println("==> ERRO ao criar PLC: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Retorna todos os PLCs - GET /api/plcs
    @GetMapping
    public ResponseEntity<List<Plc>> getAllPlcs() {
        List<Plc> plcs = plcService.getAllPlcs();
        return ResponseEntity.ok(plcs);
    }

    // Busca um PLC por ID - GET /api/plcs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlcById(@PathVariable Long id) {
        return plcService.getPlcById(id)
                .map(plc -> ResponseEntity.ok((Object) plc))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "PLC nao encontrado")));
    }

    // Busca um PLC por nome - GET /api/plcs/name/{name}
    @GetMapping("/name/{name}")
    public ResponseEntity<?> getPlcByName(@PathVariable String name) {
        return plcService.getPlcByName(name)
                .map(plc -> ResponseEntity.ok((Object) plc))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "PLC nao encontrado")));
    }

    // Busca PLCs por fabricante - GET /api/plcs/manufacturer/{manufacturer}
    @GetMapping("/manufacturer/{manufacturer}")
    public ResponseEntity<List<Plc>> getPlcsByManufacturer(@PathVariable String manufacturer) {
        List<Plc> plcs = plcService.getPlcsByManufacturer(manufacturer);
        return ResponseEntity.ok(plcs);
    }

    // Busca PLCs por IP - GET /api/plcs/ip/{ip}
    @GetMapping("/ip/{ip}")
    public ResponseEntity<List<Plc>> getPlcsByIp(@PathVariable String ip) {
        List<Plc> plcs = plcService.getPlcsByIp(ip);
        return ResponseEntity.ok(plcs);
    }

    // Atualiza um PLC existente - PUT /api/plcs/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlc(@PathVariable Long id, @RequestBody Plc plc) {
        try {
            System.out.println("==> PUT /api/plcs/" + id + " - Recebido: " + plc);
            Plc updatedPlc = plcService.updatePlc(id, plc);
            System.out.println("==> PLC atualizado com sucesso");
            return ResponseEntity.ok(updatedPlc);
        } catch (RuntimeException e) {
            System.err.println("==> ERRO ao atualizar PLC: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Remove um PLC - DELETE /api/plcs/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlc(@PathVariable Long id) {
        try {
            plcService.deletePlc(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}