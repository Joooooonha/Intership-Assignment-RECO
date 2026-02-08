package RECO.Internship.Assignment.api.dto;

import RECO.Internship.Assignment.domain.validator.WeightValidator;
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
        private String status;
        private String message;
        private Integer calculatedNetWeight;
    }
}
