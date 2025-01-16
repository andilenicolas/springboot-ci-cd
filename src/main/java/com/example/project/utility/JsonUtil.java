package com.example.project.utility;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class JsonUtil 
{
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "Error serializing object: " + e.getMessage();
        }
    }
}
