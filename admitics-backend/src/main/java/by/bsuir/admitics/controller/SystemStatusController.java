package by.bsuir.admitics.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/status")
@CrossOrigin(origins = "http://localhost:5173")
public class SystemStatusController {

    @GetMapping
    public Map<String, Object> getStatus() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("system", "Admitics — Аналитика приёмной кампании БГУИР");
        response.put("status", "ONLINE");
        response.put("campaignYear", 2026);
        response.put("university", "БГУИР (АСПЗиЗ)");
        response.put("serverTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
        return response;
    }
}