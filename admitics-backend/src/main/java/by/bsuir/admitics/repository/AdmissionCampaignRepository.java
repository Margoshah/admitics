package by.bsuir.admitics.repository;

import by.bsuir.admitics.entity.AdmissionCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdmissionCampaignRepository extends JpaRepository<AdmissionCampaign, Long> {
    Optional<AdmissionCampaign> findByYear(Integer year);
    Optional<AdmissionCampaign> findFirstByIsCurrentTrue();
}