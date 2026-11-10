package by.bsuir.admitics.controller;

import by.bsuir.admitics.dto.DashboardSummaryDto;
import by.bsuir.admitics.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary(
            @RequestParam(required = false) Long campaignId,
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) Long specialtyId) {
        return ResponseEntity.ok(dashboardService.getDashboardSummary(campaignId, facultyId, specialtyId));
    }
}