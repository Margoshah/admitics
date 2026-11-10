package by.bsuir.admitics.service;

import by.bsuir.admitics.entity.*;
import by.bsuir.admitics.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final ApplicantRepository applicantRepository;
    private final AdmissionCampaignRepository campaignRepository;
    private final SpecialtyRepository specialtyRepository;
    private final RegionRepository regionRepository;

    // Внедряем сервис симуляции, чтобы сразу после импорта запустить пересчет
    private final EnrollmentSimulationService simulationService;

    @Transactional
    public String importApplicantsCsv(MultipartFile file, Long campaignId) {
        AdmissionCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Кампания не найдена"));

        List<Applicant> validApplicants = new ArrayList<>();

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord record : csvRecords) {
                // 1. Обезличивание: хешируем личный номер паспорта (или ФИО) в applicant_code
                String rawPersonalId = record.get("personal_id");
                String anonymizedCode = hashSHA256(rawPersonalId);

                // Если абитуриент с таким кодом уже загружен, пропускаем (или обновляем)
                if (applicantRepository.findByApplicantCode(anonymizedCode).isPresent()) {
                    continue;
                }

                Applicant applicant = new Applicant();
                applicant.setCampaign(campaign);
                applicant.setApplicantCode(anonymizedCode);

                // 2. Чтение основных данных (баллы ЦТ, аттестат)
                applicant.setLangScore(Integer.parseInt(record.get("lang_score")));
                applicant.setProf1Score(Integer.parseInt(record.get("prof1_score")));
                applicant.setProf2Score(Integer.parseInt(record.get("prof2_score")));
                applicant.setCertScore(Integer.parseInt(record.get("cert_score")));
                applicant.setTotalScore(applicant.getLangScore() + applicant.getProf1Score() + applicant.getProf2Score() + applicant.getCertScore());

                applicant.setFinancingType(record.get("financing_type")); // BUDGET, PAID, TARGET
                applicant.setStudyForm(record.get("study_form")); // DAYTIME, EXTRAMURAL
                applicant.setApplicationDate(LocalDate.now());

                // Парсинг региона (если указан)
                String regionName = record.get("region");
                if (regionName != null && !regionName.isBlank()) {
                    regionRepository.findByName(regionName).ifPresent(applicant::setRegion);
                }

                // 3. Обработка приоритетов (в CSV столбцы priority_1, priority_2 и т.д. содержат код специальности)
                List<ApplicationPriority> priorities = new ArrayList<>();
                for (int i = 1; i <= 5; i++) {
                    String specCode = record.get("priority_" + i);
                    if (specCode != null && !specCode.isBlank()) {
                        Specialty spec = specialtyRepository.findByCode(specCode).orElse(null);
                        if (spec != null) {
                            ApplicationPriority priority = new ApplicationPriority();
                            priority.setApplicant(applicant);
                            priority.setSpecialty(spec);
                            priority.setPriorityOrder(i);
                            priorities.add(priority);
                        }
                    }
                }
                applicant.setPriorities(priorities);
                validApplicants.add(applicant);
            }

            // Пакетное сохранение всех заявлений
            applicantRepository.saveAll(validApplicants);

            // ЗАПУСК АЛГОРИТМА СИМУЛЯЦИИ сразу после сохранения (как описано в п. 1.2 записки)
            simulationService.simulateByPriorities(campaignId);

            return "Успешно импортировано заявлений: " + validApplicants.size();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при разборе CSV файла: " + e.getMessage());
        }
    }

    // Метод для криптографического обезличивания данных (SHA-256)
    private String hashSHA256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}