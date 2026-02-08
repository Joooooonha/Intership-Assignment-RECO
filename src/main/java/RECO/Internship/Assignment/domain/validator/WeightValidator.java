package RECO.Internship.Assignment.domain.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 중량 데이터 검증 클래스
 * 총중량 - 공차중량 = 실중량 검증 등
 */
@Component
public class WeightValidator {

    private static final Logger log = LoggerFactory.getLogger(WeightValidator.class);

    // 허용 오차 (kg) - OCR 인식 오류 고려
    private static final int TOLERANCE = 10;

    /**
     * 중량 계산 검증
     * 총중량 - 공차중량 = 실중량 확인
     * 
     * @param totalWeight 총중량 (kg)
     * @param emptyWeight 공차중량 (kg)
     * @param netWeight   실중량 (kg)
     * @return 검증 결과
     */
    public ValidationResult validateWeightCalculation(Integer totalWeight, Integer emptyWeight, Integer netWeight) {
        log.info("중량 검증 시작 - 총중량: {}, 공차중량: {}, 실중량: {}", totalWeight, emptyWeight, netWeight);

        // 필수 값 체크
        if (totalWeight == null || emptyWeight == null) {
            log.warn("중량 검증 불가 - 필수 값 누락");
            return ValidationResult.cannotValidate("총중량 또는 공차중량이 누락되었습니다");
        }

        // 계산된 실중량
        int calculatedNetWeight = totalWeight - emptyWeight;

        // 실중량이 제공된 경우 비교
        if (netWeight != null) {
            int difference = Math.abs(calculatedNetWeight - netWeight);
            boolean isValid = difference <= TOLERANCE;

            if (isValid) {
                log.info("중량 검증 성공 - 계산: {}, 실제: {}, 차이: {}kg",
                        calculatedNetWeight, netWeight, difference);
                return ValidationResult.valid(calculatedNetWeight,
                        String.format("검증 성공 (차이: %dkg)", difference));
            } else {
                log.warn("중량 검증 실패 - 계산: {}, 실제: {}, 차이: {}kg (허용: {}kg)",
                        calculatedNetWeight, netWeight, difference, TOLERANCE);
                return ValidationResult.invalid(calculatedNetWeight,
                        String.format("계산된 실중량(%dkg)과 입력된 실중량(%dkg)이 %dkg 차이납니다 (허용: %dkg)",
                                calculatedNetWeight, netWeight, difference, TOLERANCE));
            }
        }

        // 실중량이 없으면 계산값 제공
        log.info("실중량 없음 - 계산 결과: {}kg", calculatedNetWeight);
        return ValidationResult.calculated(calculatedNetWeight, "실중량이 없어 계산값을 제공합니다");
    }

    /**
     * 개별 중량 값 유효성 검증
     */
    public boolean isValidWeight(Integer weight) {
        if (weight == null) {
            return false;
        }
        // 음수이거나 비현실적으로 큰 값 체크
        return weight >= 0 && weight <= 1_000_000;
    }

    /**
     * 중량 범위 검증
     * 공차중량 < 총중량 확인
     */
    public ValidationResult validateWeightRange(Integer totalWeight, Integer emptyWeight) {
        if (totalWeight == null || emptyWeight == null) {
            return ValidationResult.cannotValidate("중량 값이 누락되었습니다");
        }

        // 음수 체크 먼저
        if (emptyWeight < 0 || totalWeight < 0) {
            return ValidationResult.invalid(null, "중량은 음수일 수 없습니다");
        }

        if (emptyWeight > totalWeight) {
            log.warn("비정상 중량 - 공차중량({})이 총중량({})보다 큽니다", emptyWeight, totalWeight);
            return ValidationResult.invalid(null,
                    String.format("공차중량(%dkg)이 총중량(%dkg)보다 클 수 없습니다", emptyWeight, totalWeight));
        }

        return ValidationResult.valid(null, "중량 범위 정상");
    }

    /**
     * 검증 결과 레코드
     */
    public record ValidationResult(
            ValidationStatus status,
            Integer calculatedNetWeight,
            String message) {
        public static ValidationResult valid(Integer calculatedNetWeight, String message) {
            return new ValidationResult(ValidationStatus.VALID, calculatedNetWeight, message);
        }

        public static ValidationResult invalid(Integer calculatedNetWeight, String message) {
            return new ValidationResult(ValidationStatus.INVALID, calculatedNetWeight, message);
        }

        public static ValidationResult calculated(Integer calculatedNetWeight, String message) {
            return new ValidationResult(ValidationStatus.CALCULATED, calculatedNetWeight, message);
        }

        public static ValidationResult cannotValidate(String message) {
            return new ValidationResult(ValidationStatus.CANNOT_VALIDATE, null, message);
        }

        public boolean isValid() {
            return status == ValidationStatus.VALID || status == ValidationStatus.CALCULATED;
        }
    }

    /**
     * 검증 상태 열거형
     */
    public enum ValidationStatus {
        VALID, // 검증 성공
        INVALID, // 검증 실패
        CALCULATED, // 실중량 없이 계산만 수행
        CANNOT_VALIDATE // 검증 불가 (데이터 부족)
    }
}
