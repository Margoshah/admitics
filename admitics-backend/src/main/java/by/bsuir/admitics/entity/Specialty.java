package by.bsuir.admitics.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "specialties", uniqueConstraints = {@UniqueConstraint(columnNames = {"code", "faculty_id"}) // Уникальна связка кода и факультета!
})
public class Specialty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @Column(length = 20, nullable = false)
    private String code;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(name = "profile_subjects", length = 255)
    private String profileSubjects;

    @Column(name = "is_active")
    private Boolean isActive = true;
}