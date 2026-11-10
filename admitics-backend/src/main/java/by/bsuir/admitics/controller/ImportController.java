
package by.bsuir.admitics.controller;

import by.bsuir.admitics.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @PostMapping
    public ResponseEntity<Map<String, String>> uploadCampaignData(
            @RequestParam("file") MultipartFile file,
            @RequestParam("campaignId") Long campaignId) {

        Map<String, String> response = new HashMap<>();

        if (file == null || file.isEmpty()) {
            response.put("error", "Файл пуст. Выберите корректный CSV-файл.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            String resultMessage = importService.importApplicantsCsv(file, campaignId);
            response.put("message", resultMessage);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Ошибка при обработке файла: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
