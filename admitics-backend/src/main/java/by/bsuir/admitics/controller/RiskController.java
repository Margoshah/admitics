package by.bsuir.admitics.controller;

import by.bsuir.admitics.entity.Notification;
import by.bsuir.admitics.service.RiskAnalysisService;
import by.bsuir.admitics.service.RiskAnalysisService.RisksOverviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics-and-risks")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class RiskController {

    private final RiskAnalysisService riskAnalysisService;

    // 1. Уведомления для верхнего колокольчика в шапке (UC-13)
    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications() {
        return ResponseEntity.ok(riskAnalysisService.getAllNotifications());
    }

    // 2. Полные данные для страницы "Аномалии и риски" (Обзор, Аномалии, Риски, Светофор, Рекомендации)
    @GetMapping
    public ResponseEntity<RisksOverviewResponse> getAnalyticsAndRisksOverview(@RequestParam Long campaignId) {
        return ResponseEntity.ok(riskAnalysisService.getAnalyticsAndRisksOverview(campaignId));
    }
}