package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.coffee.domain.dtos.PlcValueDto;
import org.coffee.domain.enums.PlcDataType;
import org.coffee.domain.models.SiemensS7Protocol;
import org.coffee.examples.api.ExampleMessageDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint generico para leitura/escrita Siemens S7 (sem persistencia).
 *
 * Para operacoes sobre variaveis cadastradas, use SiemensS7OperationController.
 *
 * Esta classe substitui completamente a versao anterior que fazia proxy HTTP
 * para um microservico Python. A implementacao agora e nativa em Java via
 * Apache PLC4X.
 */
@RestController
@RequestMapping("/siemens-s7")
@Tag(name = "Siemens S7",
     description = "Endpoints para comunicacao atraves do protocolo Siemens S7 "
                 + "(implementacao nativa via Apache PLC4X)")
public class SiemensS7Controller {

    // Defaults para CLPs Siemens mais comuns (S7-1200 / S7-1500).
    // Para S7-300 usar slot=2; para S7-400 normalmente slot=3.
    // Estes defaults aplicam apenas ao endpoint avulso /siemens-s7/read|write.
    // O fluxo de operations usa rack/slot vindos do banco (PlcSiemensS7ConfigTable).
    private static final int DEFAULT_RACK = 0;
    private static final int DEFAULT_SLOT = 1;

    @GetMapping("/read")
    @Operation(summary = "Leitura de uma variavel atraves do protocolo Siemens S7")
    @ApiResponse(responseCode = "200", description = "Valor lido com sucesso")
    public ResponseEntity<PlcValueDto> readSiemens(
            @Parameter(description = "IP do CLP", required = true)
            @RequestParam String ip,

            @Parameter(description = "Tipo de dado a ser lido", required = true)
            @RequestParam PlcDataType data_type,

            @Parameter(description = "Numero da DB da Tag", required = true)
            @RequestParam int db_number,

            @Parameter(description = "Byte offset da Tag", required = true)
            @RequestParam int offset,

            @Parameter(description = "Bit offset (usado apenas para BOOLEAN, 0-7)", required = false)
            @RequestParam(required = false, defaultValue = "0") int bit_offset,

            @Parameter(description = "Rack do CPU (default 0)", required = false)
            @RequestParam(required = false, defaultValue = "0") int rack,

            @Parameter(description = "Slot do CPU (default 1 para S7-1200/1500)", required = false)
            @RequestParam(required = false, defaultValue = "1") int slot) {

        SiemensS7Protocol protocol = new SiemensS7Protocol(ip, rack, slot);

        try {
            protocol.openConnection();

            Object value;
            switch (data_type) {
                case BOOLEAN:
                    value = protocol.readBool(db_number, offset, bit_offset);
                    break;
                case INT:
                    // Usa INT (16 bits signed) por padrao. Para DINT, usar o endpoint
                    // /api/operations/siemens-s7 com dataType="DINT" na variavel cadastrada.
                    value = protocol.readInt(db_number, offset);
                    break;
                case FLOAT:
                    value = protocol.readReal(db_number, offset);
                    break;
                case STRING:
                    value = protocol.readString(db_number, offset);
                    break;
                default:
                    return ResponseEntity.badRequest().body(
                            new PlcValueDto("Tipo de dado nao suportado: " + data_type));
            }

            PlcValueDto response = new PlcValueDto();
            response.setType(data_type);
            response.setValue(value);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            PlcValueDto errorResponse = new PlcValueDto();
            errorResponse.setValue("Erro ao ler do CLP S7: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } finally {
            protocol.closeConnection();
        }
    }

    @PostMapping("/write")
    @Operation(summary = "Escrita de uma variavel atraves do protocolo Siemens S7")
    @ApiResponse(responseCode = "200", description = "Valor escrito com sucesso")
    public ResponseEntity<ExampleMessageDto> writeSiemens(
            @Parameter(description = "IP do CLP", required = true)
            @RequestParam String ip,

            @Parameter(description = "Tipo de dado a ser escrito", required = true)
            @RequestParam PlcDataType data_type,

            @Parameter(description = "Numero da DB da Tag", required = true)
            @RequestParam int db_number,

            @Parameter(description = "Byte offset da Tag", required = true)
            @RequestParam int offset,

            @Parameter(description = "Bit offset (usado apenas para BOOLEAN, 0-7)", required = false)
            @RequestParam(required = false, defaultValue = "0") int bit_offset,

            @Parameter(description = "Valor a ser escrito", required = true)
            @RequestParam String value,

            @Parameter(description = "Rack do CPU (default 0)", required = false)
            @RequestParam(required = false, defaultValue = "0") int rack,

            @Parameter(description = "Slot do CPU (default 1 para S7-1200/1500)", required = false)
            @RequestParam(required = false, defaultValue = "1") int slot) {

        SiemensS7Protocol protocol = new SiemensS7Protocol(ip, rack, slot);

        try {
            protocol.openConnection();

            switch (data_type) {
                case BOOLEAN:
                    protocol.writeBool(db_number, offset, bit_offset, Boolean.parseBoolean(value));
                    break;
                case INT:
                    protocol.writeInt(db_number, offset, Integer.parseInt(value));
                    break;
                case FLOAT:
                    protocol.writeReal(db_number, offset, Float.parseFloat(value));
                    break;
                case STRING:
                    protocol.writeString(db_number, offset, value);
                    break;
                default:
                    return ResponseEntity.badRequest().body(
                            new ExampleMessageDto("Tipo de dado nao suportado: " + data_type));
            }

            return ResponseEntity.ok(new ExampleMessageDto("Valor escrito com sucesso"));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    new ExampleMessageDto("Formato invalido para o tipo " + data_type + ": " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ExampleMessageDto("Erro ao escrever no CLP S7: " + e.getMessage()));
        } finally {
            protocol.closeConnection();
        }
    }
}
