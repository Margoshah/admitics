package by.bsuir.admitics.dto;

import by.bsuir.admitics.entity.AdmissionPlan;
import by.bsuir.admitics.entity.MlPrediction;
import by.bsuir.admitics.entity.Specialty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DirectionsAnalyticsDto {
    private Integer directionsCount;
    private Integer totalApplications;
    private Double averageCompetition;
    private Double averagePlanFill;
    private List<FacultyRatingItem> facultyRating;
    private List<DirectionTableRow> directionsTable;

    @Data
    public static class DirectionTableRow {
        private Long specialtyId;
        private String code;
        private String name;
        private String faculty;
        private Integer applicationsCount;
        private Integer totalPlaces;
        private Integer budgetPlaces;
        private Integer paidPlaces;
        private Double competition;
        private Double averageScore;
        private Integer passingScoreActual;
        private Double planFillPercentage;
    }

    @Data
    public static class FacultyRatingItem {
        private String faculty;
        private Integer applicationsCount;
        private Integer places;
        private Double competition;
        private Double fillPercentage;
    }

    @Data
    public static class DirectionDetailsDto {
        private Specialty specialty;
        private AdmissionPlan plan;
        private Integer applicationsCount;
        private Map<String, Long> dynamics;
        private Map<String, Long> scoreDistribution;
        private List<MlPrediction> forecasts;
    }
}