package com.example.demo_saga_2.business_logic.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Configuration
public class JsonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm");


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.setDateFormat(df);
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    @Bean
    public JavaTimeModule dateTimeModule(){
        return new JavaTimeModule();
    }
}
