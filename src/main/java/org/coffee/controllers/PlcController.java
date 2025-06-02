package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.coffee.domain.dtos.MessageDto;
import org.coffee.domain.dtos.PlcValueDto;
import org.coffee.domain.enums.PlcDataType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/plc/rockwell")
@Tag(name = "Rockwell PLC Controller", description = "Endpoints for Rockwell PLC communication")
public class PlcController {

    @Value("${plc.service.url}")
    private String plcServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/read")
    @Operation(summary = "Read from Rockwell PLC")
    @ApiResponse(responseCode = "200", description = "Value read successfully")
    public PlcValueDto readRockwell(
            @Parameter(description = "PLC IP address", required = true)
            @RequestParam String ip,

            @Parameter(description = "Data type to read", required = true)
            @RequestParam PlcDataType data_type,

            @Parameter(description = "Tag name to read")
            @RequestParam(required = false) String tag_name) {

        String queryParams = String.format("ip=%s&data_type=%s&tag_name=%s",
                ip, data_type, tag_name != null ? tag_name : "");

        return proxyRequest(plcServiceUrl + "/plc/rockwell/read?" + queryParams,
                null,
                PlcValueDto.class,
                HttpMethod.POST); // Changed to POST
    }

    @GetMapping("/write")
    @Operation(summary = "Write to Rockwell PLC")
    @ApiResponse(responseCode = "200", description = "Value written successfully")
    public MessageDto writeRockwell(
            @Parameter(description = "PLC IP address", required = true)
            @RequestParam String ip,

            @Parameter(description = "Data type to write", required = true)
            @RequestParam PlcDataType data_type,

            @Parameter(description = "Value to write", required = true)
            @RequestParam String value,

            @Parameter(description = "Tag name to write to")
            @RequestParam(required = false) String tag_name) {

        String queryParams = String.format("ip=%s&data_type=%s&value=%s&tag_name=%s",
                ip, data_type, value, tag_name != null ? tag_name : "");

        return proxyRequest(plcServiceUrl + "/plc/rockwell/write?" + queryParams,
                null,
                MessageDto.class,
                HttpMethod.POST); // Changed to POST
    }

    private <T> T proxyRequest(String url, Object body, Class<T> responseType, HttpMethod method) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);

            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw new ResponseStatusException(
                    HttpStatus.valueOf(e.getRawStatusCode()),
                    e.getResponseBodyAsString()
            );
        }
    }
}