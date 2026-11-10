package by.bsuir.admitics.controller;

import by.bsuir.admitics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // Для страницы 2.2 «Динамика» (UC-5)
    @GetMapping("/dynamics")
    public ResponseEntity<Object> getDynamics(
            @RequestParam(defaultValue = "1") Long campaignId,
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) Long specialtyId) {
        return ResponseEntity.ok(analyticsService.getApplicationsDynamics(campaignId, facultyId, specialtyId));
    }

    // Для страницы 2.4 «Абитуриенты» (UC-7)
    @GetMapping("/applicants")
    public ResponseEntity<Object> getApplicantsStats(
            @RequestParam(defaultValue = "1") Long campaignId) {
        return ResponseEntity.ok(analyticsService.getApplicantsAnalytics(campaignId));
    }
}