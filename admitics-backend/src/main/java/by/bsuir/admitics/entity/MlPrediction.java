package by.bsuir.admitics.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ml_predictions")
public class MlPrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private AdmissionCampaign campaign;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @Column(name = "prediction_type", length = 50, nullable = false)
    private String predictionType; // APPLICATIONS_COUNT, PASSING_SCORE, KCP_FILL_PCT

    @Column(name = "predicted_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal predictedValue;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}