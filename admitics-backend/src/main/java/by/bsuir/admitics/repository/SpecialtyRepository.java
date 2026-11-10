package by.bsuir.admitics.repository;

import by.bsuir.admitics.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
    Optional<Specialty> findByCode(String code);
    List<Specialty> findByFacultyId(Long facultyId);
}