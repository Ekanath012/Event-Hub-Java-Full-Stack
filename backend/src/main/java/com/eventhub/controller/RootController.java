package com.eventhub.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {
    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "service", "EventHub API",
                "status", "UP",
                "health", "/actuator/health",
                "docs", "/swagger-ui/index.html");
    }
}
