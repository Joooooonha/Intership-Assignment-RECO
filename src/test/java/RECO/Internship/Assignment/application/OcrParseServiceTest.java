package RECO.Internship.Assignment.application;

import RECO.Internship.Assignment.api.dto.ParsedResultResponse;
import RECO.Internship.Assignment.domain.parser.FieldExtractor;
import RECO.Internship.Assignment.domain.validator.DateTimeValidator;
import RECO.Internship.Assignment.domain.validator.GpsValidator;
import RECO.Internship.Assignment.domain.validator.VehicleValidator;
import RECO.Internship.Assignment.domain.validator.WeightValidator;
import RECO.Internship.Assignment.infrastructure.file.OcrFileReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("OcrParseService 테스트")
class OcrParseServiceTest {

    @Mock
    private OcrFileReader ocrFileReader;
    @Mock
    private FieldExtractor fieldExtractor;
    @Mock
    private WeightValidator weightValidator;
    @Mock
    private DateTimeValidator dateTimeValidator;
    @Mock
    private GpsValidator gpsValidator;
    @Mock
    private VehicleValidator vehicleValidator;

    @InjectMocks
    private OcrParseService ocrParseService;

    private static final String SAMPLE_OCR_TEXT = """
            계량증명서
            계량일자: 2026-02-02
            시간: 05:37:55
            차량번호: 80구8713
            총중량: 12,480 kg
            공차중량: 7,470 kg
            실중량: 5,010 kg
            """;

    /**
     * 공통 mock 설정 헬퍼
     */
    private void setupCommonMocks() {
        // DateTime validator mock
        given(dateTimeValidator.validateDate(anyString()))
                .willReturn(new DateTimeValidator.ValidationResult(
                        DateTimeValidator.ValidationStatus.VALID, "날짜 형식 유효"));
        given(dateTimeValidator.validateTime(anyString()))
                .willReturn(new DateTimeValidator.ValidationResult(
                        DateTimeValidator.ValidationStatus.VALID, "시간 형식 유효"));

        // GPS validator mock
        given(gpsValidator.validateCoordinates(any(double[].class)))
                .willReturn(new GpsValidator.ValidationResult(
                        GpsValidator.ValidationStatus.VALID, "GPS 좌표 유효"));

        // Vehicle validator mock
        given(vehicleValidator.validateVehicleNumber(anyString()))
                .willReturn(new VehicleValidator.ValidationResult(
                        VehicleValidator.ValidationStatus.VALID, "차량번호 유효"));
    }

    @Nested
    @DisplayName("parseFromFile")
    class ParseFromFile {

        @Test
        @DisplayName("파일에서 OCR 텍스트를 파싱하여 결과를 반환한다")
        void parseFromFile_success() throws IOException {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "sample.json",
                    MediaType.APPLICATION_JSON_VALUE,
                    "{\"text\": \"test\"}".getBytes());

            given(ocrFileReader.readFromMultipartFile(any())).willReturn(SAMPLE_OCR_TEXT);
            given(ocrFileReader.extractConfidence(any(org.springframework.web.multipart.MultipartFile.class)))
                    .willReturn(0.95);

            // Mock field extractor
            given(fieldExtractor.extractDocumentType(anyString())).willReturn("계량증명서");
            given(fieldExtractor.extractDate(anyString())).willReturn("2026-02-02");
            given(fieldExtractor.extractTime(anyString())).willReturn("05:37:55");
            given(fieldExtractor.extractVehicleNumber(anyString())).willReturn("80구8713");
            given(fieldExtractor.extractTotalWeight(anyString())).willReturn(12480);
            given(fieldExtractor.extractEmptyWeight(anyString())).willReturn(7470);
            given(fieldExtractor.extractNetWeight(anyString())).willReturn(5010);
            given(fieldExtractor.extractCustomer(anyString())).willReturn("테스트업체");
            given(fieldExtractor.extractProductName(anyString())).willReturn(null);
            given(fieldExtractor.extractIssuer(anyString())).willReturn("동우바이오(주)");
            given(fieldExtractor.extractGpsCoordinates(anyString()))
                    .willReturn(new double[] { 37.105317, 127.375673 });

            // Mock weight validator
            given(weightValidator.validateWeightCalculation(12480, 7470, 5010))
                    .willReturn(WeightValidator.ValidationResult.valid(5010, "검증 성공"));

            // Mock other validators
            setupCommonMocks();

            // when
            ParsedResultResponse result = ocrParseService.parseFromFile(file);

            // then
            assertThat(result.getDocumentType()).isEqualTo("계량증명서");
            assertThat(result.getDate()).isEqualTo("2026-02-02");
            assertThat(result.getTime()).isEqualTo("05:37:55");
            assertThat(result.getVehicleNumber()).isEqualTo("80구8713");
            assertThat(result.getTotalWeight()).isEqualTo(12480);
            assertThat(result.getEmptyWeight()).isEqualTo(7470);
            assertThat(result.getNetWeight()).isEqualTo(5010);
            assertThat(result.getConfidence()).isEqualTo(0.95);
            assertThat(result.getGps().getLatitude()).isEqualTo(37.105317);
            assertThat(result.getGps().getLongitude()).isEqualTo(127.375673);

            // 새로운 ValidationInfo 구조 검증
            assertThat(result.getValidation().getOverallStatus()).isEqualTo("VALID");
            assertThat(result.getValidation().getOverallMessage()).isEqualTo("모든 검증 통과");
            assertThat(result.getValidation().getWeight().getStatus()).isEqualTo("VALID");
            assertThat(result.getValidation().getDateTime().getStatus()).isEqualTo("VALID");
            assertThat(result.getValidation().getGps().getStatus()).isEqualTo("VALID");
            assertThat(result.getValidation().getVehicle().getStatus()).isEqualTo("VALID");
        }
    }

    @Nested
    @DisplayName("parseFromJson")
    class ParseFromJson {

        @Test
        @DisplayName("JSON 문자열에서 OCR 텍스트를 파싱하여 결과를 반환한다")
        void parseFromJson_success() {
            // given
            String jsonContent = "{\"text\": \"" + SAMPLE_OCR_TEXT + "\"}";

            given(ocrFileReader.readFromString(anyString())).willReturn(SAMPLE_OCR_TEXT);
            given(ocrFileReader.extractConfidenceFromString(anyString())).willReturn(0.88);

            // Mock field extractor
            given(fieldExtractor.extractDocumentType(anyString())).willReturn("계량증명서");
            given(fieldExtractor.extractDate(anyString())).willReturn("2026-02-02");
            given(fieldExtractor.extractTime(anyString())).willReturn("05:37:55");
            given(fieldExtractor.extractVehicleNumber(anyString())).willReturn("80구8713");
            given(fieldExtractor.extractTotalWeight(anyString())).willReturn(12480);
            given(fieldExtractor.extractEmptyWeight(anyString())).willReturn(7470);
            given(fieldExtractor.extractNetWeight(anyString())).willReturn(5010);
            given(fieldExtractor.extractCustomer(anyString())).willReturn(null);
            given(fieldExtractor.extractProductName(anyString())).willReturn(null);
            given(fieldExtractor.extractIssuer(anyString())).willReturn(null);
            given(fieldExtractor.extractGpsCoordinates(anyString())).willReturn(null);

            // Mock weight validator
            given(weightValidator.validateWeightCalculation(12480, 7470, 5010))
                    .willReturn(WeightValidator.ValidationResult.valid(5010, "검증 성공"));

            // Mock other validators - GPS returns CANNOT_VALIDATE when null
            given(dateTimeValidator.validateDate(anyString()))
                    .willReturn(new DateTimeValidator.ValidationResult(
                            DateTimeValidator.ValidationStatus.VALID, "날짜 형식 유효"));
            given(dateTimeValidator.validateTime(anyString()))
                    .willReturn(new DateTimeValidator.ValidationResult(
                            DateTimeValidator.ValidationStatus.VALID, "시간 형식 유효"));
            given(gpsValidator.validateCoordinates((double[]) null))
                    .willReturn(new GpsValidator.ValidationResult(
                            GpsValidator.ValidationStatus.CANNOT_VALIDATE, "GPS 좌표가 없습니다"));
            given(vehicleValidator.validateVehicleNumber(anyString()))
                    .willReturn(new VehicleValidator.ValidationResult(
                            VehicleValidator.ValidationStatus.VALID, "차량번호 유효"));

            // when
            ParsedResultResponse result = ocrParseService.parseFromJson(jsonContent);

            // then
            assertThat(result.getDocumentType()).isEqualTo("계량증명서");
            assertThat(result.getConfidence()).isEqualTo(0.88);
            assertThat(result.getGps()).isNull();

            // GPS가 없으면 CANNOT_VALIDATE 상태
            assertThat(result.getValidation().getOverallStatus()).isEqualTo("CANNOT_VALIDATE");
            assertThat(result.getValidation().getGps().getStatus()).isEqualTo("CANNOT_VALIDATE");
        }
    }

    @Nested
    @DisplayName("중량 검증 실패 케이스")
    class ValidationFailure {

        @Test
        @DisplayName("중량 불일치 시 INVALID 상태를 반환한다")
        void parseFromJson_weightMismatch_returnsInvalid() {
            // given
            String jsonContent = "{\"text\": \"test\"}";

            given(ocrFileReader.readFromString(anyString())).willReturn(SAMPLE_OCR_TEXT);
            given(ocrFileReader.extractConfidenceFromString(anyString())).willReturn(null);

            given(fieldExtractor.extractDocumentType(anyString())).willReturn("계량증명서");
            given(fieldExtractor.extractDate(anyString())).willReturn("2026-02-02");
            given(fieldExtractor.extractTime(anyString())).willReturn(null);
            given(fieldExtractor.extractVehicleNumber(anyString())).willReturn(null);
            given(fieldExtractor.extractTotalWeight(anyString())).willReturn(12480);
            given(fieldExtractor.extractEmptyWeight(anyString())).willReturn(7470);
            given(fieldExtractor.extractNetWeight(anyString())).willReturn(6000); // 불일치!
            given(fieldExtractor.extractCustomer(anyString())).willReturn(null);
            given(fieldExtractor.extractProductName(anyString())).willReturn(null);
            given(fieldExtractor.extractIssuer(anyString())).willReturn(null);
            given(fieldExtractor.extractGpsCoordinates(anyString())).willReturn(null);

            // Mock weight validator - 불일치
            given(weightValidator.validateWeightCalculation(12480, 7470, 6000))
                    .willReturn(WeightValidator.ValidationResult.invalid(5010,
                            "계산된 실중량(5010kg)과 입력된 실중량(6000kg)이 990kg 차이납니다 (허용: 100kg)"));

            // Mock other validators
            given(dateTimeValidator.validateDate(anyString()))
                    .willReturn(new DateTimeValidator.ValidationResult(
                            DateTimeValidator.ValidationStatus.VALID, "날짜 형식 유효"));
            given(dateTimeValidator.validateTime((String) null))
                    .willReturn(new DateTimeValidator.ValidationResult(
                            DateTimeValidator.ValidationStatus.CANNOT_VALIDATE, "시간이 없습니다"));
            given(gpsValidator.validateCoordinates((double[]) null))
                    .willReturn(new GpsValidator.ValidationResult(
                            GpsValidator.ValidationStatus.CANNOT_VALIDATE, "GPS 좌표가 없습니다"));
            given(vehicleValidator.validateVehicleNumber((String) null))
                    .willReturn(new VehicleValidator.ValidationResult(
                            VehicleValidator.ValidationStatus.CANNOT_VALIDATE, "차량번호가 없습니다"));

            // when
            ParsedResultResponse result = ocrParseService.parseFromJson(jsonContent);

            // then
            assertThat(result.getValidation().getOverallStatus()).isEqualTo("INVALID");
            assertThat(result.getValidation().getWeight().getStatus()).isEqualTo("INVALID");
            assertThat(result.getValidation().getWeight().getMessage()).contains("차이");
            assertThat(result.getValidation().getWeight().getValue()).isEqualTo(5010);
        }
    }
}
