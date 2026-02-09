package RECO.Internship.Assignment.domain.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DateTimeValidator 테스트
 */
class DateTimeValidatorTest {

    private DateTimeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DateTimeValidator();
    }

    @Nested
    @DisplayName("날짜 검증")
    class DateValidation {

        @Test
        @DisplayName("유효한 날짜는 검증 성공")
        void validateDate_valid() {
            var result = validator.validateDate("2026-02-02");
            assertThat(result.isValid()).isTrue();
            assertThat(result.status()).isEqualTo(DateTimeValidator.ValidationStatus.VALID);
        }

        @Test
        @DisplayName("미래 날짜는 경고")
        void validateDate_future() {
            var result = validator.validateDate("2030-12-31");
            assertThat(result.isValid()).isTrue();
            assertThat(result.status()).isEqualTo(DateTimeValidator.ValidationStatus.WARNING);
            assertThat(result.message()).contains("미래");
        }

        @Test
        @DisplayName("10년 이전 날짜는 경고")
        void validateDate_tooOld() {
            var result = validator.validateDate("2010-01-01");
            assertThat(result.isValid()).isTrue();
            assertThat(result.status()).isEqualTo(DateTimeValidator.ValidationStatus.WARNING);
            assertThat(result.message()).contains("10년");
        }

        @Test
        @DisplayName("잘못된 형식은 실패")
        void validateDate_invalidFormat() {
            var result = validator.validateDate("2026/02/02");
            assertThat(result.isValid()).isFalse();
            assertThat(result.status()).isEqualTo(DateTimeValidator.ValidationStatus.INVALID);
        }

        @Test
        @DisplayName("존재하지 않는 날짜는 실패")
        void validateDate_invalidDate() {
            var result = validator.validateDate("2026-13-01"); // 13월은 없음
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("null은 검증 불가")
        void validateDate_null() {
            var result = validator.validateDate(null);
            assertThat(result.status()).isEqualTo(DateTimeValidator.ValidationStatus.CANNOT_VALIDATE);
        }
    }

    @Nested
    @DisplayName("시간 검증")
    class TimeValidation {

        @Test
        @DisplayName("유효한 시간은 검증 성공")
        void validateTime_valid() {
            var result = validator.validateTime("05:37:55");
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("자정은 유효")
        void validateTime_midnight() {
            var result = validator.validateTime("00:00:00");
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("잘못된 형식은 실패")
        void validateTime_invalidFormat() {
            var result = validator.validateTime("5:37:55");
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("범위 초과 시간은 실패")
        void validateTime_outOfRange() {
            var result = validator.validateTime("25:00:00");
            assertThat(result.isValid()).isFalse();
        }
    }
}
