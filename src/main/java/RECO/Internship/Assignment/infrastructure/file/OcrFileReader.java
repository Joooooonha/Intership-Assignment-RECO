package RECO.Internship.Assignment.infrastructure.file;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * OCR JSON 파일을 읽어서 텍스트를 추출하는 클래스
 */
@Component
public class OcrFileReader {

    private static final Logger log = LoggerFactory.getLogger(OcrFileReader.class);
    private final ObjectMapper objectMapper;

    public OcrFileReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * OCR JSON 파일에서 텍스트 추출
     * 
     * @param filePath JSON 파일 경로
     * @return 추출된 텍스트
     */
    public String extractText(Path filePath) throws IOException {
        log.info("파일 읽기 시작 - {}", filePath.getFileName());

        String jsonContent = Files.readString(filePath);
        JsonNode rootNode = objectMapper.readTree(jsonContent);

        // OCR JSON 구조: root.text 또는 root.pages[0].text
        String text = extractTextFromJson(rootNode);

        log.info("텍스트 추출 완료 - 길이: {} 글자", text.length());
        return text;
    }

    /**
     * JSON 노드에서 텍스트 필드 추출
     */
    private String extractTextFromJson(JsonNode rootNode) {
        // 먼저 root.text 확인 (존재하고 비어있지 않은 경우)
        if (rootNode.has("text")) {
            String text = rootNode.get("text").asText();
            if (text != null && !text.trim().isEmpty()) {
                return text;
            }
        }

        // pages[0].text 확인 (존재하고 비어있지 않은 경우)
        if (rootNode.has("pages") && rootNode.get("pages").isArray()) {
            JsonNode pages = rootNode.get("pages");
            if (pages.size() > 0 && pages.get(0).has("text")) {
                String pageText = pages.get(0).get("text").asText();
                if (pageText != null && !pageText.trim().isEmpty()) {
                    return pageText;
                }
            }
        }

        log.warn("텍스트 필드를 찾을 수 없거나 비어있습니다");
        return "";
    }

    /**
     * OCR JSON 파일의 신뢰도 추출
     */
    public double extractConfidence(Path filePath) throws IOException {
        String jsonContent = Files.readString(filePath);
        JsonNode rootNode = objectMapper.readTree(jsonContent);

        if (rootNode.has("confidence")) {
            return rootNode.get("confidence").asDouble();
        }
        return 0.0;
    }
}
