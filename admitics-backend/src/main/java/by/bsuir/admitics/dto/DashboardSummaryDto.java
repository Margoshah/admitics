package by.bsuir.admitics.dto;

import by.bsuir.admitics.entity.Notification;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardSummaryDto {
    private Long campaignId;
    private String campaignName;
    private Integer campaignYear;
    private Map<String, Object> kpi;
    private Map<String, Long> dynamics;
    private List<DirectionStatDto> top5Directions;
    private Map<String, Long> geography;
    private List<Notification> recentEvents;

    @Data
    public static class DirectionStatDto {
        private Long specialtyId;
        private String code;
        private String name;
        private String faculty;
        private Long applicationsCount;
        private Integer places;
        private Double competition;
    }
}