package by.bsuir.admitics.controller;

import by.bsuir.admitics.dto.ReportPayloadDto;
import by.bsuir.admitics.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // Просмотр любого из 8 типов отчётов на странице "Отчёты" (UC-10)
    @GetMapping("/view")
    public ResponseEntity<ReportPayloadDto> viewReport(
            @RequestParam Long campaignId,
            @RequestParam(defaultValue = "SUMMARY") String type) {
        return ResponseEntity.ok(reportService.generateReport(campaignId, type));
    }

    // Скачивание отчёта в формате CSV/Excel (UC-11)
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam Long campaignId,
            @RequestParam(defaultValue = "SUMMARY") String type) {
        byte[] csvBytes = reportService.exportReportToCsv(campaignId, type);

        String filename = "admitics_report_" + type.toLowerCase() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvBytes);
    }
}