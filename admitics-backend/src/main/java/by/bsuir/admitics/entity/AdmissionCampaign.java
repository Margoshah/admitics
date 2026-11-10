package by.bsuir.admitics.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "admission_campaigns")
public class AdmissionCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer year;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current")
    private Boolean isCurrent = false;

    @Column(name = "green_kcp_threshold", precision = 5, scale = 2)
    private BigDecimal greenKcpThreshold;

    @Column(name = "yellow_kcp_threshold", precision = 5, scale = 2)
    private BigDecimal yellowKcpThreshold;

    @Column(name = "anomaly_threshold_pct", precision = 5, scale = 2)
    private BigDecimal anomalyThresholdPct;
}