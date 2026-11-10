package by.bsuir.admitics.controller;

import by.bsuir.admitics.dto.DirectionsAnalyticsDto;
import by.bsuir.admitics.dto.DirectionsAnalyticsDto.DirectionDetailsDto;
import by.bsuir.admitics.entity.Specialty;
import by.bsuir.admitics.service.SpecialtyService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directions")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @GetMapping
    public ResponseEntity<Object> getAllDirections(@RequestParam(required = false) Long facultyId) {
        List<Specialty> list = (facultyId != null)
                ? specialtyService.getByFaculty(facultyId)
                : specialtyService.getAllSpecialties();
        return ResponseEntity.ok(list);
    }

    // Полная аналитика для страницы 2.3 "Направления"
    @GetMapping("/analytics")
    public ResponseEntity<DirectionsAnalyticsDto> getDirectionsAnalytics(
            @RequestParam(defaultValue = "1") Long campaignId,
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) String studyForm) {
        return ResponseEntity.ok(specialtyService.getDirectionsPageAnalytics(campaignId, facultyId, studyForm));
    }

    // Детальная карточка выбранного направления
    @GetMapping("/{id}/details")
    public ResponseEntity<DirectionDetailsDto> getDirectionDetails(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Long campaignId) {
        return ResponseEntity.ok(specialtyService.getDirectionDetails(id, campaignId));
    }

    @PostMapping
    public ResponseEntity<Specialty> createDirection(@RequestBody CreateSpecialtyRequest request) {
        Specialty created = specialtyService.createSpecialty(
                request.getFacultyId(), request.getCode(),
                request.getName(), request.getProfileSubjects()
        );
        return ResponseEntity.ok(created);
    }

    @Data
    public static class CreateSpecialtyRequest {
        private Long facultyId;
        private String code;
        private String name;
        private String profileSubjects;
    }
}