package com.crqs.command.cqrs_command.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    public Map<String, Object> toMap(Object object) {
        if (object == null) return Map.of();
        return objectMapper.convertValue(object, new TypeReference<>() {});
    }
}
