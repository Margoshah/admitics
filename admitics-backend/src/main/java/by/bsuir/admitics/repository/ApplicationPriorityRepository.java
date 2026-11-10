package by.bsuir.admitics.repository;

import by.bsuir.admitics.entity.ApplicationPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationPriorityRepository extends JpaRepository<ApplicationPriority, Long> {
    List<ApplicationPriority> findBySpecialtyId(Long specialtyId);
    List<ApplicationPriority> findBySpecialtyIdAndPriorityOrder(Long specialtyId, Integer priorityOrder);
    List<ApplicationPriority> findBySpecialtyIdAndIsEnrolledSimulatedTrue(Long specialtyId);
}