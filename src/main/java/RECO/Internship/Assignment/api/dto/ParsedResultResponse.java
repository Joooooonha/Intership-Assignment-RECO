package RECO.Internship.Assignment.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * OCR 파싱 결과 응답 DTO
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParsedResultResponse {

    // 문서 정보
    private String documentType;
    private String date;
    private String time;

    // 차량 정보
    private String vehicleNumber;

    // 중량 정보
    private Integer totalWeight;
    private Integer emptyWeight;
    private Integer netWeight;

    // 거래 정보
    private String customer;
    private String productName;
    private String issuer;

    // GPS 정보
    private GpsInfo gps;

    // 검증 결과
    private ValidationInfo validation;

    // OCR 신뢰도
    private Double confidence;

    @Data
    @Builder
    public static class GpsInfo {
        private Double latitude;
        private Double longitude;
    }

    @Data
    @Builder
    public static class ValidationInfo {
        // 전체 검증 상태 (모든 검증 종합)
        private String overallStatus;
        private String overallMessage;

        // 중량 검증
        private FieldValidation weight;

        // 날짜/시간 검증
        private FieldValidation dateTime;

        // GPS 검증
        private FieldValidation gps;

        // 차량번호 검증
        private FieldValidation vehicle;
    }

    @Data
    @Builder
    public static class FieldValidation {
        private String status;
        private String message;
        private Object value; // 계산된 값 등 (예: calculatedNetWeight)
    }
}
