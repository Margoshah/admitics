package by.bsuir.admitics.service;

import by.bsuir.admitics.dto.DirectionsAnalyticsDto;
import by.bsuir.admitics.dto.DirectionsAnalyticsDto.DirectionDetailsDto;
import by.bsuir.admitics.dto.DirectionsAnalyticsDto.DirectionTableRow;
import by.bsuir.admitics.dto.DirectionsAnalyticsDto.FacultyRatingItem;
import by.bsuir.admitics.entity.*;
import by.bsuir.admitics.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final FacultyRepository facultyRepository;
    private final AdmissionPlanRepository planRepository;
    private final ApplicantRepository applicantRepository;
    private final MlPredictionRepository predictionRepository;

    public List<Specialty> getAllSpecialties() {
        return specialtyRepository.findAll();
    }

    public List<Specialty> getByFaculty(Long facultyId) {
        return specialtyRepository.findByFacultyId(facultyId);
    }

    public Specialty getById(Long id) {
        return specialtyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Специальность не найдена"));
    }

    // Полная аналитика для страницы 2.3 "Направления"
    public DirectionsAnalyticsDto getDirectionsPageAnalytics(Long campaignId, Long facultyId, String studyForm) {
        List<AdmissionPlan> plans = planRepository.findByCampaignId(campaignId);
        List<Applicant> applicants = applicantRepository.findByCampaignId(campaignId);

        if (facultyId != null) {
            plans = plans.stream()
                    .filter(p -> p.getSpecialty().getFaculty().getId().equals(facultyId))
                    .collect(Collectors.toList());
        }
        if (studyForm != null && !studyForm.isBlank()) {
            applicants = applicants.stream()
                    .filter(a -> studyForm.equalsIgnoreCase(a.getStudyForm()))
                    .collect(Collectors.toList());
        }

        // Группируем абитуриентов по 1-му приоритету специальности (через var без двойных скобок)
        var appsBySpec = new HashMap<Long, List<Applicant>>();
        for (Applicant a : applicants) {
            if (a.getPriorities() != null) {
                for (ApplicationPriority pr : a.getPriorities()) {
                    if (pr.getPriorityOrder() == 1) {
                        appsBySpec.computeIfAbsent(pr.getSpecialty().getId(), k -> new ArrayList<>()).add(a);
                    }
                }
            }
        }

        List<DirectionTableRow> tableRows = new ArrayList<>();
        Map<String, Integer> facultyApps = new HashMap<>();
        Map<String, Integer> facultyPlaces = new HashMap<>();

        int totalApps = 0;
        int totalPlacesAll = 0;

        for (AdmissionPlan p : plans) {
            Specialty s = p.getSpecialty();
            String facName = s.getFaculty().getShortName();
            List<Applicant> specApplicants = appsBySpec.getOrDefault(s.getId(), Collections.emptyList());

            int appCount = specApplicants.size();
            int places = p.getBudgetPlaces() + p.getPaidPlaces() + p.getTargetPlaces();
            double competition = places > 0 ? Math.round(((double) appCount / places) * 100.0) / 100.0 : 0.0;
            double avgScore = Math.round(specApplicants.stream().mapToInt(Applicant::getTotalScore).average().orElse(0.0) * 10.0) / 10.0;
            double planFillPct = places > 0 ? Math.round(((double) appCount / places) * 1000.0) / 10.0 : 0.0;

            totalApps += appCount;
            totalPlacesAll += places;

            facultyApps.put(facName, facultyApps.getOrDefault(facName, 0) + appCount);
            facultyPlaces.put(facName, facultyPlaces.getOrDefault(facName, 0) + places);

            DirectionTableRow row = new DirectionTableRow();
            row.setSpecialtyId(s.getId());
            row.setCode(s.getCode());
            row.setName(s.getName());
            row.setFaculty(facName);
            row.setApplicationsCount(appCount);
            row.setTotalPlaces(places);
            row.setBudgetPlaces(p.getBudgetPlaces());
            row.setPaidPlaces(p.getPaidPlaces());
            row.setCompetition(competition);
            row.setAverageScore(avgScore);
            row.setPassingScoreActual(p.getPassingScoreActual());
            row.setPlanFillPercentage(planFillPct);
            tableRows.add(row);
        }

        // Сравнительный рейтинг факультетов
        List<FacultyRatingItem> facultyRating = new ArrayList<>();
        for (String fac : facultyPlaces.keySet()) {
            int fApps = facultyApps.getOrDefault(fac, 0);
            int fPlaces = facultyPlaces.getOrDefault(fac, 0);
            double fComp = fPlaces > 0 ? Math.round(((double) fApps / fPlaces) * 100.0) / 100.0 : 0.0;
            double fFill = fPlaces > 0 ? Math.round(((double) fApps / fPlaces) * 1000.0) / 10.0 : 0.0;

            FacultyRatingItem fItem = new FacultyRatingItem();
            fItem.setFaculty(fac);
            fItem.setApplicationsCount(fApps);
            fItem.setPlaces(fPlaces);
            fItem.setCompetition(fComp);
            fItem.setFillPercentage(fFill);
            facultyRating.add(fItem);
        }
        facultyRating.sort((a, b) -> Double.compare(b.getCompetition(), a.getCompetition()));

        DirectionsAnalyticsDto result = new DirectionsAnalyticsDto();
        result.setDirectionsCount(plans.size());
        result.setTotalApplications(totalApps);
        result.setAverageCompetition(totalPlacesAll > 0 ? Math.round(((double) totalApps / totalPlacesAll) * 100.0) / 100.0 : 0.0);
        result.setAveragePlanFill(totalPlacesAll > 0 ? Math.round(((double) totalApps / totalPlacesAll) * 1000.0) / 10.0 : 0.0);
        result.setFacultyRating(facultyRating);
        result.setDirectionsTable(tableRows);
        return result;
    }

    // Детальная карточка конкретного направления при клике
    public DirectionDetailsDto getDirectionDetails(Long specialtyId, Long campaignId) {
        Specialty s = getById(specialtyId);
        List<AdmissionPlan> plans = planRepository.findByCampaignId(campaignId);
        AdmissionPlan plan = plans.stream()
                .filter(p -> p.getSpecialty().getId().equals(specialtyId))
                .findFirst().orElse(null);

        List<Applicant> specApplicants = applicantRepository.findByCampaignId(campaignId).stream()
                .filter(a -> a.getPriorities() != null && a.getPriorities().stream()
                        .anyMatch(pr -> pr.getSpecialty().getId().equals(specialtyId)))
                .collect(Collectors.toList());

        Map<String, Long> dynamics = specApplicants.stream()
                .filter(a -> a.getApplicationDate() != null)
                .collect(Collectors.groupingBy(a -> a.getApplicationDate().toString(), TreeMap::new, Collectors.counting()));

        Map<String, Long> scoreDistribution = new LinkedHashMap<>();
        scoreDistribution.put("до 250", specApplicants.stream().filter(a -> a.getTotalScore() < 250).count());
        scoreDistribution.put("250-300", specApplicants.stream().filter(a -> a.getTotalScore() >= 250 && a.getTotalScore() < 300).count());
        scoreDistribution.put("300-350", specApplicants.stream().filter(a -> a.getTotalScore() >= 300 && a.getTotalScore() < 350).count());
        scoreDistribution.put("350-400", specApplicants.stream().filter(a -> a.getTotalScore() >= 350).count());

        List<MlPrediction> forecasts = predictionRepository.findByCampaignId(campaignId).stream()
                .filter(m -> m.getSpecialty().getId().equals(specialtyId))
                .collect(Collectors.toList());

        DirectionDetailsDto details = new DirectionDetailsDto();
        details.setSpecialty(s);
        details.setPlan(plan);
        details.setApplicationsCount(specApplicants.size());
        details.setDynamics(dynamics);
        details.setScoreDistribution(scoreDistribution);
        details.setForecasts(forecasts);
        return details;
    }

    @Transactional
    public Specialty createSpecialty(Long facultyId, String code, String name, String profileSubjects) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new IllegalArgumentException("Факультет не найден"));

        Specialty specialty = new Specialty();
        specialty.setFaculty(faculty);
        specialty.setCode(code);
        specialty.setName(name);
        specialty.setProfileSubjects(profileSubjects);
        specialty.setIsActive(true);

        return specialtyRepository.save(specialty);
    }
}