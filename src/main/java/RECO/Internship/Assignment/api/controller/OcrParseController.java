package RECO.Internship.Assignment.api.controller;

import RECO.Internship.Assignment.api.dto.ParsedResultResponse;
import RECO.Internship.Assignment.application.OcrParseService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * OCR 파싱 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrParseController {

    private static final Logger log = LoggerFactory.getLogger(OcrParseController.class);

    private final OcrParseService ocrParseService;

    /**
     * 단일 OCR JSON 파일 파싱
     * POST /api/ocr/parse
     */
    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParsedResultResponse> parseOcrFile(
            @RequestParam("file") MultipartFile file) throws IOException {

        log.info("파싱 요청 - 파일명: {}, 크기: {} bytes",
                file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        ParsedResultResponse result = ocrParseService.parseFromFile(file);
        return ResponseEntity.ok(result);
    }

    /**
     * JSON 문자열 직접 파싱
     * POST /api/ocr/parse/json
     */
    @PostMapping(value = "/parse/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ParsedResultResponse> parseOcrJson(
            @RequestBody String jsonContent) {

        log.info("JSON 직접 파싱 요청");

        ParsedResultResponse result = ocrParseService.parseFromJson(jsonContent);
        return ResponseEntity.ok(result);
    }

    /**
     * 다중 파일 일괄 파싱
     * POST /api/ocr/parse/batch
     */
    @PostMapping(value = "/parse/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ParsedResultResponse>> parseOcrFiles(
            @RequestParam("files") MultipartFile[] files) throws IOException {

        log.info("일괄 파싱 요청 - 파일 수: {}", files.length);

        List<ParsedResultResponse> results = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                results.add(ocrParseService.parseFromFile(file));
            }
        }

        return ResponseEntity.ok(results);
    }

    /**
     * 헬스체크 엔드포인트
     * GET /api/ocr/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
