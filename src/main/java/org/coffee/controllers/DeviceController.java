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

/**
 * Controlador REST para gerenciamento de dispositivos.
 * Fornece endpoints para operações CRUD e consultas sobre dispositivos.
 */
@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    /** Serviço de negócio para operações com dispositivos */
    private final DeviceService deviceService;

    /**
     * Construtor com injeção de dependência do serviço.
     *
     * @param deviceService Serviço de dispositivos a ser injetado
     */
    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * Cria um novo dispositivo no sistema.
     * Endpoint: POST /api/devices
     *
     * @param device Dados do dispositivo a ser criado
     * @return ResponseEntity contendo o dispositivo criado ou erro
     */
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

    /**
     * Retorna todos os dispositivos cadastrados.
     * Endpoint: GET /api/devices
     *
     * @return ResponseEntity contendo lista de todos os dispositivos
     */
    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        List<Device> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(devices);
    }

    /**
     * Busca um dispositivo específico por ID.
     * Endpoint: GET /api/devices/{id}
     *
     * @param id Identificador único do dispositivo
     * @return ResponseEntity contendo o dispositivo ou erro 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDeviceById(@PathVariable Long id) {
        return deviceService.getDeviceById(id)
                .map(device -> ResponseEntity.ok((Object) device))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Dispositivo nao encontrado")));
    }

    /**
     * Busca um dispositivo por nome.
     * Endpoint: GET /api/devices/name/{name}
     *
     * @param name Nome do dispositivo a ser buscado
     * @return ResponseEntity contendo o dispositivo ou erro 404
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<?> getDeviceByName(@PathVariable String name) {
        return deviceService.getDeviceByName(name)
                .map(device -> ResponseEntity.ok((Object) device))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Dispositivo nao encontrado")));
    }

    /**
     * Busca todos os dispositivos associados a um PLC específico.
     * Endpoint: GET /api/devices/plc/{plcId}
     *
     * @param plcId Identificador do PLC
     * @return ResponseEntity contendo lista de dispositivos do PLC
     */
    @GetMapping("/plc/{plcId}")
    public ResponseEntity<List<Device>> getDevicesByPlcId(@PathVariable Long plcId) {
        List<Device> devices = deviceService.getDevicesByPlcId(plcId);
        return ResponseEntity.ok(devices);
    }

    /**
     * Busca todos os dispositivos que utilizam um protocolo específico.
     * Endpoint: GET /api/devices/protocol/{protocolId}
     *
     * @param protocolId Identificador do protocolo
     * @return ResponseEntity contendo lista de dispositivos do protocolo
     */
    @GetMapping("/protocol/{protocolId}")
    public ResponseEntity<List<Device>> getDevicesByProtocolId(@PathVariable Long protocolId) {
        List<Device> devices = deviceService.getDevicesByProtocolId(protocolId);
        return ResponseEntity.ok(devices);
    }

    /**
     * Busca todos os dispositivos de um fabricante específico.
     * Endpoint: GET /api/devices/manufacturer/{manufacturer}
     *
     * @param manufacturer Nome do fabricante
     * @return ResponseEntity contendo lista de dispositivos do fabricante
     */
    @GetMapping("/manufacturer/{manufacturer}")
    public ResponseEntity<List<Device>> getDevicesByManufacturer(@PathVariable String manufacturer) {
        List<Device> devices = deviceService.getDevicesByManufacturer(manufacturer);
        return ResponseEntity.ok(devices);
    }

    /**
     * Atualiza os dados de um dispositivo existente.
     * Endpoint: PUT /api/devices/{id}
     *
     * @param id Identificador do dispositivo a ser atualizado
     * @param device Novos dados do dispositivo
     * @return ResponseEntity contendo o dispositivo atualizado ou erro
     */
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

    /**
     * Remove um dispositivo do sistema.
     * Endpoint: DELETE /api/devices/{id}
     *
     * @param id Identificador do dispositivo a ser removido
     * @return ResponseEntity vazio (204) em caso de sucesso ou erro 404
     */
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