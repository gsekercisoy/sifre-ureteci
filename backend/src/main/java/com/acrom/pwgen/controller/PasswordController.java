package com.acrom.pwgen.controller;

import com.acrom.pwgen.service.PasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PasswordController {

    private final PasswordService service;

    public PasswordController(PasswordService service) {
        this.service = service;
    }

    @PostMapping("/passwords/generate")
    public Map<String, Object> generate(@RequestBody(required = false) Map<String, Object> req) {
        return service.generate(req);
    }

    @PostMapping("/passwords/generate/batch")
    public List<Map<String, Object>> generateBatch(@RequestBody(required = false) Map<String, Object> req) {
        int count = 5;
        if (req != null && req.get("count") != null) {
            count = ((Number) req.get("count")).intValue();
        }
        if (count < 1 || count > 20) {
            throw new IllegalArgumentException("count 1-20 araliginda olmali");
        }
        List<Map<String, Object>> out = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            out.add(service.generate(req));
        }
        return out;
    }

    @PostMapping("/passwords/strength")
    public Map<String, Object> strength(@RequestBody Map<String, Object> req) {
        return service.strength(req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> bad(IllegalArgumentException ex) {
        Map<String, String> err = new LinkedHashMap<>();
        err.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(err);
    }
}
