package by.bsuir.admitics.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "applicants")
public class Applicant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private AdmissionCampaign campaign;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "region_id")
    private Region region;

    @Column(name = "applicant_code", length = 50, unique = true, nullable = false)
    private String applicantCode;

    private Integer age;

    @Column(name = "locality_type", length = 20)
    private String localityType;

    @Column(name = "school_type", length = 30)
    private String schoolType;

    @Column(name = "needs_dormitory")
    private Boolean needsDormitory = false;

    @Column(name = "lang_score")
    private Integer langScore = 0;

    @Column(name = "prof1_score")
    private Integer prof1Score = 0;

    @Column(name = "prof2_score")
    private Integer prof2Score = 0;

    @Column(name = "cert_score")
    private Integer certScore = 0;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore = 0;

    @Column(name = "financing_type", length = 20)
    private String financingType = "BUDGET";

    @Column(name = "study_form", length = 20)
    private String studyForm = "DAYTIME";

    @Column(name = "privilege_type", length = 30)
    private String privilegeType = "NONE";

    @Column(length = 20, nullable = false)
    private String status = "ACTIVE";

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("priorityOrder ASC")
    private List<ApplicationPriority> priorities = new ArrayList<>();
}