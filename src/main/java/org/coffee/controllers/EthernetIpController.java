package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.coffee.domain.dtos.PlcValueDto;
import org.coffee.domain.enums.PlcDataType;
import org.coffee.domain.models.EthernetIpProtocol;
import org.coffee.examples.api.ExampleMessageDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para comunicacao direta via protocolo Ethernet/IP.
 *
 * Exclusivo para CLPs Rockwell: CompactLogix, ControlLogix e MicroLogix.
 *
 * Implementacao nativa em Java via biblioteca etherip (ornl-epics/etherip),
 * sem dependencia de microsservico externo.
 */
@RestController
@RequestMapping("/ethernet-ip")
@Tag(name = "Ethernet/IP", description = "Endpoints para comunicacao via protocolo Ethernet/IP (exclusivo para CLPs Rockwell CompactLogix, ControlLogix e MicroLogix)")
public class EthernetIpController {

    @GetMapping("/read")
    @Operation(summary = "Leitura de uma variavel via protocolo Ethernet/IP")
    @ApiResponse(responseCode = "200", description = "Variavel lida com sucesso")
    @ApiResponse(responseCode = "500", description = "Erro de comunicacao com o CLP")
    public ResponseEntity<PlcValueDto> readRockwell(
            @Parameter(description = "IP do CLP", required = true)
            @RequestParam String ip,

            @Parameter(description = "Tipo de dado a ser lido", required = true)
            @RequestParam PlcDataType data_type,

            @Parameter(description = "Nome da tag no CLP (ex: MinhaTag, Estrutura.Membro, MinhaTag.0, Array[0])")
            @RequestParam(required = false) String tag_name,

            @Parameter(description = "Slot do controlador no chassi (padrao: 0)")
            @RequestParam(required = false, defaultValue = "0") int slot) {

        // Abre conexao, le a tag e encerra a conexao em seguida
        EthernetIpProtocol protocol = new EthernetIpProtocol(ip, slot);
        try {
            protocol.openConnection();

            Object value;
            switch (data_type) {
                case BOOLEAN:
                case BOOL:
                    value = protocol.readBool(tag_name);
                    break;
                case INT:
                    value = protocol.readInt(tag_name);
                    break;
                case FLOAT:
                    value = protocol.readReal(tag_name);
                    break;
                case STRING:
                    value = protocol.readString(tag_name);
                    break;
                default:
                    PlcValueDto erro = new PlcValueDto("Tipo de dado nao suportado: " + data_type);
                    return ResponseEntity.badRequest().body(erro);
            }

            PlcValueDto response = new PlcValueDto();
            response.setType(data_type);
            response.setValue(value);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("==> Erro na leitura Ethernet/IP: " + e.getMessage());
            PlcValueDto erro = new PlcValueDto("Erro ao ler tag '" + tag_name + "': " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        } finally {
            // Garante que a conexao seja sempre encerrada
            protocol.closeConnection();
        }
    }

    @PostMapping("/write")
    @Operation(summary = "Escrita de uma variavel via protocolo Ethernet/IP")
    @ApiResponse(responseCode = "200", description = "Variavel escrita com sucesso")
    @ApiResponse(responseCode = "400", description = "Formato de valor invalido para o tipo informado")
    @ApiResponse(responseCode = "500", description = "Erro de comunicacao com o CLP")
    public ResponseEntity<ExampleMessageDto> writeRockwell(
            @Parameter(description = "IP do CLP", required = true)
            @RequestParam String ip,

            @Parameter(description = "Tipo de dado a ser escrito", required = true)
            @RequestParam PlcDataType data_type,

            @Parameter(description = "Valor a ser escrito", required = true)
            @RequestParam String value,

            @Parameter(description = "Nome da tag no CLP (ex: MinhaTag, Estrutura.Membro, MinhaTag.0)")
            @RequestParam(required = false) String tag_name,

            @Parameter(description = "Slot do controlador no chassi (padrao: 0)")
            @RequestParam(required = false, defaultValue = "0") int slot) {

        EthernetIpProtocol protocol = new EthernetIpProtocol(ip, slot);
        try {
            protocol.openConnection();

            switch (data_type) {
                case BOOLEAN:
                case BOOL:
                    // Aceita true/false ou 1/0
                    boolean boolValue = value.equalsIgnoreCase("true") || value.equals("1");
                    protocol.writeBool(tag_name, boolValue);
                    break;
                case INT:
                    protocol.writeInt(tag_name, Integer.parseInt(value));
                    break;
                case FLOAT:
                    protocol.writeReal(tag_name, Float.parseFloat(value));
                    break;
                case STRING:
                    protocol.writeString(tag_name, value);
                    break;
                default:
                    return ResponseEntity.badRequest()
                            .body(new ExampleMessageDto("Tipo de dado nao suportado: " + data_type));
            }

            return ResponseEntity.ok(new ExampleMessageDto(
                    "Tag '" + tag_name + "' escrita com sucesso. Valor: " + value));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(new ExampleMessageDto("Formato de valor invalido para o tipo " + data_type + ": " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("==> Erro na escrita Ethernet/IP: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ExampleMessageDto("Erro ao escrever tag '" + tag_name + "': " + e.getMessage()));
        } finally {
            protocol.closeConnection();
        }
    }
}
