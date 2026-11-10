package by.bsuir.admitics.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportPayloadDto {
    private String reportType;
    private String reportTitle;
    private String campaignName;
    private LocalDateTime generatedAt;
    private List<String> columns;
    private List<ReportRowDto> rows;
    private List<String> conclusions;

    @Data
    public static class ReportRowDto {
        private List<String> values;

        public ReportRowDto(List<String> values) {
            this.values = values;
        }
    }
}