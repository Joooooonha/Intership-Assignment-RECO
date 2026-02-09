package RECO.Internship.Assignment.domain.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * VehicleValidator 테스트
 */
class VehicleValidatorTest {

    private VehicleValidator validator;

    @BeforeEach
    void setUp() {
        validator = new VehicleValidator();
    }

    @Nested
    @DisplayName("차량번호 검증")
    class VehicleNumberValidation {

        @Test
        @DisplayName("신형식 차량번호 (2자리) 검증 성공")
        void validateVehicleNumber_newFormat2Digit() {
            var result = validator.validateVehicleNumber("80구8713");
            assertThat(result.isValid()).isTrue();
            assertThat(result.status()).isEqualTo(VehicleValidator.ValidationStatus.VALID);
        }

        @Test
        @DisplayName("신형식 차량번호 (3자리) 검증 성공")
        void validateVehicleNumber_newFormat3Digit() {
            var result = validator.validateVehicleNumber("123가4567");
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("구형식 차량번호 검증 성공")
        void validateVehicleNumber_oldFormat() {
            var result = validator.validateVehicleNumber("서울12가3456");
            assertThat(result.isValid()).isTrue();
            assertThat(result.message()).contains("구형식");
        }

        @Test
        @DisplayName("부분 인식된 번호는 경고")
        void validateVehicleNumber_partial() {
            var result = validator.validateVehicleNumber("8713"); // 숫자만
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("한글+숫자 조합은 경고로 허용")
        void validateVehicleNumber_withKoreanAndDigit() {
            var result = validator.validateVehicleNumber("가8713");
            assertThat(result.isValid()).isTrue();
            assertThat(result.status()).isEqualTo(VehicleValidator.ValidationStatus.WARNING);
        }

        @Test
        @DisplayName("완전히 잘못된 형식은 실패")
        void validateVehicleNumber_invalid() {
            var result = validator.validateVehicleNumber("ABCD1234");
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("null은 검증 불가")
        void validateVehicleNumber_null() {
            var result = validator.validateVehicleNumber(null);
            assertThat(result.status()).isEqualTo(VehicleValidator.ValidationStatus.CANNOT_VALIDATE);
        }

        @Test
        @DisplayName("공백은 검증 불가")
        void validateVehicleNumber_blank() {
            var result = validator.validateVehicleNumber("   ");
            assertThat(result.status()).isEqualTo(VehicleValidator.ValidationStatus.CANNOT_VALIDATE);
        }
    }
}
