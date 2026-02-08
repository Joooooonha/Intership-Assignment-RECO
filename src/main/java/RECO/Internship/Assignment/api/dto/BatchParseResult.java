package RECO.Internship.Assignment.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * 배치 파싱 개별 파일 결과 DTO
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchParseResult {

    private String filename;
    private boolean success;
    private ParsedResultResponse result;
    private String error;

    public static BatchParseResult success(String filename, ParsedResultResponse result) {
        return BatchParseResult.builder()
                .filename(filename)
                .success(true)
                .result(result)
                .build();
    }

    public static BatchParseResult error(String filename, String errorMessage) {
        return BatchParseResult.builder()
                .filename(filename)
                .success(false)
                .error(errorMessage)
                .build();
    }
}
