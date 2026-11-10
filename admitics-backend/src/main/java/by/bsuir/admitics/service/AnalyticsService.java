package by.bsuir.admitics.service;

import by.bsuir.admitics.entity.AdmissionPlan;
import by.bsuir.admitics.entity.Applicant;
import by.bsuir.admitics.repository.AdmissionPlanRepository;
import by.bsuir.admitics.repository.ApplicantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ApplicantRepository applicantRepository;
    private final AdmissionPlanRepository planRepository;

    // Для страницы 2.2 "Динамика" (UC-5)
    public Map<String, Object> getApplicationsDynamics(Long campaignId, Long facultyId, Long specialtyId) {
        List<AdmissionPlan> plans = planRepository.findByCampaignId(campaignId);
        List<Applicant> applicants = applicantRepository.findByCampaignId(campaignId);

        if (facultyId != null) {
            plans = plans.stream()
                    .filter(p -> p.getSpecialty().getFaculty().getId().equals(facultyId))
                    .collect(Collectors.toList());
        }
        if (specialtyId != null) {
            plans = plans.stream()
                    .filter(p -> p.getSpecialty().getId().equals(specialtyId))
                    .collect(Collectors.toList());
        }

        Set<Long> allowedSpecIds = plans.stream()
                .map(p -> p.getSpecialty().getId())
                .collect(Collectors.toSet());

        if (facultyId != null || specialtyId != null) {
            applicants = applicants.stream()
                    .filter(a -> a.getPriorities() != null && a.getPriorities().stream()
                            .anyMatch(pr -> allowedSpecIds.contains(pr.getSpecialty().getId())))
                    .collect(Collectors.toList());
        }

        int totalPlaces = plans.stream()
                .mapToInt(p -> p.getBudgetPlaces() + p.getPaidPlaces() + p.getTargetPlaces())
                .sum();

        // Группируем по дате без вложенных угловых скобок
        var byDate = applicants.stream()
                .filter(a -> a.getApplicationDate() != null)
                .collect(Collectors.groupingBy(Applicant::getApplicationDate, TreeMap::new, Collectors.toList()));

        Map<String, Long> dailyApplications = new LinkedHashMap<>();
        Map<String, Double> dailyAverageScore = new LinkedHashMap<>();
        Map<String, Double> cumulativeCompetition = new LinkedHashMap<>();
        Map<String, Double> cumulativePlanFill = new LinkedHashMap<>();

        long runningTotal = 0;
        for (LocalDate date : byDate.keySet()) {
            String dateKey = date.toString();
            List<Applicant> dayApps = byDate.get(date);

            runningTotal += dayApps.size();
            double dayAvg = Math.round(dayApps.stream().mapToInt(Applicant::getTotalScore).average().orElse(0.0) * 10.0) / 10.0;
            double comp = totalPlaces > 0 ? Math.round(((double) runningTotal / totalPlaces) * 100.0) / 100.0 : 0.0;
            double fill = totalPlaces > 0 ? Math.round(((double) runningTotal / totalPlaces) * 1000.0) / 10.0 : 0.0;

            dailyApplications.put(dateKey, (long) dayApps.size());
            dailyAverageScore.put(dateKey, dayAvg);
            cumulativeCompetition.put(dateKey, comp);
            cumulativePlanFill.put(dateKey, fill);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("dailyApplications", dailyApplications);
        response.put("dailyAverageScore", dailyAverageScore);
        response.put("cumulativeCompetition", cumulativeCompetition);
        response.put("cumulativePlanFill", cumulativePlanFill);
        return response;
    }

    // Для страницы 2.4 "Абитуриенты" (UC-7)
    public Map<String, Object> getApplicantsAnalytics(Long campaignId) {
        List<Applicant> applicants = applicantRepository.findByCampaignId(campaignId);
        int total = applicants.size();

        long budgetCount = applicants.stream().filter(a -> "BUDGET".equalsIgnoreCase(a.getFinancingType())).count();
        long paidCount = applicants.stream().filter(a -> "PAID".equalsIgnoreCase(a.getFinancingType())).count();
        long targetCount = applicants.stream().filter(a -> "TARGET".equalsIgnoreCase(a.getFinancingType())).count();

        double avgScore = Math.round(applicants.stream().mapToInt(Applicant::getTotalScore).average().orElse(0.0) * 10.0) / 10.0;

        List<Integer> sortedScores = applicants.stream()
                .map(Applicant::getTotalScore)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        double medianScore = 0.0;
        if (!sortedScores.isEmpty()) {
            int mid = sortedScores.size() / 2;
            medianScore = (sortedScores.size() % 2 == 0)
                    ? (sortedScores.get(mid - 1) + sortedScores.get(mid)) / 2.0
                    : sortedScores.get(mid);
        }

        double budgetSharePct = total > 0 ? Math.round(((double) budgetCount / total) * 1000.0) / 10.0 : 0.0;

        Map<String, Long> scoreDistribution = new LinkedHashMap<>();
        scoreDistribution.put("менее 250", applicants.stream().filter(a -> a.getTotalScore() < 250).count());
        scoreDistribution.put("250–299", applicants.stream().filter(a -> a.getTotalScore() >= 250 && a.getTotalScore() < 300).count());
        scoreDistribution.put("300–349", applicants.stream().filter(a -> a.getTotalScore() >= 300 && a.getTotalScore() < 350).count());
        scoreDistribution.put("350–400", applicants.stream().filter(a -> a.getTotalScore() >= 350).count());

        Map<String, Long> geography = applicants.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getRegion() != null ? a.getRegion().getName() : "Не указан",
                        Collectors.counting()
                ));

        List<String> conclusions = new ArrayList<>();
        conclusions.add("Средний балл поступающих составляет " + avgScore + " (медианный балл — " + medianScore + ").");
        conclusions.add("Доля абитуриентов, претендующих на бюджетную форму обучения: " + budgetSharePct + "%.");
        conclusions.add("Высокобалльники (350+ баллов) составляют " + scoreDistribution.get("350–400") + " чел.");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("budget", budgetCount);
        result.put("paid", paidCount);
        result.put("target", targetCount);
        result.put("budgetSharePercentage", budgetSharePct);
        result.put("averageScore", avgScore);
        result.put("medianScore", medianScore);
        result.put("scoreDistribution", scoreDistribution);
        result.put("geography", geography);
        result.put("conclusions", conclusions);
        return result;
    }
}