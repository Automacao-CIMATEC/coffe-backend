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

/*
 OBSERVAÇÃO IMPORTANTE
 A funcionalidade de comunicação via protocolo Ethernet/IP não está nativamente contida nessa aplicação JAVA
 Existe um micro serviço Python que possui essa funcionalidade, o aplicação JAVA consome esse serviço via HTTP

 Essa funcionalidade é exclusiva para comunicação com CLPs Rockwell CompactLogix, ControlLogix, MicroLogix
 Essa funcionalidade NÃO irá funcionar em qualquer outro dispositivo, mesmo que seja compatível com o protocolo Ethernet/IP
 Lembrete para testar essa funcionalidade com outro dispositivo Rockwell como IHM que fale o protocolo Ethernet/IP para ter certeza que só funciona com CLPs
 */

@RestController
@RequestMapping("/ethernet-ip")
@Tag(name = "Ethernet/IP", description = "Endpoints para comunicação através do protocolo Ethernet/IP (Exclusivo para comunicação com CLPs CompactLogix, ControlLogix e MicroLogix)")
public class EthernetIpController extends BasePlcController {

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

    @PostMapping("/write") // VERIFICAR SE ESTÁ FUNCIONANDO APÓS ALTERAR PARA POST
    @Operation(summary = "Escrita de uma variável através do protocolo Ethernet/IP")
    @ApiResponse(responseCode = "200", description = "Variável escrita com sucesso")
    public ExampleMessageDto writeRockwell(
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
                ExampleMessageDto.class,
                HttpMethod.POST);
    }
}