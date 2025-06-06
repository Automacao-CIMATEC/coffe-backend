package org.coffee.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

public abstract class BasePlcController {

    @Value("${plc.service.url}")
    protected String plcServiceUrl;

    protected final RestTemplate restTemplate = new RestTemplate();

    protected <T> T proxyRequest(String url, Object body, Class<T> responseType, HttpMethod method) {
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