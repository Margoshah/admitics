package by.bsuir.admitics.service;

import by.bsuir.admitics.entity.AdmissionCampaign;
import by.bsuir.admitics.entity.MlPrediction;
import by.bsuir.admitics.entity.Specialty;
import by.bsuir.admitics.repository.AdmissionCampaignRepository;
import by.bsuir.admitics.repository.MlPredictionRepository;
import by.bsuir.admitics.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final MlPredictionRepository predictionRepository;
    private final AdmissionCampaignRepository campaignRepository;
    private final SpecialtyRepository specialtyRepository;

    // Адрес твоего будущего Python-микросервиса на FastAPI
    private final String ML_SERVICE_URL = "http://localhost:8000";
    private final RestTemplate restTemplate = new RestTemplate();

    // Получить все сохранённые прогнозы для дашборда/отчётов
    public List<MlPrediction> getCampaignPredictions(Long campaignId) {
        return predictionRepository.findByCampaignId(campaignId);
    }

    // Запрос к Python для генерации нового прогноза (Random Forest)
    public MlPrediction generatePrediction(Long campaignId, Long specialtyId, String predictionType) {
        AdmissionCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Кампания не найдена"));
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new IllegalArgumentException("Специальность не найдена"));

        // В реальной системе здесь собираются признаки (features) для отправки в ML
        // Пример отправки запроса к Python (заглушка для интеграции)
        /*
        Map<String, Object> requestData = Map.of("specialty_id", specialtyId, "features", ...);
        ResponseEntity response = restTemplate.postForEntity(
                ML_SERVICE_URL + "/predict/" + predictionType, requestData, Map.class);
        BigDecimal predictedValue = new BigDecimal(response.getBody().get("predicted_value").toString());
        */

        // Временная заглушка, пока Python-сервер не запущен (генерируем правдоподобное число)
        BigDecimal simulatedPredictionValue = new BigDecimal(Math.random() * 50 + 320);

        MlPrediction prediction = new MlPrediction();
        prediction.setCampaign(campaign);
        prediction.setSpecialty(specialty);
        prediction.setPredictionType(predictionType); // PASSING_SCORE или APPLICATIONS_COUNT
        prediction.setPredictedValue(simulatedPredictionValue);
        prediction.setCreatedAt(LocalDateTime.now());

        return predictionRepository.save(prediction);
    }
}