package org.coffee.controllers;

import org.coffee.domain.models.Protocol;
import org.coffee.services.ProtocolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/protocols")
public class ProtocolController {

    private final ProtocolService protocolService;

    @Autowired
    public ProtocolController(ProtocolService protocolService) {
        this.protocolService = protocolService;
    }

    // Cria um novo protocolo - POST /api/protocols
    @PostMapping
    public ResponseEntity<?> createProtocol(@RequestBody Protocol protocol) {
        try {
            System.out.println("==> POST /api/protocols - Recebido: " + protocol);
            Protocol createdProtocol = protocolService.createProtocol(protocol);
            System.out.println("==> Protocolo criado com sucesso: " + createdProtocol.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProtocol);
        } catch (RuntimeException e) {
            System.err.println("==> ERRO ao criar protocolo: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Retorna todos os protocolos - GET /api/protocols
    @GetMapping
    public ResponseEntity<List<Protocol>> getAllProtocols() {
        List<Protocol> protocols = protocolService.getAllProtocols();
        return ResponseEntity.ok(protocols);
    }

    // Busca um protocolo por ID - GET /api/protocols/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getProtocolById(@PathVariable Long id) {
        return protocolService.getProtocolById(id)
                .map(protocol -> ResponseEntity.ok((Object) protocol))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Protocolo nao encontrado")));
    }

    // Busca um protocolo por nome - GET /api/protocols/name/{name}
    @GetMapping("/name/{name}")
    public ResponseEntity<?> getProtocolByName(@PathVariable String name) {
        return protocolService.getProtocolByName(name)
                .map(protocol -> ResponseEntity.ok((Object) protocol))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Protocolo nao encontrado")));
    }

    // Atualiza um protocolo existente - PUT /api/protocols/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProtocol(@PathVariable Long id, @RequestBody Protocol protocol) {
        try {
            System.out.println("==> PUT /api/protocols/" + id + " - Recebido: " + protocol);
            Protocol updatedProtocol = protocolService.updateProtocol(id, protocol);
            System.out.println("==> Protocolo atualizado com sucesso");
            return ResponseEntity.ok(updatedProtocol);
        } catch (RuntimeException e) {
            System.err.println("==> ERRO ao atualizar protocolo: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Remove um protocolo - DELETE /api/protocols/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProtocol(@PathVariable Long id) {
        try {
            protocolService.deleteProtocol(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}