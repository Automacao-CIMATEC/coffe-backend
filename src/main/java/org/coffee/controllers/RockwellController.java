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

@RestController
@RequestMapping("/ethernet-ip")
@Tag(name = "Ethernet/IP", description = "Endpoints para comunicação através do protocolo Ethernet/IP (Exclusivo para comunicação com CLPs)")
public class RockwellController extends BasePlcController {

    @GetMapping("/read")
    @Operation(summary = "Leitura de uma variável através do protocolo Ethernet/IP")
    @ApiResponse(responseCode = "200", description = "Variável lida com sucesso")
    public PlcValueDto readRockwell(
            @Parameter(description = "IP do CLP", required = true)
            @RequestParam String ip,

            @Parameter(description = "Tipo de dado a ser lido", required = true)
            @RequestParam PlcDataType data_type,

            @Parameter(description = "Nome da Tag no CLP")
            @RequestParam(required = false) String tag_name) {

        String queryParams = String.format("ip=%s&data_type=%s&tag_name=%s",
                ip, data_type, tag_name != null ? tag_name : "");

        return proxyRequest(plcServiceUrl + "/plc/rockwell/read?" + queryParams,
                null,
                PlcValueDto.class,
                HttpMethod.POST);
    }

    @GetMapping("/write")
    @Operation(summary = "Escrita de uma variável através do protocolo Ethernet/IP")
    @ApiResponse(responseCode = "200", description = "Variável escrita com sucesso")
    public MessageDto writeRockwell(
            @Parameter(description = "IP do CLP", required = true)
            @RequestParam String ip,

            @Parameter(description = "Tipo de dado a ser escrito", required = true)
            @RequestParam PlcDataType data_type,

            @Parameter(description = "Valor a ser escrito", required = true)
            @RequestParam String value,

            @Parameter(description = "Nome da Tag no CLP")
            @RequestParam(required = false) String tag_name) {

        String queryParams = String.format("ip=%s&data_type=%s&value=%s&tag_name=%s",
                ip, data_type, value, tag_name != null ? tag_name : "");

        return proxyRequest(plcServiceUrl + "/plc/rockwell/write?" + queryParams,
                null,
                MessageDto.class,
                HttpMethod.POST);
    }
}