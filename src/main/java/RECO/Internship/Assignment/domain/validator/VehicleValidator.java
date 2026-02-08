package RECO.Internship.Assignment.domain.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 차량번호 유효성 검증 클래스
 */
@Component
public class VehicleValidator {

    private static final Logger log = LoggerFactory.getLogger(VehicleValidator.class);

    // 한국 차량번호 패턴
    // 신형식: 123가1234 (지역명 없음, 2~3자리 숫자 + 한글 + 4자리 숫자)
    // 구형식: 서울12가1234 (지역명 + 2자리 숫자 + 한글 + 4자리 숫자)
    private static final Pattern NEW_FORMAT = Pattern.compile("^\\d{2,3}[가-힣]\\d{4}$");
    private static final Pattern OLD_FORMAT = Pattern.compile("^[가-힣]{2}\\d{2}[가-힣]\\d{4}$");

    // 영업용/자가용 구분 한글 (앞자리)
    private static final String COMMERCIAL_CHARS = "아바사자배허호";
    private static final String PRIVATE_CHARS = "가나다라마거너더러머버서어저고노도로모보소오조구누두루무부수우주";

    /**
     * 차량번호 유효성 검증
     * 
     * @param vehicleNumber 차량번호 문자열
     * @return 검증 결과
     */
    public ValidationResult validateVehicleNumber(String vehicleNumber) {
        if (vehicleNumber == null || vehicleNumber.isBlank()) {
            return ValidationResult.cannotValidate("차량번호가 없습니다");
        }

        String trimmed = vehicleNumber.trim().replaceAll("\\s+", "");

        // 형식 체크
        boolean isNewFormat = NEW_FORMAT.matcher(trimmed).matches();
        boolean isOldFormat = OLD_FORMAT.matcher(trimmed).matches();

        if (!isNewFormat && !isOldFormat) {
            // 부분 매칭 시도 (OCR 오류 고려)
            if (containsKoreanAndDigits(trimmed)) {
                log.warn("비표준 차량번호 형식: {}", vehicleNumber);
                return ValidationResult.warning("비표준 형식이지만 차량번호로 추정: " + vehicleNumber);
            }
            log.warn("잘못된 차량번호 형식: {}", vehicleNumber);
            return ValidationResult.invalid("차량번호 형식이 올바르지 않습니다: " + vehicleNumber);
        }

        log.debug("차량번호 검증 성공: {}", vehicleNumber);
        String format = isNewFormat ? "신형식" : "구형식";
        return ValidationResult.valid(format + " 차량번호 유효: " + vehicleNumber);
    }

    /**
     * 한글과 숫자가 포함되어 있는지 확인 (부분 인식 허용)
     */
    private boolean containsKoreanAndDigits(String str) {
        boolean hasKorean = str.chars().anyMatch(c -> c >= '가' && c <= '힣');
        boolean hasDigits = str.chars().anyMatch(Character::isDigit);
        return hasKorean && hasDigits;
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
