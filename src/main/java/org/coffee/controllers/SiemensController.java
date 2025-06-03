package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.coffee.domain.dtos.MessageDto;
import org.coffee.domain.dtos.PlcValueDto;
import org.coffee.domain.enums.PlcDataType;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/profinet")
@Tag(name = "Profinet", description = "Endpoints para comunicação através do protocolo Profinet (Exclusivo para comunicação com CLPs Siemens S7)")
public class SiemensController extends BasePlcController {

    @PostMapping("/read")
    @Operation(summary = "Leitura de uma variável através do protocolo Profinet")
    @ApiResponse(responseCode = "200", description = "Valor lido com sucesso")
    public PlcValueDto readSiemens(
            @Parameter(description = "IP do CLP", required = true)
            @RequestParam String ip,

            @Parameter(description = "Tipo de dado a ser lido", required = true)
            @RequestParam PlcDataType data_type,

            @Parameter(description = "Número da DB da Tag", required = true)
            @RequestParam int db_number,

            @Parameter(description = "Byte offset da Tag", required = true)
            @RequestParam int offset,

            @Parameter(description = "Bit offset do offset da Tag", required = true)
            @RequestParam int bit_offset) {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ip", ip);
        requestBody.put("data_type", data_type.name());

        Map<String, Object> tag = new HashMap<>();
        tag.put("db_number", db_number);
        tag.put("offset", offset);
        tag.put("bit_offset", bit_offset);
        requestBody.put("tag", tag);

        return proxyRequest(plcServiceUrl + "/plc/siemens/read",
                requestBody,
                PlcValueDto.class,
                HttpMethod.POST);
    }

    @PostMapping("/write")
    @Operation(summary = "Escrita de uma variável através do protocolo Profinet")
    @ApiResponse(responseCode = "200", description = "Valor escrito com sucesso")
    public MessageDto writeSiemens(
            @Parameter(description = "IP do CLP", required = true)
            @RequestParam String ip,

            @Parameter(description = "Tipo de dado a ser escrito", required = true)
            @RequestParam PlcDataType data_type,

            @Parameter(description = "Número da DB da Tag", required = true)
            @RequestParam int db_number,

            @Parameter(description = "Byte offset da Tag", required = true)
            @RequestParam int offset,

            @Parameter(description = "Bit offset do offset da Tag", required = true)
            @RequestParam int bit_offset,

            @Parameter(description = "Valor a ser escrito", required = true)
            @RequestParam String value) {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ip", ip);
        requestBody.put("data_type", data_type.name());
        requestBody.put("value", value);

        Map<String, Object> tag = new HashMap<>();
        tag.put("db_number", db_number);
        tag.put("offset", offset);
        tag.put("bit_offset", bit_offset);
        requestBody.put("tag", tag);

        return proxyRequest(plcServiceUrl + "/plc/siemens/write",
                requestBody,
                MessageDto.class,
                HttpMethod.POST);
    }
}