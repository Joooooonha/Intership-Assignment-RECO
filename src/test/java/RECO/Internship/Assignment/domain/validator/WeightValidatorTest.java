package RECO.Internship.Assignment.domain.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WeightValidator 테스트
 */
class WeightValidatorTest {

    private WeightValidator weightValidator;

    @BeforeEach
    void setUp() {
        weightValidator = new WeightValidator();
    }

    @Nested
    @DisplayName("중량 계산 검증")
    class WeightCalculationValidation {

        @Test
        @DisplayName("총중량 - 공차중량 = 실중량이 일치하면 검증 성공")
        void validateWeightCalculation_success() {
            // given
            Integer totalWeight = 12480;
            Integer emptyWeight = 7470;
            Integer netWeight = 5010;

            // when
            var result = weightValidator.validateWeightCalculation(totalWeight, emptyWeight, netWeight);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.status()).isEqualTo(WeightValidator.ValidationStatus.VALID);
            assertThat(result.calculatedNetWeight()).isEqualTo(5010);
        }

        @Test
        @DisplayName("허용 오차(10kg) 이내면 검증 성공")
        void validateWeightCalculation_withinTolerance() {
            // given
            Integer totalWeight = 12480;
            Integer emptyWeight = 7470;
            Integer netWeight = 5015; // 5kg 차이

            // when
            var result = weightValidator.validateWeightCalculation(totalWeight, emptyWeight, netWeight);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.status()).isEqualTo(WeightValidator.ValidationStatus.VALID);
        }

        @Test
        @DisplayName("허용 오차를 초과하면 검증 실패")
        void validateWeightCalculation_exceedsTolerance() {
            // given
            Integer totalWeight = 12480;
            Integer emptyWeight = 7470;
            Integer netWeight = 5100; // 90kg 차이

            // when
            var result = weightValidator.validateWeightCalculation(totalWeight, emptyWeight, netWeight);

            // then
            assertThat(result.isValid()).isFalse();
            assertThat(result.status()).isEqualTo(WeightValidator.ValidationStatus.INVALID);
            assertThat(result.message()).contains("차이납니다");
        }

        @Test
        @DisplayName("실중량이 없으면 계산값 제공")
        void validateWeightCalculation_noNetWeight() {
            // given
            Integer totalWeight = 12480;
            Integer emptyWeight = 7470;
            Integer netWeight = null;

            // when
            var result = weightValidator.validateWeightCalculation(totalWeight, emptyWeight, netWeight);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.status()).isEqualTo(WeightValidator.ValidationStatus.CALCULATED);
            assertThat(result.calculatedNetWeight()).isEqualTo(5010);
        }

        @Test
        @DisplayName("총중량이 없으면 검증 불가")
        void validateWeightCalculation_noTotalWeight() {
            // given
            Integer totalWeight = null;
            Integer emptyWeight = 7470;
            Integer netWeight = 5010;

            // when
            var result = weightValidator.validateWeightCalculation(totalWeight, emptyWeight, netWeight);

            // then
            assertThat(result.isValid()).isFalse();
            assertThat(result.status()).isEqualTo(WeightValidator.ValidationStatus.CANNOT_VALIDATE);
        }
    }

    @Nested
    @DisplayName("중량 범위 검증")
    class WeightRangeValidation {

        @Test
        @DisplayName("공차중량이 총중량보다 작으면 정상")
        void validateWeightRange_valid() {
            // when
            var result = weightValidator.validateWeightRange(12480, 7470);

            // then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("공차중량이 총중량보다 크면 실패")
        void validateWeightRange_invalidRange() {
            // when
            var result = weightValidator.validateWeightRange(5000, 7000);

            // then
            assertThat(result.isValid()).isFalse();
            assertThat(result.message()).contains("클 수 없습니다");
        }

        @Test
        @DisplayName("음수 중량은 실패")
        void validateWeightRange_negativeWeight() {
            // when
            var result = weightValidator.validateWeightRange(-100, 50);

            // then
            assertThat(result.isValid()).isFalse();
            assertThat(result.message()).contains("음수");
        }
    }

    @Nested
    @DisplayName("개별 중량 값 검증")
    class IndividualWeightValidation {

        @Test
        @DisplayName("정상 범위 중량은 유효")
        void isValidWeight_valid() {
            assertThat(weightValidator.isValidWeight(12480)).isTrue();
            assertThat(weightValidator.isValidWeight(0)).isTrue();
            assertThat(weightValidator.isValidWeight(100000)).isTrue();
        }

        @Test
        @DisplayName("null 중량은 무효")
        void isValidWeight_null() {
            assertThat(weightValidator.isValidWeight(null)).isFalse();
        }

        @Test
        @DisplayName("음수 중량은 무효")
        void isValidWeight_negative() {
            assertThat(weightValidator.isValidWeight(-100)).isFalse();
        }

        @Test
        @DisplayName("비현실적으로 큰 중량은 무효")
        void isValidWeight_tooLarge() {
            assertThat(weightValidator.isValidWeight(2_000_000)).isFalse();
        }
    }

    @Nested
    @DisplayName("실제 샘플 데이터 테스트")
    class RealSampleTest {

        @Test
        @DisplayName("sample_01 데이터 검증 성공")
        void validateSample01() {
            // sample_01: 총중량 12,480kg, 공차중량 7,470kg, 실중량 5,010kg
            var result = weightValidator.validateWeightCalculation(12480, 7470, 5010);

            assertThat(result.isValid()).isTrue();
            assertThat(result.calculatedNetWeight()).isEqualTo(5010);
        }
    }
}
