package by.bsuir.admitics.controller;

import by.bsuir.admitics.dto.SettingsOverviewDto;
import by.bsuir.admitics.dto.SettingsOverviewDto.AnalyticsThresholdsDto;
import by.bsuir.admitics.dto.SettingsOverviewDto.UpdatePlanRequest;
import by.bsuir.admitics.entity.AdmissionCampaign;
import by.bsuir.admitics.entity.AdmissionPlan;
import by.bsuir.admitics.entity.Faculty;
import by.bsuir.admitics.service.SettingsService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    // 1. Получить все справочники и настройки для страницы "Настройки"
    @GetMapping
    public ResponseEntity<SettingsOverviewDto> getSettings(
            @RequestParam(defaultValue = "1") Long campaignId) {
        return ResponseEntity.ok(settingsService.getSettingsOverview(campaignId));
    }

    // 2. Добавить факультет
    @PostMapping("/faculties")
    public ResponseEntity<Faculty> createFaculty(@RequestBody CreateFacultyRequest req) {
        return ResponseEntity.ok(settingsService.createFaculty(req.getShortName(), req.getFullName()));
    }

    // 3. Обновить план приёма (КЦП: бюджетные, платные, целевые места) по специальности
    @PutMapping("/plans/{planId}")
    public ResponseEntity<AdmissionPlan> updatePlan(
            @PathVariable Long planId,
            @RequestBody UpdatePlanRequest req) {
        return ResponseEntity.ok(settingsService.updateAdmissionPlan(planId, req));
    }

    // 4. Изменить пороговые значения светофора рисков и аномалий
    @PutMapping("/thresholds")
    public ResponseEntity<AdmissionCampaign> updateThresholds(@RequestBody AnalyticsThresholdsDto req) {
        return ResponseEntity.ok(settingsService.updateThresholds(req));
    }

    @Data
    public static class CreateFacultyRequest {
        private String shortName;
        private String fullName;
    }
}