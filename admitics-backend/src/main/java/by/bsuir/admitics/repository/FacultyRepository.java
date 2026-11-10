package by.bsuir.admitics.repository;

import by.bsuir.admitics.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByShortName(String shortName);
}