package by.bsuir.admitics.service;

import by.bsuir.admitics.dto.DashboardSummaryDto;
import by.bsuir.admitics.dto.DashboardSummaryDto.DirectionStatDto;
import by.bsuir.admitics.entity.*;
import by.bsuir.admitics.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AdmissionCampaignRepository campaignRepository;
    private final ApplicantRepository applicantRepository;
    private final AdmissionPlanRepository planRepository;
    private final NotificationRepository notificationRepository;

    public DashboardSummaryDto getDashboardSummary(Long campaignId, Long facultyId, Long specialtyId) {
        AdmissionCampaign campaign = (campaignId != null)
                ? campaignRepository.findById(campaignId).orElseThrow(() -> new RuntimeException("Кампания не найдена"))
                : campaignRepository.findFirstByIsCurrentTrue().orElseThrow(() -> new RuntimeException("Текущая кампания не найдена"));

        Long activeCampaignId = campaign.getId();

        List<AdmissionPlan> plans = planRepository.findByCampaignId(activeCampaignId);
        List<Applicant> applicants = applicantRepository.findByCampaignId(activeCampaignId);

        if (facultyId != null) {
            plans = plans.stream().filter(p -> p.getSpecialty().getFaculty().getId().equals(facultyId)).collect(Collectors.toList());
        }
        if (specialtyId != null) {
            plans = plans.stream().filter(p -> p.getSpecialty().getId().equals(specialtyId)).collect(Collectors.toList());
        }

        Set<Long> allowedSpecIds = plans.stream().map(p -> p.getSpecialty().getId()).collect(Collectors.toSet());

        if (facultyId != null || specialtyId != null) {
            applicants = applicants.stream()
                    .filter(a -> a.getPriorities() != null && a.getPriorities().stream()
                            .anyMatch(pr -> allowedSpecIds.contains(pr.getSpecialty().getId())))
                    .collect(Collectors.toList());
        }

        // 1. Карточки KPI (5 штук как на макете)
        int totalApplicants = applicants.size();
        int totalPlaces = plans.stream().mapToInt(p -> p.getBudgetPlaces() + p.getPaidPlaces() + p.getTargetPlaces()).sum();

        double competition = totalPlaces > 0 ? Math.round(((double) totalApplicants / totalPlaces) * 100.0) / 100.0 : 0.0;
        double fillPct = totalPlaces > 0 ? Math.round(((double) totalApplicants / totalPlaces) * 1000.0) / 10.0 : 0.0;
        double avgScore = Math.round(applicants.stream().mapToInt(Applicant::getTotalScore).average().orElse(0.0) * 10.0) / 10.0;

        long riskyDirectionsCount = plans.stream()
                .filter(p -> p.getPassingScoreActual() != null && p.getPassingScoreActual() > 0 && p.getPassingScoreActual() < 300)
                .count();

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("totalApplicants", totalApplicants);
        kpi.put("competitionPerPlace", competition);
        kpi.put("averageScore", avgScore);
        kpi.put("kcpFillPercentage", fillPct);
        kpi.put("totalPlaces", totalPlaces);
        kpi.put("riskyDirectionsCount", riskyDirectionsCount);

        // 2. Динамика подачи заявлений по датам
        Map<String, Long> dynamics = applicants.stream()
                .filter(a -> a.getApplicationDate() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getApplicationDate().toString(),
                        TreeMap::new,
                        Collectors.counting()
                ));

        // 3. Топ-5 популярных направлений (через DirectionStatDto — без вложенных скобок!)
        Map<Long, Long> appsPerSpecialty = new HashMap<>();
        for (Applicant a : applicants) {
            if (a.getPriorities() != null) {
                for (ApplicationPriority pr : a.getPriorities()) {
                    if (pr.getPriorityOrder() == 1) {
                        Long sId = pr.getSpecialty().getId();
                        appsPerSpecialty.put(sId, appsPerSpecialty.getOrDefault(sId, 0L) + 1L);
                    }
                }
            }
        }

        List<DirectionStatDto> top5Directions = plans.stream()
                .map(p -> {
                    Long sId = p.getSpecialty().getId();
                    long count = appsPerSpecialty.getOrDefault(sId, 0L);
                    int places = p.getBudgetPlaces() + p.getPaidPlaces() + p.getTargetPlaces();
                    double specComp = places > 0 ? Math.round(((double) count / places) * 100.0) / 100.0 : 0.0;

                    DirectionStatDto item = new DirectionStatDto();
                    item.setSpecialtyId(sId);
                    item.setCode(p.getSpecialty().getCode());
                    item.setName(p.getSpecialty().getName());
                    item.setFaculty(p.getSpecialty().getFaculty().getShortName());
                    item.setApplicationsCount(count);
                    item.setPlaces(places);
                    item.setCompetition(specComp);
                    return item;
                })
                .sorted((a, b) -> Long.compare(b.getApplicationsCount(), a.getApplicationsCount()))
                .limit(5)
                .collect(Collectors.toList());

        // 4. География поступающих (по областям Беларуси)
        Map<String, Long> geography = applicants.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getRegion() != null ? a.getRegion().getName() : "Не указан",
                        Collectors.counting()
                ));

        // 5. Последние важные изменения и риски (5 последних событий)
        List<Notification> recentEvents = notificationRepository.findAllByOrderByCreatedAtDesc()
                .stream().limit(5).collect(Collectors.toList());

        DashboardSummaryDto summary = new DashboardSummaryDto();
        summary.setCampaignId(activeCampaignId);
        summary.setCampaignName(campaign.getName());
        summary.setCampaignYear(campaign.getYear());
        summary.setKpi(kpi);
        summary.setDynamics(dynamics);
        summary.setTop5Directions(top5Directions);
        summary.setGeography(geography);
        summary.setRecentEvents(recentEvents);

        return summary;
    }
}