package RECO.Internship.Assignment.infrastructure.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OcrFileReader 테스트
 */
class OcrFileReaderTest {

    private OcrFileReader ocrFileReader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ocrFileReader = new OcrFileReader(new ObjectMapper());
    }

    @Test
    @DisplayName("OCR JSON 파일에서 텍스트를 추출할 수 있다")
    void extractTextFromOcrJson() throws IOException {
        // given
        String jsonContent = """
                {
                    "text": "계량증명서 계량일자: 2026-02-02 차량번호: 8713",
                    "confidence": 0.92
                }
                """;
        Path testFile = tempDir.resolve("test.json");
        Files.writeString(testFile, jsonContent);

        // when
        String result = ocrFileReader.extractText(testFile);

        // then
        assertThat(result).contains("계량증명서");
        assertThat(result).contains("2026-02-02");
        assertThat(result).contains("8713");
    }

    @Test
    @DisplayName("pages 배열 구조에서도 텍스트를 추출할 수 있다")
    void extractTextFromPagesStructure() throws IOException {
        // given
        String jsonContent = """
                {
                    "pages": [
                        {
                            "text": "계량확인서 총중량: 12,480 kg 실중량: 5,010 kg"
                        }
                    ],
                    "confidence": 0.95
                }
                """;
        Path testFile = tempDir.resolve("test_pages.json");
        Files.writeString(testFile, jsonContent);

        // when
        String result = ocrFileReader.extractText(testFile);

        // then
        assertThat(result).contains("계량확인서");
        assertThat(result).contains("12,480");
        assertThat(result).contains("5,010");
    }

    @Test
    @DisplayName("신뢰도(confidence)를 추출할 수 있다")
    void extractConfidence() throws IOException {
        // given
        String jsonContent = """
                {
                    "text": "테스트",
                    "confidence": 0.9242
                }
                """;
        Path testFile = tempDir.resolve("test_confidence.json");
        Files.writeString(testFile, jsonContent);

        // when
        double confidence = ocrFileReader.extractConfidence(testFile);

        // then
        assertThat(confidence).isEqualTo(0.9242);
    }

    @Test
    @DisplayName("실제 샘플 데이터 형식으로 텍스트를 추출할 수 있다")
    void extractTextFromRealSampleFormat() throws IOException {
        // given - 실제 sample_01.json과 유사한 구조
        String jsonContent = """
                {
                    "apiVersion": "1.1",
                    "confidence": 0.9242,
                    "text": "계 량 증 명 서 \\n계량일자: 2026-02-02 0016 \\n차량번호: 8713 \\n거 래 처: 곰욕환경폐기물 \\n실 중 량: 5,010 kg"
                }
                """;
        Path testFile = tempDir.resolve("sample_format.json");
        Files.writeString(testFile, jsonContent);

        // when
        String result = ocrFileReader.extractText(testFile);

        // then
        assertThat(result).contains("계 량 증 명 서");
        assertThat(result).contains("5,010 kg");
    }

    @Test
    @DisplayName("텍스트 필드가 없으면 빈 문자열을 반환한다")
    void returnEmptyStringWhenNoTextField() throws IOException {
        // given
        String jsonContent = """
                {
                    "confidence": 0.9
                }
                """;
        Path testFile = tempDir.resolve("no_text.json");
        Files.writeString(testFile, jsonContent);

        // when
        String result = ocrFileReader.extractText(testFile);

        // then
        assertThat(result).isEmpty();
    }
}
