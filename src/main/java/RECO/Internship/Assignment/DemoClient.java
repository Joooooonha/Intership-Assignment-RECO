package RECO.Internship.Assignment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.stream.Stream;

/**
 * OCR 파싱 API 데모 클라이언트
 * 
 * 사용 방법:
 * 1. 먼저 서버를 실행합니다: ./gradlew bootRun
 * 2. 새 터미널에서 이 클래스를 실행합니다: ./gradlew runDemo
 * 
 * 이 클라이언트는 샘플 OCR JSON 파일들을 API로 전송하고
 * 파싱 결과를 콘솔에 출력합니다.
 */
public class DemoClient {

    private static final String API_BASE_URL = "http://localhost:8080/api/ocr";
    private static final String SAMPLE_DATA_DIR = "[2026 ICT_리코] smaple_data_ocr";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DemoClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static void main(String[] args) {
        printBanner();

        DemoClient client = new DemoClient();

        // 서버 상태 확인
        if (!client.checkServerHealth()) {
            System.err.println("[ERROR] 서버에 연결할 수 없습니다.");
            System.err.println("[INFO] 먼저 서버를 실행하세요: ./gradlew bootRun");
            System.exit(1);
        }

        System.out.println("[INFO] 서버 연결 성공!\n");

        // 샘플 파일 파싱
        client.parseAllSampleFiles();

        printFooter();
    }

    /**
     * 서버 헬스 체크
     */
    private boolean checkServerHealth() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/health"))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 모든 샘플 파일 파싱
     */
    private void parseAllSampleFiles() {
        Path sampleDir = Path.of(SAMPLE_DATA_DIR);

        if (!Files.exists(sampleDir)) {
            System.err.println("[ERROR] 샘플 데이터 디렉토리를 찾을 수 없습니다: " + sampleDir);
            return;
        }

        try (Stream<Path> files = Files.list(sampleDir)) {
            files.filter(p -> p.toString().endsWith(".json"))
                    .sorted()
                    .forEach(this::parseSingleFile);
        } catch (IOException e) {
            System.err.println("[ERROR] 파일 목록 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 단일 파일 파싱
     */
    private void parseSingleFile(Path filePath) {
        System.out.println("============================================================");
        System.out.println("[FILE] " + filePath.getFileName());
        System.out.println("============================================================");

        try {
            // JSON 파일 읽기
            String jsonContent = Files.readString(filePath);

            // API 호출
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/parse/json"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonContent))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                printParsedResult(response.body());
            } else {
                System.err.println("[ERROR] API 오류 (HTTP " + response.statusCode() + ")");
                System.err.println(response.body());
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("[ERROR] 파싱 실패: " + e.getMessage());
        }

        System.out.println();
    }

    /**
     * 파싱 결과를 보기 좋게 출력
     */
    private void printParsedResult(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            // 기본 정보
            System.out.println("\n[Parse Result]");
            System.out.println("----------------------------------------");
            printField("문서종류", root.path("documentType"));
            printField("날짜", root.path("date"));
            printField("시간", root.path("time"));
            printField("차량번호", root.path("vehicleNumber"));

            // 중량 정보
            System.out.println("\n[Weight Info]");
            System.out.println("----------------------------------------");
            printField("총중량", root.path("totalWeight"), "kg");
            printField("공차중량", root.path("emptyWeight"), "kg");
            printField("실중량", root.path("netWeight"), "kg");

            // 기타 정보
            if (!root.path("customer").isMissingNode() && !root.path("customer").isNull()) {
                System.out.println("\n[Other Info]");
                System.out.println("----------------------------------------");
                printField("고객사", root.path("customer"));
                printField("품목", root.path("productName"));
                printField("발행처", root.path("issuer"));
            }

            // GPS 정보
            JsonNode gps = root.path("gps");
            if (!gps.isMissingNode() && !gps.isNull()) {
                System.out.println("\n[GPS Info]");
                System.out.println("----------------------------------------");
                System.out.printf("   위도: %.6f%n", gps.path("latitude").asDouble());
                System.out.printf("   경도: %.6f%n", gps.path("longitude").asDouble());
            }

            // 검증 결과
            JsonNode validation = root.path("validation");
            if (!validation.isMissingNode()) {
                System.out.println("\n[Validation]");
                System.out.println("----------------------------------------");
                String overallStatus = validation.path("overallStatus").asText();
                String statusMark = getStatusMark(overallStatus);
                System.out.println("   전체: " + statusMark + " " + overallStatus);
                System.out.println("   메시지: " + validation.path("overallMessage").asText());

                // 각 필드별 검증 결과
                printValidationField("중량", validation.path("weight"));
                printValidationField("날짜/시간", validation.path("dateTime"));
                printValidationField("GPS", validation.path("gps"));
                printValidationField("차량번호", validation.path("vehicle"));
            }

            // 신뢰도
            if (!root.path("confidence").isMissingNode() && !root.path("confidence").isNull()) {
                System.out.println("\n[Confidence] " +
                        String.format("%.1f%%", root.path("confidence").asDouble() * 100));
            }

        } catch (Exception e) {
            // 파싱 실패 시 원본 JSON 출력
            System.out.println(jsonResponse);
        }
    }

    private void printField(String label, JsonNode node) {
        printField(label, node, null);
    }

    private void printField(String label, JsonNode node, String unit) {
        if (node.isMissingNode() || node.isNull()) {
            System.out.println("   " + label + ": -");
        } else {
            String value = node.isNumber() ? String.format("%,d", node.asInt()) : node.asText();
            System.out.println("   " + label + ": " + value + (unit != null ? " " + unit : ""));
        }
    }

    private void printValidationField(String label, JsonNode node) {
        if (node.isMissingNode() || node.isNull())
            return;

        String status = node.path("status").asText();
        String mark = getStatusMark(status);
        System.out.println("   " + label + ": " + mark + " " + status);
    }

    private String getStatusMark(String status) {
        return switch (status) {
            case "VALID" -> "[PASS]";
            case "WARNING" -> "[WARN]";
            case "INVALID" -> "[FAIL]";
            case "CANNOT_VALIDATE" -> "[SKIP]";
            default -> "[INFO]";
        };
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("############################################################");
        System.out.println("#                                                          #");
        System.out.println("#           OCR 계량증명서 파싱 데모 클라이언트            #");
        System.out.println("#                                                          #");
        System.out.println("############################################################");
        System.out.println();
        System.out.println("Server URL: " + API_BASE_URL);
        System.out.println("Sample Dir: " + SAMPLE_DATA_DIR);
        System.out.println();
    }

    private static void printFooter() {
        System.out.println("============================================================");
        System.out.println("모든 샘플 파일 파싱 완료.");
        System.out.println("============================================================");
    }
}
