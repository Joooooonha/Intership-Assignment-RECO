package RECO.Internship.Assignment.domain.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 날짜 및 시간 유효성 검증 클래스
 */
@Component
public class DateTimeValidator {

    private static final Logger log = LoggerFactory.getLogger(DateTimeValidator.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * 날짜 문자열 유효성 검증
     * 
     * @param dateStr yyyy-MM-dd 형식 날짜
     * @return 검증 결과
     */
    public ValidationResult validateDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return ValidationResult.cannotValidate("날짜가 없습니다");
        }

        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);

            // 미래 날짜 체크
            if (date.isAfter(LocalDate.now())) {
                log.warn("미래 날짜 감지: {}", dateStr);
                return ValidationResult.warning("미래 날짜입니다: " + dateStr);
            }

            // 너무 오래된 날짜 체크 (10년 이전)
            if (date.isBefore(LocalDate.now().minusYears(10))) {
                log.warn("오래된 날짜 감지: {}", dateStr);
                return ValidationResult.warning("10년 이전 날짜입니다: " + dateStr);
            }

            log.debug("날짜 검증 성공: {}", dateStr);
            return ValidationResult.valid("날짜 형식 유효: " + dateStr);

        } catch (DateTimeParseException e) {
            log.warn("날짜 파싱 실패: {}", dateStr);
            return ValidationResult.invalid("날짜 형식이 올바르지 않습니다: " + dateStr);
        }
    }

    /**
     * 시간 문자열 유효성 검증
     * 
     * @param timeStr HH:mm:ss 형식 시간
     * @return 검증 결과
     */
    public ValidationResult validateTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) {
            return ValidationResult.cannotValidate("시간이 없습니다");
        }

        try {
            LocalTime.parse(timeStr, TIME_FORMATTER);
            log.debug("시간 검증 성공: {}", timeStr);
            return ValidationResult.valid("시간 형식 유효: " + timeStr);

        } catch (DateTimeParseException e) {
            log.warn("시간 파싱 실패: {}", timeStr);
            return ValidationResult.invalid("시간 형식이 올바르지 않습니다: " + timeStr);
        }
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
