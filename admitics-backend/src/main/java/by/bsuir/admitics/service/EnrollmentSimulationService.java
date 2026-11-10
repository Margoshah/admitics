package by.bsuir.admitics.service;

import by.bsuir.admitics.entity.AdmissionPlan;
import by.bsuir.admitics.entity.Applicant;
import by.bsuir.admitics.entity.ApplicationPriority;
import by.bsuir.admitics.repository.AdmissionPlanRepository;
import by.bsuir.admitics.repository.ApplicantRepository;
import by.bsuir.admitics.repository.ApplicationPriorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EnrollmentSimulationService {

    private final AdmissionPlanRepository planRepository;
    private final ApplicantRepository applicantRepository;
    private final ApplicationPriorityRepository priorityRepository;

    @Transactional
    public void simulateByPriorities(Long campaignId) {
        // 1. Загрузка планов приёма (КЦП) для текущей кампании
        List<AdmissionPlan> plans = planRepository.findByCampaignId(campaignId);

        // 2. Загрузка всех активных заявлений, отсортированных по убыванию конкурсного балла (от 400 до 0)
        List<Applicant> applicants = applicantRepository
                .findByCampaignIdAndStatusOrderByTotalScoreDesc(campaignId, "ACTIVE");

        if (plans.isEmpty() || applicants.isEmpty()) {
            System.out.println("Недостаточно данных для симуляции (нет планов или анкет).");
            return;
        }

        // Подготавливаем словари для отслеживания свободных бюджетных мест и списков зачисленных
        Map<Long, List<Integer>> enrolledScores = new HashMap<>();
        Map<Long, Integer> freeBudgetQuotas = new HashMap<>();

        // Сброс предыдущей симуляции
        List<ApplicationPriority> allPrioritiesToUpdate = new ArrayList<>();

        for (AdmissionPlan plan : plans) {
            freeBudgetQuotas.put(plan.getSpecialty().getId(), plan.getBudgetPlaces());
            enrolledScores.put(plan.getSpecialty().getId(), new ArrayList<>());
        }

        // 3. Каскадное распределение абитуриентов по приоритетам (1..N)
        for (Applicant app : applicants) {
            // Для абитуриентов, претендующих на бюджет (по ТЗ мы пока симулируем бюджет)
            if ("BUDGET".equalsIgnoreCase(app.getFinancingType())) {
                for (ApplicationPriority prio : app.getPriorities()) {
                    Long specId = prio.getSpecialty().getId();
                    int available = freeBudgetQuotas.getOrDefault(specId, 0);

                    // Сбрасываем старый флаг
                    prio.setIsEnrolledSimulated(false);

                    // Если на специальности ещё есть свободные бюджетные места
                    if (available > 0) {
                        prio.setIsEnrolledSimulated(true); // Зачислен по симуляции
                        freeBudgetQuotas.put(specId, available - 1); // Уменьшаем квоту
                        enrolledScores.get(specId).add(app.getTotalScore()); // Сохраняем балл
                        allPrioritiesToUpdate.add(prio);

                        // Если абитуриент зачислен на этот приоритет, следующие (менее важные) приоритеты не рассматриваем
                        break;
                    }
                    allPrioritiesToUpdate.add(prio); // Сохраняем сброшенный флаг, если не прошел
                }
            }
        }

        // Пакетно обновляем флаги приоритетов в БД
        priorityRepository.saveAll(allPrioritiesToUpdate);

        // 4. Расчёт и обновление текущих проходных баллов
        updatePassingScoresInDatabase(plans, enrolledScores);
    }

    private void updatePassingScoresInDatabase(List<AdmissionPlan> plans, Map<Long, List<Integer>> enrolledScores) {
        for (AdmissionPlan plan : plans) {
            List<Integer> scores = enrolledScores.get(plan.getSpecialty().getId());
            if (scores != null && !scores.isEmpty()) {
                // Проходной балл — это минимальный балл среди всех симулированно зачисленных
                // Так как абитуриенты шли по убыванию, последний добавленный балл и есть минимальный
                Integer passingScore = scores.get(scores.size() - 1);
                plan.setPassingScoreActual(passingScore);
            } else {
                plan.setPassingScoreActual(0); // Если на специальность никто не подал, балл 0
            }
        }
        // Сохраняем обновленные проходные баллы
        planRepository.saveAll(plans);
    }
}