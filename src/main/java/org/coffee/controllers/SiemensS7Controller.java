package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.coffee.examples.api.ExampleMessageDto;
import org.coffee.domain.dtos.PlcValueDto;
import org.coffee.domain.enums.PlcDataType;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/*
 OBSERVAÇÃO IMPORTANTE
 A funcionalidade de comunicação via protocolo Siemens S7 não está nativamente contida nessa aplicação JAVA
 Existe um micro serviço Python que possui essa funcionalidade, o aplicação JAVA consome esse serviço via HTTP

 Essa funcionalidade é exclusiva para comunicação com CLPs Siemens S7
 Essa funcionalidade NÃO irá funcionar em qualquer outro dispositivo, mesmo que seja compatível com o protocolo Siemens S7
 Lembrete para testar essa funcionalidade com outro dispositivo Siemens como IHM que fale o protocolo Siemens S7 para ter certeza que só funciona com CLPs
 */

@RestController
@RequestMapping("/siemens-s7")
@Tag(name = "Siemens S7", description = "Endpoints para comunicação através do protocolo Siemens S7 (Exclusivo para comunicação com CLPs Siemens S7)")
public class SiemensS7Controller extends BasePlcController {

    @GetMapping("/read") // VERIFICAR SE ESTÁ FUNCIONANDO APÓS ALTERAR PARA GET
    @Operation(summary = "Leitura de uma variável através do protocolo Siemens S7")
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
    @Operation(summary = "Escrita de uma variável através do protocolo Siemens S7")
    @ApiResponse(responseCode = "200", description = "Valor escrito com sucesso")
    public ExampleMessageDto writeSiemens(
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
                ExampleMessageDto.class,
                HttpMethod.POST);
    }
}