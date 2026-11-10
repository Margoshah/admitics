package by.bsuir.admitics.repository;

import by.bsuir.admitics.entity.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
    List<Applicant> findByCampaignId(Long campaignId);
    List<Applicant> findByCampaignIdAndStatusOrderByTotalScoreDesc(Long campaignId, String status);
    Optional<Applicant> findByApplicantCode(String applicantCode);
}