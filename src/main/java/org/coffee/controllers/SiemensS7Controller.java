package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.plc4x.java.DefaultPlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.coffee.examples.api.ExampleMessageDto;
import org.coffee.domain.dtos.PlcValueDto;
import org.coffee.domain.enums.PlcDataType;
import org.coffee.domain.models.SiemensS7Protocol;
import org.coffee.services.SiemensS7DataTypeDispatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint avulso para leitura/escrita Siemens S7 sem cadastro.
 *
 * Util para diagnostico rapido / testes manuais. Cada chamada abre e
 * fecha uma conexao S7 - operacao cara (~100ms de handshake), entao
 * NAO use este endpoint para cenarios de alta frequencia. Para isso,
 * cadastre o device e use os endpoints de /api/operations/siemens-s7
 * com /connect explicito.
 */
@RestController
@RequestMapping("/siemens-s7")
@Tag(name = "Siemens S7",
        description = "Endpoints avulsos S7 (cada chamada abre/fecha conexao - use para diagnostico)")
public class SiemensS7Controller {

    @GetMapping("/read")
    @Operation(summary = "Leitura avulsa de variavel Siemens S7 (abre e fecha conexao)")
    @ApiResponse(responseCode = "200", description = "Valor lido com sucesso")
    public ResponseEntity<PlcValueDto> readSiemens(
            @Parameter(description = "IP do CLP", required = true) @RequestParam String ip,
            @Parameter(description = "Tipo de dado a ser lido", required = true) @RequestParam PlcDataType data_type,
            @Parameter(description = "Numero da DB da Tag", required = true) @RequestParam int db_number,
            @Parameter(description = "Byte offset da Tag", required = true) @RequestParam int offset,
            @Parameter(description = "Bit offset (BOOL apenas, 0-7)") @RequestParam(required = false, defaultValue = "0") int bit_offset,
            @Parameter(description = "Rack do CPU (default 0)") @RequestParam(required = false, defaultValue = "0") int rack,
            @Parameter(description = "Slot do CPU (default 1 para S7-1200/1500)") @RequestParam(required = false, defaultValue = "1") int slot) {

        try (PlcConnection connection = openS7Connection(ip, rack, slot)) {
            SiemensS7Protocol protocol = new SiemensS7Protocol(ip, rack, slot, connection);

            // O enum PlcDataType tem nomes (BOOLEAN, INT, FLOAT, STRING) que sao
            // sinonimos aceitos pelo dispatcher (que tambem aceita BOOL, REAL, etc).
            Object value = SiemensS7DataTypeDispatcher.read(
                    protocol, data_type.name(), db_number, offset, bit_offset);

            PlcValueDto response = new PlcValueDto();
            response.setType(data_type);
            response.setValue(value);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new PlcValueDto(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PlcValueDto("Erro ao ler do CLP S7: " + e.getMessage()));
        }
    }

    @PostMapping("/write")
    @Operation(summary = "Escrita avulsa de variavel Siemens S7 (abre e fecha conexao)")
    @ApiResponse(responseCode = "200", description = "Valor escrito com sucesso")
    public ResponseEntity<ExampleMessageDto> writeSiemens(
            @Parameter(description = "IP do CLP", required = true) @RequestParam String ip,
            @Parameter(description = "Tipo de dado a ser escrito", required = true) @RequestParam PlcDataType data_type,
            @Parameter(description = "Numero da DB da Tag", required = true) @RequestParam int db_number,
            @Parameter(description = "Byte offset da Tag", required = true) @RequestParam int offset,
            @Parameter(description = "Bit offset (BOOL apenas, 0-7)") @RequestParam(required = false, defaultValue = "0") int bit_offset,
            @Parameter(description = "Valor a ser escrito", required = true) @RequestParam String value,
            @Parameter(description = "Rack do CPU (default 0)") @RequestParam(required = false, defaultValue = "0") int rack,
            @Parameter(description = "Slot do CPU (default 1 para S7-1200/1500)") @RequestParam(required = false, defaultValue = "1") int slot) {

        try (PlcConnection connection = openS7Connection(ip, rack, slot)) {
            SiemensS7Protocol protocol = new SiemensS7Protocol(ip, rack, slot, connection);

            SiemensS7DataTypeDispatcher.write(
                    protocol, data_type.name(), db_number, offset, bit_offset, value);

            return ResponseEntity.ok(new ExampleMessageDto("Valor escrito com sucesso"));

        } catch (IllegalArgumentException e) {
            // Cobre tanto tipo nao suportado quanto NumberFormatException (subclasse)
            return ResponseEntity.badRequest().body(new ExampleMessageDto(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ExampleMessageDto("Erro ao escrever no CLP S7: " + e.getMessage()));
        }
    }

    /**
     * Abre uma conexao S7 standalone (sem usar o registry).
     *
     * Usa "s7-light" em vez de "s7" pelo mesmo motivo do SiemensS7ConnectionRegistry:
     * o driver padrao tem bug conhecido sob carga concorrente (issue apache/plc4x #2394).
     * O controller-type=S7_1200 esta hardcoded pelo mesmo motivo - so esse modelo
     * no kit atual.
     */
    private PlcConnection openS7Connection(String ip, int rack, int slot) throws Exception {
        String connectionString = String.format(
                "s7-light://%s?remote-rack=%d&remote-slot=%d&controller-type=S7_1200",
                ip, rack, slot);
        return new DefaultPlcDriverManager()
                .getConnectionManager()
                .getConnection(connectionString);
    }
}
