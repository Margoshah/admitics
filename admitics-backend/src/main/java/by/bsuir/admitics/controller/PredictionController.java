package by.bsuir.admitics.controller;

import by.bsuir.admitics.entity.MlPrediction;
import by.bsuir.admitics.service.PredictionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forecasts")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    // Получить историю прогнозов для кампании
    @GetMapping
    public ResponseEntity<List<MlPrediction>> getForecasts(@RequestParam Long campaignId) {
        return ResponseEntity.ok(predictionService.getCampaignPredictions(campaignId));
    }

    // Запустить пересчёт прогноза через ML-модуль
    @PostMapping("/generate")
    public ResponseEntity<MlPrediction> generateForecast(@RequestBody ForecastRequest request) {
        MlPrediction newPrediction = predictionService.generatePrediction(
                request.getCampaignId(),
                request.getSpecialtyId(),
                request.getPredictionType()
        );
        return ResponseEntity.ok(newPrediction);
    }

    @Data
    public static class ForecastRequest {
        private Long campaignId;
        private Long specialtyId;
        private String predictionType; // Например, "PASSING_SCORE"
    }
}