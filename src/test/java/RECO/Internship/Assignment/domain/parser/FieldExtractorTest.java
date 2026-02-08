package RECO.Internship.Assignment.domain.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FieldExtractor 테스트
 */
class FieldExtractorTest {

    private FieldExtractor fieldExtractor;

    @BeforeEach
    void setUp() {
        fieldExtractor = new FieldExtractor();
    }

    @Nested
    @DisplayName("문서 종류 추출")
    class DocumentTypeExtraction {

        @Test
        @DisplayName("계량증명서를 추출할 수 있다")
        void extractDocumentType_계량증명서() {
            String text = "계 량 증 명 서 \n계량일자: 2026-02-02";
            String result = fieldExtractor.extractDocumentType(text);
            assertThat(result).isEqualTo("계량증명서");
        }

        @Test
        @DisplayName("계근표를 추출할 수 있다")
        void extractDocumentType_계근표() {
            String text = "계 근 표 \n날짜: 2026-02-02";
            String result = fieldExtractor.extractDocumentType(text);
            assertThat(result).isEqualTo("계근표");
        }

        @Test
        @DisplayName("계량확인서를 추출할 수 있다")
        void extractDocumentType_계량확인서() {
            String text = "계량확인서 발행";
            String result = fieldExtractor.extractDocumentType(text);
            assertThat(result).isEqualTo("계량확인서");
        }
    }

    @Nested
    @DisplayName("날짜 추출")
    class DateExtraction {

        @Test
        @DisplayName("yyyy-MM-dd 형식 날짜를 추출할 수 있다")
        void extractDate_standard() {
            String text = "계량일자: 2026-02-02 시간: 10:30";
            String result = fieldExtractor.extractDate(text);
            assertThat(result).isEqualTo("2026-02-02");
        }

        @Test
        @DisplayName("yyyy.MM.dd 형식 날짜를 추출할 수 있다")
        void extractDate_dot() {
            String text = "날짜: 2026.02.02";
            String result = fieldExtractor.extractDate(text);
            assertThat(result).isEqualTo("2026-02-02");
        }

        @Test
        @DisplayName("yyyy/MM/dd 형식 날짜를 추출할 수 있다")
        void extractDate_slash() {
            String text = "Date: 2026/2/5";
            String result = fieldExtractor.extractDate(text);
            assertThat(result).isEqualTo("2026-02-05");
        }
    }

    @Nested
    @DisplayName("차량번호 추출")
    class VehicleNumberExtraction {

        @Test
        @DisplayName("차량번호 라벨로 추출할 수 있다")
        void extractVehicleNumber_label() {
            String text = "차량번호: 80구8713";
            String result = fieldExtractor.extractVehicleNumber(text);
            assertThat(result).isEqualTo("80구8713");
        }

        @Test
        @DisplayName("차 번 호 띄어쓰기가 있어도 추출할 수 있다")
        void extractVehicleNumber_spaced() {
            String text = "차 번 호: 12가3456";
            String result = fieldExtractor.extractVehicleNumber(text);
            assertThat(result).isEqualTo("12가3456");
        }

        @Test
        @DisplayName("차량 No. 형식으로도 추출할 수 있다")
        void extractVehicleNumber_no() {
            String text = "차량 No. 34나5678";
            String result = fieldExtractor.extractVehicleNumber(text);
            assertThat(result).isEqualTo("34나5678");
        }
    }

    @Nested
    @DisplayName("중량 추출")
    class WeightExtraction {

        @Test
        @DisplayName("총중량을 추출할 수 있다")
        void extractTotalWeight() {
            String text = "총 중 량: 05:37:55 12,480 kg";
            Integer result = fieldExtractor.extractTotalWeight(text);
            assertThat(result).isEqualTo(12480);
        }

        @Test
        @DisplayName("공차중량을 추출할 수 있다")
        void extractEmptyWeight() {
            String text = "공차중량: 04:26:18 7,470 kg";
            Integer result = fieldExtractor.extractEmptyWeight(text);
            assertThat(result).isEqualTo(7470);
        }

        @Test
        @DisplayName("차중량 라벨로도 공차중량을 추출할 수 있다")
        void extractEmptyWeight_alternative() {
            String text = "차 중 량: 8,500 kg";
            Integer result = fieldExtractor.extractEmptyWeight(text);
            assertThat(result).isEqualTo(8500);
        }

        @Test
        @DisplayName("실중량을 추출할 수 있다")
        void extractNetWeight() {
            String text = "실 중 량: 5,010 kg";
            Integer result = fieldExtractor.extractNetWeight(text);
            assertThat(result).isEqualTo(5010);
        }

        @Test
        @DisplayName("쉼표 없는 중량도 추출할 수 있다")
        void extractWeight_noComma() {
            String text = "총중량: 15000 kg";
            Integer result = fieldExtractor.extractTotalWeight(text);
            assertThat(result).isEqualTo(15000);
        }
    }

    @Nested
    @DisplayName("거래처 추출")
    class CustomerExtraction {

        @Test
        @DisplayName("거래처를 추출할 수 있다")
        void extractCustomer() {
            String text = "거 래 처: 곰욕환경폐기물 \n품명: 폐기물";
            String result = fieldExtractor.extractCustomer(text);
            assertThat(result).isEqualTo("곰욕환경폐기물");
        }

        @Test
        @DisplayName("상호로도 추출할 수 있다")
        void extractCustomer_상호() {
            String text = "상호: ABC회사 총중량:";
            String result = fieldExtractor.extractCustomer(text);
            assertThat(result).isEqualTo("ABC회사");
        }
    }

    @Nested
    @DisplayName("발행업체 추출")
    class IssuerExtraction {

        @Test
        @DisplayName("(주) 형식 발행업체를 추출할 수 있다")
        void extractIssuer_주() {
            String text = "동우바이오(주) 경기도 용인시";
            String result = fieldExtractor.extractIssuer(text);
            assertThat(result).isEqualTo("동우바이오(주)");
        }

        @Test
        @DisplayName("주식회사 형식 발행업체를 추출할 수 있다")
        void extractIssuer_주식회사() {
            String text = "발행: 한국환경주식회사";
            String result = fieldExtractor.extractIssuer(text);
            assertThat(result).isEqualTo("한국환경주식회사");
        }
    }

    @Nested
    @DisplayName("GPS 좌표 추출")
    class GpsExtraction {

        @Test
        @DisplayName("GPS 좌표를 추출할 수 있다")
        void extractGps() {
            String text = "위치: 37.105317, 127.375673";
            double[] result = fieldExtractor.extractGpsCoordinates(text);
            assertThat(result).isNotNull();
            assertThat(result[0]).isEqualTo(37.105317);
            assertThat(result[1]).isEqualTo(127.375673);
        }

        @Test
        @DisplayName("GPS가 없으면 null을 반환한다")
        void extractGps_notFound() {
            String text = "주소: 경기도 용인시";
            double[] result = fieldExtractor.extractGpsCoordinates(text);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("실제 샘플 데이터 테스트")
    class RealSampleTest {

        @Test
        @DisplayName("sample_01 형식 데이터에서 필드를 추출할 수 있다")
        void extractFromSample01Format() {
            String text = """
                    계 량 증 명 서
                    계량일자: 2026-02-02 0016
                    차량번호: 8713
                    거 래 처: 곰욕환경폐기물
                    총 중 량: 05:37:55 12,480 kg
                    공차중량: 04:26:18 7,470 kg
                    실 중 량: 5,010 kg
                    동우바이오(주)
                    37.105317, 127.375673
                    """;

            assertThat(fieldExtractor.extractDocumentType(text)).isEqualTo("계량증명서");
            assertThat(fieldExtractor.extractDate(text)).isEqualTo("2026-02-02");
            assertThat(fieldExtractor.extractVehicleNumber(text)).isEqualTo("8713");
            assertThat(fieldExtractor.extractTotalWeight(text)).isEqualTo(12480);
            assertThat(fieldExtractor.extractEmptyWeight(text)).isEqualTo(7470);
            assertThat(fieldExtractor.extractNetWeight(text)).isEqualTo(5010);
            assertThat(fieldExtractor.extractIssuer(text)).isEqualTo("동우바이오(주)");
        }
    }
}
