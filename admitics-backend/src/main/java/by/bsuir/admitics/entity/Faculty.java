package by.bsuir.admitics.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "faculties")
public class Faculty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_name", length = 20, unique = true, nullable = false)
    private String shortName;

    @Column(name = "full_name", length = 255, nullable = false)
    private String fullName;

    @Column(name = "is_active")
    private Boolean isActive = true;
}