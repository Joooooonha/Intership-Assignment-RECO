package RECO.Internship.Assignment.domain.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GpsValidator 테스트
 */
class GpsValidatorTest {

    private GpsValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GpsValidator();
    }

    @Nested
    @DisplayName("GPS 좌표 검증")
    class CoordinatesValidation {

        @Test
        @DisplayName("한국 내 좌표는 검증 성공")
        void validateCoordinates_inKorea() {
            var result = validator.validateCoordinates(37.105317, 127.375673);
            assertThat(result.isValid()).isTrue();
            assertThat(result.status()).isEqualTo(GpsValidator.ValidationStatus.VALID);
        }

        @Test
        @DisplayName("서울 좌표는 검증 성공")
        void validateCoordinates_seoul() {
            var result = validator.validateCoordinates(37.5665, 126.9780);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("한국 범위 외 좌표는 경고")
        void validateCoordinates_outsideKorea() {
            var result = validator.validateCoordinates(35.6762, 139.6503); // 도쿄
            assertThat(result.isValid()).isTrue();
            assertThat(result.status()).isEqualTo(GpsValidator.ValidationStatus.WARNING);
            assertThat(result.message()).contains("한국 범위 외");
        }

        @Test
        @DisplayName("잘못된 위도는 실패")
        void validateCoordinates_invalidLatitude() {
            var result = validator.validateCoordinates(95.0, 127.0);
            assertThat(result.isValid()).isFalse();
            assertThat(result.message()).contains("위도");
        }

        @Test
        @DisplayName("잘못된 경도는 실패")
        void validateCoordinates_invalidLongitude() {
            var result = validator.validateCoordinates(37.0, 200.0);
            assertThat(result.isValid()).isFalse();
            assertThat(result.message()).contains("경도");
        }

        @Test
        @DisplayName("배열 형태 좌표 검증")
        void validateCoordinates_array() {
            double[] coords = { 37.105317, 127.375673 };
            var result = validator.validateCoordinates(coords);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("null 좌표는 검증 불가")
        void validateCoordinates_null() {
            var result = validator.validateCoordinates(null, null);
            assertThat(result.status()).isEqualTo(GpsValidator.ValidationStatus.CANNOT_VALIDATE);
        }
    }

    @Nested
    @DisplayName("한국 범위 체크")
    class KoreaRangeCheck {

        @Test
        @DisplayName("한국 범위 내 좌표 확인")
        void isInKoreaRange_true() {
            assertThat(validator.isInKoreaRange(37.5665, 126.9780)).isTrue(); // 서울
            assertThat(validator.isInKoreaRange(35.1796, 129.0756)).isTrue(); // 부산
            assertThat(validator.isInKoreaRange(33.4996, 126.5312)).isTrue(); // 제주
        }

        @Test
        @DisplayName("한국 범위 외 좌표 확인")
        void isInKoreaRange_false() {
            assertThat(validator.isInKoreaRange(35.6762, 139.6503)).isFalse(); // 도쿄
            assertThat(validator.isInKoreaRange(39.9042, 116.4074)).isFalse(); // 베이징
        }
    }
}
