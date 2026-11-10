package by.bsuir.admitics.repository;

import by.bsuir.admitics.entity.MlPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MlPredictionRepository extends JpaRepository<MlPrediction, Long> {
    List<MlPrediction> findByCampaignId(Long campaignId);
    List<MlPrediction> findByCampaignIdAndSpecialtyId(Long campaignId, Long specialtyId);
}