package by.bsuir.admitics.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "admission_plans")
public class AdmissionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private AdmissionCampaign campaign;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @Column(name = "budget_places", nullable = false)
    private Integer budgetPlaces = 0;

    @Column(name = "paid_places", nullable = false)
    private Integer paidPlaces = 0;

    @Column(name = "target_places", nullable = false)
    private Integer targetPlaces = 0;

    @Column(name = "passing_score_actual")
    private Integer passingScoreActual = 0;
}