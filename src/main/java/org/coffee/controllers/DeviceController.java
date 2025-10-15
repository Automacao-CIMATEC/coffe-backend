package org.coffee.controllers;

import org.coffee.domain.models.Device;
import org.coffee.services.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    // Cria um novo dispositivo - POST /api/devices
    @PostMapping
    public ResponseEntity<?> createDevice(@RequestBody Device device) {
        try {
            System.out.println("==> POST /api/devices - Recebido: " + device);
            Device createdDevice = deviceService.createDevice(device);
            System.out.println("==> Dispositivo criado com sucesso: " + createdDevice.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDevice);
        } catch (RuntimeException e) {
            System.err.println("==> ERRO ao criar dispositivo: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Retorna todos os dispositivos - GET /api/devices
    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        List<Device> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(devices);
    }

    // Busca um dispositivo por ID - GET /api/devices/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getDeviceById(@PathVariable Long id) {
        return deviceService.getDeviceById(id)
                .map(device -> ResponseEntity.ok((Object) device))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Dispositivo nao encontrado")));
    }

    // Busca um dispositivo por nome - GET /api/devices/name/{name}
    @GetMapping("/name/{name}")
    public ResponseEntity<?> getDeviceByName(@PathVariable String name) {
        return deviceService.getDeviceByName(name)
                .map(device -> ResponseEntity.ok((Object) device))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Dispositivo nao encontrado")));
    }

    // Busca dispositivos por PLC - GET /api/devices/plc/{plcId}
    @GetMapping("/plc/{plcId}")
    public ResponseEntity<List<Device>> getDevicesByPlcId(@PathVariable Long plcId) {
        List<Device> devices = deviceService.getDevicesByPlcId(plcId);
        return ResponseEntity.ok(devices);
    }

    // Busca dispositivos por protocolo - GET /api/devices/protocol/{protocolId}
    @GetMapping("/protocol/{protocolId}")
    public ResponseEntity<List<Device>> getDevicesByProtocolId(@PathVariable Long protocolId) {
        List<Device> devices = deviceService.getDevicesByProtocolId(protocolId);
        return ResponseEntity.ok(devices);
    }

    // Busca dispositivos por fabricante - GET /api/devices/manufacturer/{manufacturer}
    @GetMapping("/manufacturer/{manufacturer}")
    public ResponseEntity<List<Device>> getDevicesByManufacturer(@PathVariable String manufacturer) {
        List<Device> devices = deviceService.getDevicesByManufacturer(manufacturer);
        return ResponseEntity.ok(devices);
    }

    // Atualiza um dispositivo existente - PUT /api/devices/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDevice(@PathVariable Long id, @RequestBody Device device) {
        try {
            System.out.println("==> PUT /api/devices/" + id + " - Recebido: " + device);
            Device updatedDevice = deviceService.updateDevice(id, device);
            System.out.println("==> Dispositivo atualizado com sucesso");
            return ResponseEntity.ok(updatedDevice);
        } catch (RuntimeException e) {
            System.err.println("==> ERRO ao atualizar dispositivo: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Remove um dispositivo - DELETE /api/devices/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDevice(@PathVariable Long id) {
        try {
            deviceService.deleteDevice(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}