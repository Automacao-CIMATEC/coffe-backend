package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.coffee.examples.api.ExampleMessageDto;
import org.coffee.domain.dtos.PlcValueDto;
import org.coffee.domain.enums.PlcDataType;
import org.coffee.domain.abstracts.AbstractProtocol;
import org.coffee.domain.models.ModbusProtocol;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/modbus-tcp")
@Tag(name = "Modbus/TCP", description = "Endpoints para comunicação através do protocolo Modbus/TCP")
public class ModbusController {

    // Default Modbus port
    private static final int MODBUS_PORT = 502;
    // Default Modbus unit ID
    private static final int DEFAULT_UNIT_ID = 1;

    @GetMapping("/read")
    @Operation(summary = "Leitura de uma variável através do protocolo Modbus/TCP")
    @ApiResponse(responseCode = "200", description = "Variável lida com sucesso")
    public ResponseEntity<PlcValueDto> readModbus(
            @Parameter(description = "IP do CLP", required = true)
            @RequestParam String ip,

            @Parameter(description = "Tipo de dado a ser lido", required = true)
            @RequestParam PlcDataType data_type,

            @Parameter(description = "Endereço Modbus (coil ou register)", required = true)
            @RequestParam int address,

            @Parameter(description = "Comprimento em registros (apenas para strings)")
            @RequestParam(required = false) Integer length) {

        try {
            // Create Modbus protocol instance
            AbstractProtocol protocol = new ModbusProtocol(ip, MODBUS_PORT, DEFAULT_UNIT_ID);

            // Connect to PLC
            protocol.openConnection();

            Object value;
            switch (data_type) {
                case BOOLEAN:
                    value = protocol.readDataBoolean(address);
                    break;
                case INT:
                    value = protocol.readDataInt(address);
                    break;
                case FLOAT:
                    value = protocol.readDataFloat(address);
                    break;
                case STRING:
                    if (length == null) {
                        return ResponseEntity.badRequest().body(
                                new PlcValueDto("Length parameter is required for STRING data type"));
                    }
                    value = protocol.readDataString(address, length);
                    break;
                default:
                    return ResponseEntity.badRequest().body(
                            new PlcValueDto("Unsupported data type: " + data_type));
            }

            // PROPERLY CONSTRUCT THE RESPONSE DTO
            PlcValueDto response = new PlcValueDto();
            response.setType(data_type);  // Set the data type
            response.setValue(value);     // Set the actual value

            // Close connection
            protocol.closeConnection();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Create error response properly
            PlcValueDto errorResponse = new PlcValueDto();
            errorResponse.setValue("Error reading from PLC: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/write")
    @Operation(summary = "Escrita de uma variável através do protocolo Modbus/TCP")
    @ApiResponse(responseCode = "200", description = "Variável escrita com sucesso")
    public ResponseEntity<ExampleMessageDto> writeModbus(
            @Parameter(description = "IP do CLP", required = true)
            @RequestParam String ip,

            @Parameter(description = "Tipo de dado a ser escrito", required = true)
            @RequestParam PlcDataType data_type,

            @Parameter(description = "Valor a ser escrito", required = true)
            @RequestParam String value,

            @Parameter(description = "Endereço Modbus (coil ou register)", required = true)
            @RequestParam int address) {

        try {
            // Create Modbus protocol instance
            AbstractProtocol protocol = new ModbusProtocol(ip, MODBUS_PORT, DEFAULT_UNIT_ID);

            // Connect to PLC
            protocol.openConnection();

            // Write value based on data type
            switch (data_type) {
                case BOOLEAN:
                    boolean boolValue = Boolean.parseBoolean(value);
                    protocol.writeData(address, boolValue);
                    break;
                case INT:
                    int intValue = Integer.parseInt(value);
                    protocol.writeData(address, intValue);
                    break;
                case FLOAT:
                    float floatValue = Float.parseFloat(value);
                    protocol.writeData(address, floatValue);
                    break;
                case STRING:
                    protocol.writeData(address, value);
                    break;
                default:
                    return ResponseEntity.badRequest().body(
                            new ExampleMessageDto("Unsupported data type: " + data_type));
            }

            // Close connection
            protocol.closeConnection();

            return ResponseEntity.ok(new ExampleMessageDto("Value written successfully"));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    new ExampleMessageDto("Invalid value format for data type: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ExampleMessageDto("Error writing to PLC: " + e.getMessage()));
        }
    }
}