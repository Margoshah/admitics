package by.bsuir.admitics.service;

import by.bsuir.admitics.dto.ReportPayloadDto;
import by.bsuir.admitics.dto.ReportPayloadDto.ReportRowDto;
import by.bsuir.admitics.entity.*;
import by.bsuir.admitics.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AdmissionCampaignRepository campaignRepository;
    private final AdmissionPlanRepository planRepository;
    private final ApplicantRepository applicantRepository;
    private final MlPredictionRepository predictionRepository;
    private final NotificationRepository notificationRepository;

    public ReportPayloadDto generateReport(Long campaignId, String type) {
        AdmissionCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Кампания не найдена"));

        List<AdmissionPlan> plans = planRepository.findByCampaignId(campaignId);
        List<Applicant> applicants = applicantRepository.findByCampaignId(campaignId);

        ReportPayloadDto report = new ReportPayloadDto();
        report.setReportType(type.toUpperCase());
        report.setCampaignName(campaign.getName());
        report.setGeneratedAt(LocalDateTime.now());

        List<ReportRowDto> rows = new ArrayList<>();
        List<String> conclusions = new ArrayList<>();

        switch (type.toUpperCase()) {
            case "DIRECTIONS":
            case "COMPETITION":
                report.setReportTitle("Отчёт по направлениям, конкурсу и выполнению КЦП");
                report.setColumns(List.of("Факультет", "Код", "Специальность", "Бюджет", "Платное", "Целевое", "Всего мест", "Проходной балл"));
                for (AdmissionPlan p : plans) {
                    int total = p.getBudgetPlaces() + p.getPaidPlaces() + p.getTargetPlaces();
                    rows.add(new ReportRowDto(List.of(
                            p.getSpecialty().getFaculty().getShortName(),
                            p.getSpecialty().getCode(),
                            p.getSpecialty().getName(),
                            String.valueOf(p.getBudgetPlaces()),
                            String.valueOf(p.getPaidPlaces()),
                            String.valueOf(p.getTargetPlaces()),
                            String.valueOf(total),
                            String.valueOf(p.getPassingScoreActual() != null ? p.getPassingScoreActual() : 0)
                    )));
                }
                conclusions.add("Проанализировано направлений подготовки: " + plans.size() + ".");
                break;

            case "APPLICANTS":
                report.setReportTitle("Отчёт по составу и характеристикам абитуриентов");
                report.setColumns(List.of("Код абитуриента", "Сумма баллов", "Язык", "Профиль 1", "Профиль 2", "Аттестат", "Финансирование", "Форма"));
                for (Applicant a : applicants) {
                    rows.add(new ReportRowDto(List.of(
                            a.getApplicantCode().substring(0, Math.min(12, a.getApplicantCode().length())),
                            String.valueOf(a.getTotalScore()),
                            String.valueOf(a.getLangScore()),
                            String.valueOf(a.getProf1Score()),
                            String.valueOf(a.getProf2Score()),
                            String.valueOf(a.getCertScore()),
                            a.getFinancingType(),
                            a.getStudyForm()
                    )));
                }
                conclusions.add("Общее число зарегистрированных анкет: " + applicants.size() + ".");
                break;

            case "FORECASTS":
                report.setReportTitle("Аналитический отчёт по ML-прогнозам");
                report.setColumns(List.of("Факультет", "Код", "Специальность", "Тип прогноза", "Прогнозное значение", "Дата расчёта"));
                List<MlPrediction> predictions = predictionRepository.findByCampaignId(campaignId);
                for (MlPrediction m : predictions) {
                    rows.add(new ReportRowDto(List.of(
                            m.getSpecialty().getFaculty().getShortName(),
                            m.getSpecialty().getCode(),
                            m.getSpecialty().getName(),
                            m.getPredictionType(),
                            String.valueOf(m.getPredictedValue()),
                            m.getCreatedAt() != null ? m.getCreatedAt().toLocalDate().toString() : "-"
                    )));
                }
                conclusions.add("Сформировано прогнозных оценок ML-модели: " + predictions.size() + ".");
                break;

            case "RISKS":
                report.setReportTitle("Отчёт по аномалиям и рискам невыполнения КЦП");
                report.setColumns(List.of("Тип события", "Заголовок", "Описание", "Дата выявления"));
                List<Notification> events = notificationRepository.findAllByOrderByCreatedAtDesc();
                for (Notification n : events) {
                    rows.add(new ReportRowDto(List.of(
                            n.getType(),
                            n.getTitle(),
                            n.getMessage(),
                            n.getCreatedAt() != null ? n.getCreatedAt().toLocalDate().toString() : "-"
                    )));
                }
                conclusions.add("Зафиксировано инцидентов и рисковых отклонений: " + events.size() + ".");
                break;

            default: // SUMMARY, DYNAMICS, COMPARISON
                report.setReportTitle("Сводный отчёт по приёмной кампании");
                report.setColumns(List.of("Показатель", "Значение"));
                int totalPlaces = plans.stream().mapToInt(p -> p.getBudgetPlaces() + p.getPaidPlaces() + p.getTargetPlaces()).sum();
                double avgScore = applicants.stream().mapToInt(Applicant::getTotalScore).average().orElse(0.0);
                rows.add(new ReportRowDto(List.of("Приёмная кампания", campaign.getName())));
                rows.add(new ReportRowDto(List.of("Количество специальностей", String.valueOf(plans.size()))));
                rows.add(new ReportRowDto(List.of("Суммарный план приёма (КЦП)", String.valueOf(totalPlaces))));
                rows.add(new ReportRowDto(List.of("Подано заявлений всего", String.valueOf(applicants.size()))));
                rows.add(new ReportRowDto(List.of("Средний балл абитуриентов", String.valueOf(Math.round(avgScore * 10.0) / 10.0))));
                conclusions.add("Сводные показатели кампании соответствуют утверждённому графику.");
                break;
        }

        report.setRows(rows);
        report.setConclusions(conclusions);
        return report;
    }

    // Генерация CSV-файла (с кодировкой UTF-8 BOM для корректного открытия в MS Excel)
    public byte[] exportReportToCsv(Long campaignId, String type) {
        ReportPayloadDto report = generateReport(campaignId, type);
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // UTF-8 BOM для русского языка в Excel

        sb.append(String.join(";", report.getColumns())).append("\r\n");
        for (ReportRowDto row : report.getRows()) {
            List<String> escaped = new ArrayList<>();
            for (String val : row.getValues()) {
                String safeVal = val != null ? val.replace(";", ",") : "";
                escaped.add(safeVal);
            }
            sb.append(String.join(";", escaped)).append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}