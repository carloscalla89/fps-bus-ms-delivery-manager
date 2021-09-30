package com.inretailpharma.digital.deliverymanager.mangepartner.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
@Component
public class RestTemplateUtil {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public RestTemplateUtil(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
    }

    public <T, R> GenericResponse<T> create(Class<T> clazz, String url, HttpMethod method, R body) {
        HttpEntity<R> request = new HttpEntity<>(body);
        return mapRestResponse(clazz, url, method, request);
    }

	public <T, R> GenericResponse<T> mapRestResponse(Class<T> clazz, String url, HttpMethod method,
                                                      HttpEntity<R> request) {
		ResponseEntity<String> responseEntity = restTemplate.exchange(url, method, request, String.class);
		JavaType javaType = objectMapper.getTypeFactory().constructType(clazz);
		GenericResponse<T> rs = new GenericResponse<>();
		rs.setGeneric(readValue(responseEntity, javaType));
		rs.setStatusCode(responseEntity.getStatusCode().toString());
		return rs;
	}

    private <T> T readValue(ResponseEntity<String> response, JavaType javaType) {
        T result = null;
        if (response.getStatusCode() == HttpStatus.OK ||
                response.getStatusCode() == HttpStatus.CREATED) {
            try {
                result = objectMapper.readValue(response.getBody(), javaType);
            } catch (IOException e) {
                log.info(e.getMessage());
            }
        } else {
            log.info("No data found {}", response.getStatusCode());
        }
        return result;
    }
}