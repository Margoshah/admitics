package by.bsuir.admitics.config;

import by.bsuir.admitics.entity.*;
import by.bsuir.admitics.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import by.bsuir.admitics.repository.ApplicantRepository;
import by.bsuir.admitics.service.EnrollmentSimulationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final FacultyRepository facultyRepository;
    private final SpecialtyRepository specialtyRepository;
    private final RegionRepository regionRepository;
    private final AdmissionCampaignRepository campaignRepository;
    private final AdmissionPlanRepository planRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ApplicantRepository applicantRepository;
    private final EnrollmentSimulationService simulationService;

    @Override
    public void run(String... args) {
        if (facultyRepository.count() > 0) {
            return; // База уже проинициализирована
        }

        // 1. Справочник регионов Республики Беларусь
        List<String> regions = List.of(
                "г. Минск", "Минская область", "Брестская область",
                "Витебская область", "Гомельская область", "Гродненская область", "Могилёвская область"
        );
        for (String rName : regions) {
            Region r = new Region();
            r.setName(rName);
            regionRepository.save(r);
        }

        // 2. Факультеты БГУИР
        Faculty fkp = createFaculty("ФКП", "Факультет компьютерного проектирования");
        Faculty fitu = createFaculty("ФИТУ", "Факультет информационных технологий и управления");
        Faculty fre = createFaculty("ФРЭ", "Факультет радиотехники и электроники");
        Faculty fksis = createFaculty("ФКСиС", "Факультет компьютерных систем и сетей");
        Faculty fib = createFaculty("ФИБ", "Факультет информационной безопасности");
        Faculty ief = createFaculty("ИЭФ", "Инженерно-экономический факультет");

        // 3. Полный официальный перечень специальностей БГУИР (чистые коды ОКРБ)
        // --- ФКП (5 специальностей) ---
        Specialty fkp_ist = createSpecialty(fkp, "6-05-0611-01", "Информационные системы и технологии", "Математика, Физика");
        Specialty fkp_ce  = createSpecialty(fkp, "6-05-0611-05", "Компьютерная инженерия", "Математика, Физика");
        Specialty fkp_pi  = createSpecialty(fkp, "6-05-0612-01", "Программная инженерия", "Математика, Физика");
        Specialty fkp_est = createSpecialty(fkp, "6-05-0713-02", "Электронные системы и технологии", "Математика, Физика");
        Specialty fkp_em  = createSpecialty(fkp, "6-05-0713-06", "Электронное машиностроение", "Математика, Физика");

        // --- ФИТУ (5 специальностей) ---
        Specialty fitu_ist = createSpecialty(fitu, "6-05-0611-01", "Информационные системы и технологии", "Математика, Физика");
        Specialty fitu_ai  = createSpecialty(fitu, "6-05-0611-03", "Искусственный интеллект", "Математика, Физика");
        Specialty fitu_cfs = createSpecialty(fitu, "6-05-0611-08", "Киберфизические системы", "Математика, Физика");
        Specialty fitu_sim = createSpecialty(fitu, "6-05-0612-03", "Системы управления информацией", "Математика, Физика");
        Specialty fitu_est = createSpecialty(fitu, "6-05-0713-02", "Электронные системы и технологии", "Математика, Физика");

        // --- ФРЭ (5 специальностей) ---
        Specialty fre_rt   = createSpecialty(fre, "6-05-0713-03", "Радиосистемы и радиотехнологии", "Математика, Физика");
        Specialty fre_nano = createSpecialty(fre, "6-05-0717-01", "Нанотехнологии и наноматериалы", "Математика, Физика");
        Specialty fre_ipd  = createSpecialty(fre, "6-05-0719-01", "Инженерно-педагогическая деятельность", "Математика, Физика");
        Specialty fre_ius  = createSpecialty(fre, "7-07-0713-01", "Информационные и управляющие системы физических установок", "Математика, Физика");
        Specialty fre_mne  = createSpecialty(fre, "7-07-0713-02", "Микро- и наноэлектроника", "Математика, Физика");

        // --- ФКСиС (3 специальности) ---
        Specialty fksis_ce  = createSpecialty(fksis, "6-05-0611-05", "Компьютерная инженерия", "Математика, Физика");
        Specialty fksis_pi  = createSpecialty(fksis, "6-05-0612-01", "Программная инженерия", "Математика, Физика");
        Specialty fksis_itp = createSpecialty(fksis, "6-05-0612-02", "Информатика и технологии программирования", "Математика, Физика");

        // --- ФИБ (3 специальности) ---
        Specialty fib_ib   = createSpecialty(fib, "6-05-0611-02", "Информационная безопасность", "Математика, Физика");
        Specialty fib_ssi  = createSpecialty(fib, "6-05-0611-06", "Системы и сети инфокоммуникаций", "Математика, Физика");
        Specialty fib_svch = createSpecialty(fib, "7-07-0713-03", "Сверхвысокочастотные системы", "Математика, Физика");

        // --- ИЭФ (3 специальности) ---
        Specialty ief_ist = createSpecialty(ief, "6-05-0611-01", "Информационные системы и технологии", "Математика, Иностранный язык");
        Specialty ief_ee  = createSpecialty(ief, "6-05-0611-04", "Электронная экономика", "Математика, Иностранный язык");
        Specialty ief_dm  = createSpecialty(ief, "6-05-0611-07", "Цифровой маркетинг", "Математика, Иностранный язык");

        // 4. Приёмная кампания 2026 года (с 12.07.2026 по 03.08.2026 — охватывает бюджет и платное)
        AdmissionCampaign c2026 = new AdmissionCampaign();
        c2026.setYear(2026);
        c2026.setName("Приёмная кампания БГУИР 2026");
        c2026.setStartDate(LocalDate.of(2026, 7, 12));
        c2026.setEndDate(LocalDate.of(2026, 8, 3));
        c2026.setIsCurrent(true);
        c2026.setGreenKcpThreshold(new BigDecimal("100.00"));
        c2026.setYellowKcpThreshold(new BigDecimal("70.00"));
        c2026.setAnomalyThresholdPct(new BigDecimal("25.00"));
        campaignRepository.save(c2026);

        // 5. Планы приёма (КЦП: бюджет, платное, целевое) и проходные баллы
        // ФКП
        createPlan(c2026, fkp_ist, 55, 45, 5, 380);
        createPlan(c2026, fkp_ce,  50, 40, 4, 367);
        createPlan(c2026, fkp_pi,  55, 45, 5, 385);
        createPlan(c2026, fkp_est, 45, 30, 4, 348);
        createPlan(c2026, fkp_em,  30, 20, 2, 334);

        // ФИТУ
        createPlan(c2026, fitu_ist, 50, 40, 5, 392);
        createPlan(c2026, fitu_ai,  55, 35, 5, 380);
        createPlan(c2026, fitu_cfs, 40, 30, 3, 358);
        createPlan(c2026, fitu_sim, 50, 35, 4, 376);
        createPlan(c2026, fitu_est, 35, 20, 2, 368);

        // ФРЭ
        createPlan(c2026, fre_rt,   60, 25, 6, 279);
        createPlan(c2026, fre_nano, 30, 15, 2, 313);
        createPlan(c2026, fre_ipd,  25, 15, 2, 260);
        createPlan(c2026, fre_ius,  30, 20, 2, 330);
        createPlan(c2026, fre_mne,  45, 20, 4, 284);

        // ФКСиС
        createPlan(c2026, fksis_ce,  75, 50, 6, 366);
        createPlan(c2026, fksis_pi,  90, 60, 8, 394);
        createPlan(c2026, fksis_itp, 70, 50, 6, 388);

        // ФИБ
        createPlan(c2026, fib_ib,   65, 40, 6, 366);
        createPlan(c2026, fib_ssi,  55, 30, 5, 340);
        createPlan(c2026, fib_svch, 35, 20, 3, 325);

        // ИЭФ
        createPlan(c2026, ief_ist, 45, 50, 4, 372);
        createPlan(c2026, ief_ee,  45, 60, 4, 394);
        createPlan(c2026, ief_dm,  40, 55, 3, 390);

        // 6. Пользователи системы (Пароли теперь хешируются BCrypt)
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash(encoder.encode("admin123")); // Хешируем пароль
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);

        User operator = new User();
        operator.setUsername("ivanov");
        operator.setPasswordHash(encoder.encode("operator123")); // Хешируем пароль
        operator.setRole("ROLE_OPERATOR");
        userRepository.save(operator);

        // 7. Стартовые уведомления
        createNotification(operator, "ФКСиС — 6-05-0612-01 Программная инженерия",
                "Выявлен резкий рост заявлений на +34% за последние сутки.", "ANOMALY");
        createNotification(operator, "ФРЭ — 6-05-0719-01 Инженерно-педагогическая деятельность",
                "Прогноз заполнения КЦП составляет 68%. Требуется дополнительная работа с абитуриентами.", "RISK_YELLOW");
        createNotification(operator, "ИЭФ — 6-05-0611-04 Электронная экономика",
                "Прогнозируемое количество заявлений превышает количество мест на +18%.", "INFO");
        // 8. ГЕНЕРАЦИЯ ТЕСТОВЫХ АБИТУРИЕНТОВ (для заполнения графиков, карты Беларуси и статистики)
        List<Specialty> allSpecs = specialtyRepository.findAll();
        List<Region> allRegions = regionRepository.findAll();
        Random random = new Random();

        List<Applicant> mockApplicants = new ArrayList<>();
        LocalDate startDate = LocalDate.of(2026, 7, 12);

        for (int i = 1; i <= 80; i++) {
            Applicant app = new Applicant();
            app.setCampaign(c2026);
            app.setApplicantCode("MOCK_CODE_" + i);

            int lang = 60 + random.nextInt(39);
            int prof1 = 65 + random.nextInt(34);
            int prof2 = 65 + random.nextInt(34);
            int cert = 70 + random.nextInt(30);

            app.setLangScore(lang);
            app.setProf1Score(prof1);
            app.setProf2Score(prof2);
            app.setCertScore(cert);
            app.setTotalScore(lang + prof1 + prof2 + cert);

            app.setFinancingType(random.nextDouble() > 0.25 ? "BUDGET" : "PAID");
            app.setStudyForm("DAYTIME");
            app.setApplicationDate(startDate.plusDays(random.nextInt(20)));

            if (!allRegions.isEmpty()) {
                app.setRegion(allRegions.get(random.nextInt(allRegions.size())));
            }

            List<ApplicationPriority> priorities = new ArrayList<>();
            if (!allSpecs.isEmpty()) {
                Specialty mainSpec = allSpecs.get(random.nextInt(allSpecs.size()));
                ApplicationPriority pr1 = new ApplicationPriority();
                pr1.setApplicant(app);
                pr1.setSpecialty(mainSpec);
                pr1.setPriorityOrder(1);
                priorities.add(pr1);
            }
            app.setPriorities(priorities);
            mockApplicants.add(app);
        }

        applicantRepository.saveAll(mockApplicants);
        simulationService.simulateByPriorities(c2026.getId());

    }

    private Faculty createFaculty(String shortName, String fullName) {
        Faculty f = new Faculty();
        f.setShortName(shortName);
        f.setFullName(fullName);
        f.setIsActive(true);
        return facultyRepository.save(f);
    }

    private Specialty createSpecialty(Faculty faculty, String code, String name, String subjects) {
        Specialty s = new Specialty();
        s.setFaculty(faculty);
        s.setCode(code);
        s.setName(name);
        s.setProfileSubjects(subjects);
        s.setIsActive(true);
        return specialtyRepository.save(s);
    }

    private void createPlan(AdmissionCampaign c, Specialty s, int budget, int paid, int target, int score) {
        AdmissionPlan p = new AdmissionPlan();
        p.setCampaign(c);
        p.setSpecialty(s);
        p.setBudgetPlaces(budget);
        p.setPaidPlaces(paid);
        p.setTargetPlaces(target);
        p.setPassingScoreActual(score);
        planRepository.save(p);
    }

    private void createNotification(User user, String title, String msg, String type) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(msg);
        n.setType(type);
        notificationRepository.save(n);
    }
}