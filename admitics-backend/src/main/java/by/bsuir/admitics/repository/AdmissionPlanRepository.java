package by.bsuir.admitics.repository;

import by.bsuir.admitics.entity.AdmissionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AdmissionPlanRepository extends JpaRepository<AdmissionPlan, Long> {
    List<AdmissionPlan> findByCampaignId(Long campaignId);
    Optional<AdmissionPlan> findByCampaignIdAndSpecialtyId(Long campaignId, Long specialtyId);
}