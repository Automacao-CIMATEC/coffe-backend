package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.coffee.domain.models.database.PlcSiemensS7ConfigTable;
import org.coffee.domain.models.database.PlcTable;
import org.coffee.repository.PlcRepository;
import org.coffee.repository.PlcSiemensS7ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * CRUD da configuracao Siemens S7 (rack/slot) de um PLC.
 *
 * Esta config e necessaria antes de ler/escrever variaveis S7 num CLP.
 * Implementada como recurso aninhado em /api/plc/{plcId}/siemens-s7-config
 * porque a relacao logica e 1:1 com PLC.
 */
@RestController
@RequestMapping("/api/plc/{plcId}/siemens-s7-config")
@Tag(name = "Siemens S7 Config", description = "Configuracao S7 (rack/slot) por PLC")
public class PlcSiemensS7ConfigController {

    private final PlcRepository plcRepository;
    private final PlcSiemensS7ConfigRepository configRepository;

    @Autowired
    public PlcSiemensS7ConfigController(PlcRepository plcRepository,
                                        PlcSiemensS7ConfigRepository configRepository) {
        this.plcRepository = plcRepository;
        this.configRepository = configRepository;
    }

    // Cria a config S7 para o PLC informado
    @PostMapping
    @Operation(summary = "Cria a configuracao Siemens S7 do PLC")
    @ApiResponse(responseCode = "201", description = "Configuracao criada com sucesso")
    public ResponseEntity<?> create(
            @Parameter(description = "ID do PLC", required = true)
            @PathVariable Long plcId,
            @RequestBody S7ConfigRequest body) {
        try {
            PlcTable plc = plcRepository.findById(plcId)
                    .orElseThrow(() -> new RuntimeException("PLC nao encontrado"));

            if (configRepository.existsByPlcId(plcId)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "PLC ja possui configuracao S7. Use PUT para atualizar.");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            validate(body);

            PlcSiemensS7ConfigTable config = new PlcSiemensS7ConfigTable(plc, body.rack, body.slot);
            PlcSiemensS7ConfigTable saved = configRepository.save(config);

            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));

        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    // Le a config S7 do PLC
    @GetMapping
    @Operation(summary = "Le a configuracao Siemens S7 do PLC")
    public ResponseEntity<?> get(@PathVariable Long plcId) {
        return configRepository.findByPlcId(plcId)
                .map(c -> ResponseEntity.ok((Object) toResponse(c)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Config S7 nao encontrada para o PLC " + plcId)));
    }

    // Atualiza a config S7 do PLC (rack/slot)
    @PutMapping
    @Operation(summary = "Atualiza a configuracao Siemens S7 do PLC")
    public ResponseEntity<?> update(
            @PathVariable Long plcId,
            @RequestBody S7ConfigRequest body) {
        try {
            PlcSiemensS7ConfigTable config = configRepository.findByPlcId(plcId)
                    .orElseThrow(() -> new RuntimeException(
                            "Config S7 nao encontrada para o PLC " + plcId));

            validate(body);
            config.setRack(body.rack);
            config.setSlot(body.slot);
            PlcSiemensS7ConfigTable saved = configRepository.save(config);
            return ResponseEntity.ok(toResponse(saved));
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    // Remove a config S7 do PLC
    @DeleteMapping
    @Operation(summary = "Remove a configuracao Siemens S7 do PLC")
    public ResponseEntity<?> delete(@PathVariable Long plcId) {
        PlcSiemensS7ConfigTable config = configRepository.findByPlcId(plcId)
                .orElse(null);
        if (config == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Config S7 nao encontrada para o PLC " + plcId));
        }
        configRepository.delete(config);
        return ResponseEntity.noContent().build();
    }

    // Helpers
    private void validate(S7ConfigRequest body) {
        if (body == null) {
            throw new RuntimeException("Corpo da requisicao ausente");
        }
        if (body.rack == null || body.rack < 0) {
            throw new RuntimeException("Rack invalido. Deve ser >= 0");
        }
        if (body.slot == null || body.slot < 0) {
            throw new RuntimeException("Slot invalido. Deve ser >= 0");
        }
    }

    private Map<String, Object> toResponse(PlcSiemensS7ConfigTable c) {
        Map<String, Object> r = new HashMap<>();
        r.put("id", c.getId());
        r.put("plcId", c.getPlc().getId());
        r.put("rack", c.getRack());
        r.put("slot", c.getSlot());
        r.put("createdAt", c.getCreatedAt());
        r.put("updatedAt", c.getUpdatedAt());
        return r;
    }

    private ResponseEntity<?> badRequest(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * DTO de entrada para criar/atualizar a config S7.
     * Inner static class porque so faz sentido neste contexto.
     */
    public static class S7ConfigRequest {
        public Integer rack;
        public Integer slot;
    }
}
