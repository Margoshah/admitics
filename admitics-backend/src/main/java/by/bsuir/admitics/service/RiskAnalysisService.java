package by.bsuir.admitics.service;

import by.bsuir.admitics.entity.AdmissionPlan;
import by.bsuir.admitics.entity.Notification;
import by.bsuir.admitics.entity.User;
import by.bsuir.admitics.repository.AdmissionPlanRepository;
import by.bsuir.admitics.repository.NotificationRepository;
import by.bsuir.admitics.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RiskAnalysisService {

    private final AdmissionPlanRepository planRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 1. Получение всех уведомлений для верхнего колокольчика (UC-13)
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    // 2. Сводная аналитика для страницы "Аномалии и риски" (UC-9)
    public RisksOverviewResponse getAnalyticsAndRisksOverview(Long campaignId) {
        List<Notification> allEvents = notificationRepository.findAllByOrderByCreatedAtDesc();

        // Фильтруем аномалии
        List<Notification> anomalies = allEvents.stream()
                .filter(n -> "ANOMALY".equalsIgnoreCase(n.getType()))
                .collect(Collectors.toList());

        // Рассчитываем светофор рисков по каждой специальности
        List<AdmissionPlan> plans = planRepository.findByCampaignId(campaignId);
        List<SpecialtyRiskDto> specialtyRisks = new ArrayList<>();

        int yellowCount = 0;
        int redCount = 0;

        for (AdmissionPlan plan : plans) {
            int totalPlaces = plan.getBudgetPlaces() + plan.getPaidPlaces() + plan.getTargetPlaces();
            Integer score = plan.getPassingScoreActual();

            String riskZone = "GREEN";
            String reason = "Показатели в пределах нормы, план приёма выполняется";

            if (score != null && score > 0 && score < 270) {
                riskZone = "RED";
                reason = "Высокий риск недобора: критически низкий проходной балл и конкурс";
                redCount++;
            } else if (score != null && score > 0 && score < 300) {
                riskZone = "YELLOW";
                reason = "Средний уровень риска: снижение темпов подачи заявлений";
                yellowCount++;
            }

            SpecialtyRiskDto dto = new SpecialtyRiskDto();
            dto.setSpecialtyId(plan.getSpecialty().getId());
            dto.setSpecialtyCode(plan.getSpecialty().getCode());
            dto.setSpecialtyName(plan.getSpecialty().getName());
            dto.setFaculty(plan.getSpecialty().getFaculty().getShortName());
            dto.setTotalPlaces(totalPlaces);
            dto.setPassingScore(score != null ? score : 0);
            dto.setRiskZone(riskZone);
            dto.setReason(reason);

            specialtyRisks.add(dto);
        }

        // Формируем автоматические рекомендации на основе реальной картины
        List<String> recommendations = new ArrayList<>();
        if (redCount > 0) {
            recommendations.add("Обнаружено критических направлений: " + redCount + ". Требуется оперативное информирование абитуриентов о наличии вакантных мест.");
        }
        if (yellowCount > 0) {
            recommendations.add("Направлений со средним риском (жёлтая зона): " + yellowCount + ". Рекомендуется усилить консультационную работу приёмной комиссии.");
        }
        if (!anomalies.isEmpty()) {
            recommendations.add("Зафиксировано аномальных отклонений в динамике подачи заявлений: " + anomalies.size() + ". Проверьте распределение нагрузки.");
        }
        if (recommendations.isEmpty()) {
            recommendations.add("Все направления находятся в зелёной зоне. Темпы подачи документов соответствуют плановым показателям КЦП.");
        }

        RisksOverviewResponse response = new RisksOverviewResponse();
        response.setAnomaliesCount(anomalies.size());
        response.setRiskSpecialtiesCount(yellowCount + redCount);
        response.setCriticalCount(redCount);
        response.setAnomaliesList(anomalies);
        response.setSpecialtyRisks(specialtyRisks);
        response.setRecommendations(recommendations);

        return response;
    }

    // 3. Метод автоматической генерации уведомлений после импорта (светофор)
    @Transactional
    public void analyzeRisksAndGenerateNotifications(Long campaignId) {
        List<AdmissionPlan> plans = planRepository.findByCampaignId(campaignId);
        User operator = userRepository.findByUsername("ivanov")
                .orElseGet(() -> userRepository.findByUsername("admin").orElse(null));

        if (operator == null) {
            return;
        }

        for (AdmissionPlan plan : plans) {
            Integer score = plan.getPassingScoreActual();
            String specTitle = plan.getSpecialty().getFaculty().getShortName() + " — "
                    + plan.getSpecialty().getCode() + " " + plan.getSpecialty().getName();

            if (score != null && score > 0 && score < 270) {
                Notification n = new Notification();
                n.setUser(operator);
                n.setTitle("Высокий риск: " + specTitle);
                n.setMessage("Критическое снижение проходного балла (" + score + "). Высокий риск невыполнения КЦП.");
                n.setType("RISK_RED");
                notificationRepository.save(n);
            } else if (score != null && score > 0 && score < 300) {
                Notification n = new Notification();
                n.setUser(operator);
                n.setTitle("Внимание: " + specTitle);
                n.setMessage("Показатели конкурса находятся в пограничной жёлтой зоне (балл: " + score + ").");
                n.setType("RISK_YELLOW");
                notificationRepository.save(n);
            }
        }
    }

    // DTO для ответа по конкретной специальности в таблице рисков
    @Data
    public static class SpecialtyRiskDto {
        private Long specialtyId;
        private String specialtyCode;
        private String specialtyName;
        private String faculty;
        private Integer totalPlaces;
        private Integer passingScore;
        private String riskZone; // GREEN, YELLOW, RED
        private String reason;
    }

    // Сводный DTO для всей страницы "Аномалии и риски"
    @Data
    public static class RisksOverviewResponse {
        private Integer anomaliesCount;
        private Integer riskSpecialtiesCount;
        private Integer criticalCount;
        private List<Notification> anomaliesList;
        private List<SpecialtyRiskDto> specialtyRisks;
        private List<String> recommendations;
    }
}