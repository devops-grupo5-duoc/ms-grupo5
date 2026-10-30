package com.example.ms_pedido.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/info")
public class InfoController {

    private final String version;

    public InfoController(@Value("${app.version:local}") String version) {
        this.version = version;
    }

    @GetMapping
    public Map<String, String> info() {
        return Map.of("servicio", "ms-pedido", "version", version);
    }

    @GetMapping("/test1")
    public String test1() {
        return "test1";
    }
}