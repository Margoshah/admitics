package by.bsuir.admitics.service;

import by.bsuir.admitics.dto.SettingsOverviewDto;
import by.bsuir.admitics.dto.SettingsOverviewDto.AnalyticsThresholdsDto;
import by.bsuir.admitics.dto.SettingsOverviewDto.UpdatePlanRequest;
import by.bsuir.admitics.dto.SettingsOverviewDto.UserSafeDto;
import by.bsuir.admitics.entity.AdmissionCampaign;
import by.bsuir.admitics.entity.AdmissionPlan;
import by.bsuir.admitics.entity.Faculty;
import by.bsuir.admitics.entity.User;
import by.bsuir.admitics.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final FacultyRepository facultyRepository;
    private final SpecialtyRepository specialtyRepository;
    private final AdmissionCampaignRepository campaignRepository;
    private final AdmissionPlanRepository planRepository;
    private final UserRepository userRepository;

    // 1. Получение всех данных для вкладок страницы "Настройки"
    public SettingsOverviewDto getSettingsOverview(Long campaignId) {
        AdmissionCampaign campaign = campaignRepository.findById(campaignId)
                .orElseGet(() -> campaignRepository.findFirstByIsCurrentTrue()
                        .orElseThrow(() -> new RuntimeException("Кампания не найдена")));

        SettingsOverviewDto dto = new SettingsOverviewDto();
        dto.setFaculties(facultyRepository.findAll());
        dto.setSpecialties(specialtyRepository.findAll());
        dto.setStudyForms(List.of("DAYTIME (Дневная)", "EXTRAMURAL (Заочная)", "DISTANCE (Дистанционная)"));
        dto.setFinancingTypes(List.of("BUDGET (Бюджетная)", "PAID (Платная)", "TARGET (Целевая)"));
        dto.setCampaigns(campaignRepository.findAll());
        dto.setAdmissionPlans(planRepository.findByCampaignId(campaign.getId()));

        List<UserSafeDto> safeUsers = new ArrayList<>();
        for (User u : userRepository.findAll()) {
            UserSafeDto uDto = new UserSafeDto();
            uDto.setId(u.getId());
            uDto.setUsername(u.getUsername());
            uDto.setRole(u.getRole());
            uDto.setIsActive(u.getIsActive());
            safeUsers.add(uDto);
        }
        dto.setUsers(safeUsers);

        AnalyticsThresholdsDto thresholds = new AnalyticsThresholdsDto();
        thresholds.setCampaignId(campaign.getId());
        thresholds.setGreenKcpThreshold(campaign.getGreenKcpThreshold());
        thresholds.setYellowKcpThreshold(campaign.getYellowKcpThreshold());
        thresholds.setAnomalyThresholdPct(campaign.getAnomalyThresholdPct());
        dto.setThresholds(thresholds);

        return dto;
    }

    // 2. Добавление нового факультета
    @Transactional
    public Faculty createFaculty(String shortName, String fullName) {
        Faculty f = new Faculty();
        f.setShortName(shortName);
        f.setFullName(fullName);
        f.setIsActive(true);
        return facultyRepository.save(f);
    }

    // 3. Редактирование плана приёма КЦП (бюджетные, платные, целевые места)
    @Transactional
    public AdmissionPlan updateAdmissionPlan(Long planId, UpdatePlanRequest req) {
        AdmissionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("План приёма не найден"));
        if (req.getBudgetPlaces() != null) plan.setBudgetPlaces(req.getBudgetPlaces());
        if (req.getPaidPlaces() != null) plan.setPaidPlaces(req.getPaidPlaces());
        if (req.getTargetPlaces() != null) plan.setTargetPlaces(req.getTargetPlaces());
        if (req.getPassingScoreActual() != null) plan.setPassingScoreActual(req.getPassingScoreActual());
        return planRepository.save(plan);
    }

    // 4. Обновление порогов рисков ("светофора") и порога аномалий
    @Transactional
    public AdmissionCampaign updateThresholds(AnalyticsThresholdsDto req) {
        AdmissionCampaign campaign = campaignRepository.findById(req.getCampaignId())
                .orElseThrow(() -> new IllegalArgumentException("Кампания не найдена"));
        if (req.getGreenKcpThreshold() != null) campaign.setGreenKcpThreshold(req.getGreenKcpThreshold());
        if (req.getYellowKcpThreshold() != null) campaign.setYellowKcpThreshold(req.getYellowKcpThreshold());
        if (req.getAnomalyThresholdPct() != null) campaign.setAnomalyThresholdPct(req.getAnomalyThresholdPct());
        return campaignRepository.save(campaign);
    }
}