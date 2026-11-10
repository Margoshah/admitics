package by.bsuir.admitics.dto;

import by.bsuir.admitics.entity.AdmissionCampaign;
import by.bsuir.admitics.entity.AdmissionPlan;
import by.bsuir.admitics.entity.Faculty;
import by.bsuir.admitics.entity.Specialty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SettingsOverviewDto {
    private List<Faculty> faculties;
    private List<Specialty> specialties;
    private List<String> studyForms;
    private List<String> financingTypes;
    private List<AdmissionCampaign> campaigns;
    private List<AdmissionPlan> admissionPlans;
    private List<UserSafeDto> users;
    private AnalyticsThresholdsDto thresholds;

    @Data
    public static class UserSafeDto {
        private Long id;
        private String username;
        private String role;
        private Boolean isActive;
    }

    @Data
    public static class AnalyticsThresholdsDto {
        private Long campaignId;
        private BigDecimal greenKcpThreshold;
        private BigDecimal yellowKcpThreshold;
        private BigDecimal anomalyThresholdPct;
    }

    @Data
    public static class UpdatePlanRequest {
        private Integer budgetPlaces;
        private Integer paidPlaces;
        private Integer targetPlaces;
        private Integer passingScoreActual;
    }
}