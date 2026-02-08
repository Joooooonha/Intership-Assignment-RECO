package RECO.Internship.Assignment.domain.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * GPS 좌표 유효성 검증 클래스
 */
@Component
public class GpsValidator {

    private static final Logger log = LoggerFactory.getLogger(GpsValidator.class);

    // 한국 영토 범위 (대략적인 범위)
    private static final double MIN_LATITUDE = 33.0; // 최남단 (마라도)
    private static final double MAX_LATITUDE = 43.0; // 최북단
    private static final double MIN_LONGITUDE = 124.0; // 최서단
    private static final double MAX_LONGITUDE = 132.0; // 최동단 (독도)

    /**
     * GPS 좌표 유효성 검증
     * 
     * @param latitude  위도
     * @param longitude 경도
     * @return 검증 결과
     */
    public ValidationResult validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return ValidationResult.cannotValidate("GPS 좌표가 없습니다");
        }

        // 기본 위/경도 범위 체크
        if (latitude < -90 || latitude > 90) {
            log.warn("잘못된 위도: {}", latitude);
            return ValidationResult.invalid("위도는 -90 ~ 90 범위여야 합니다: " + latitude);
        }

        if (longitude < -180 || longitude > 180) {
            log.warn("잘못된 경도: {}", longitude);
            return ValidationResult.invalid("경도는 -180 ~ 180 범위여야 합니다: " + longitude);
        }

        // 한국 범위 체크
        boolean inKorea = isInKoreaRange(latitude, longitude);
        if (!inKorea) {
            log.warn("한국 범위 외 GPS: {}, {}", latitude, longitude);
            return ValidationResult.warning(
                    String.format("한국 범위 외 좌표입니다: (%.6f, %.6f)", latitude, longitude));
        }

        log.debug("GPS 검증 성공: {}, {}", latitude, longitude);
        return ValidationResult.valid(
                String.format("GPS 좌표 유효: (%.6f, %.6f)", latitude, longitude));
    }

    /**
     * 좌표 배열 검증 (FieldExtractor 결과용)
     */
    public ValidationResult validateCoordinates(double[] coordinates) {
        if (coordinates == null || coordinates.length < 2) {
            return ValidationResult.cannotValidate("GPS 좌표가 없습니다");
        }
        return validateCoordinates(coordinates[0], coordinates[1]);
    }

    /**
     * 한국 범위 내 좌표인지 확인
     */
    public boolean isInKoreaRange(double latitude, double longitude) {
        return latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE &&
                longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE;
    }

    /**
     * 검증 결과 레코드
     */
    public record ValidationResult(
            ValidationStatus status,
            String message) {
        public static ValidationResult valid(String message) {
            return new ValidationResult(ValidationStatus.VALID, message);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(ValidationStatus.INVALID, message);
        }

        public static ValidationResult warning(String message) {
            return new ValidationResult(ValidationStatus.WARNING, message);
        }

        public static ValidationResult cannotValidate(String message) {
            return new ValidationResult(ValidationStatus.CANNOT_VALIDATE, message);
        }

        public boolean isValid() {
            return status == ValidationStatus.VALID || status == ValidationStatus.WARNING;
        }
    }

    public enum ValidationStatus {
        VALID,
        INVALID,
        WARNING,
        CANNOT_VALIDATE
    }
}
