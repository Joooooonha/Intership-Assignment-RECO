package RECO.Internship.Assignment.api.controller;

import RECO.Internship.Assignment.api.dto.ParsedResultResponse;
import RECO.Internship.Assignment.application.OcrParseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OcrParseController.class)
@DisplayName("OcrParseController 테스트")
class OcrParseControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private OcrParseService ocrParseService;

        @Nested
        @DisplayName("POST /api/ocr/parse")
        class ParseOcrFile {

                @Test
                @DisplayName("파일 업로드 성공 시 파싱 결과를 반환한다")
                void parseOcrFile_success() throws Exception {
                        // given
                        String jsonContent = """
                                        {
                                            "text": "계량증명서\\n계량일자: 2026-02-02\\n차량번호: 80구8713\\n총중량: 12,480 kg",
                                            "confidence": 0.95
                                        }
                                        """;

                        MockMultipartFile file = new MockMultipartFile(
                                        "file",
                                        "sample.json",
                                        MediaType.APPLICATION_JSON_VALUE,
                                        jsonContent.getBytes());

                        // 새로운 ValidationInfo 구조에 맞게 수정
                        ParsedResultResponse mockResponse = ParsedResultResponse.builder()
                                        .documentType("계량증명서")
                                        .date("2026-02-02")
                                        .vehicleNumber("80구8713")
                                        .totalWeight(12480)
                                        .emptyWeight(7470)
                                        .netWeight(5010)
                                        .confidence(0.95)
                                        .validation(ParsedResultResponse.ValidationInfo.builder()
                                                        .overallStatus("VALID")
                                                        .overallMessage("모든 검증 통과")
                                                        .weight(ParsedResultResponse.FieldValidation.builder()
                                                                        .status("VALID")
                                                                        .message("검증 성공")
                                                                        .value(5010)
                                                                        .build())
                                                        .dateTime(ParsedResultResponse.FieldValidation.builder()
                                                                        .status("VALID")
                                                                        .message("날짜/시간 형식 유효")
                                                                        .build())
                                                        .gps(ParsedResultResponse.FieldValidation.builder()
                                                                        .status("VALID")
                                                                        .message("GPS 좌표 유효")
                                                                        .build())
                                                        .vehicle(ParsedResultResponse.FieldValidation.builder()
                                                                        .status("VALID")
                                                                        .message("차량번호 유효")
                                                                        .build())
                                                        .build())
                                        .build();

                        given(ocrParseService.parseFromFile(any())).willReturn(mockResponse);

                        // when & then
                        mockMvc.perform(multipart("/api/ocr/parse")
                                        .file(file))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.documentType").value("계량증명서"))
                                        .andExpect(jsonPath("$.date").value("2026-02-02"))
                                        .andExpect(jsonPath("$.vehicleNumber").value("80구8713"))
                                        .andExpect(jsonPath("$.totalWeight").value(12480))
                                        .andExpect(jsonPath("$.validation.overallStatus").value("VALID"));
                }

                @Test
                @DisplayName("빈 파일 업로드 시 400 에러를 반환한다")
                void parseOcrFile_emptyFile_returnsBadRequest() throws Exception {
                        // given
                        MockMultipartFile emptyFile = new MockMultipartFile(
                                        "file",
                                        "empty.json",
                                        MediaType.APPLICATION_JSON_VALUE,
                                        new byte[0]);

                        // when & then
                        mockMvc.perform(multipart("/api/ocr/parse")
                                        .file(emptyFile))
                                        .andExpect(status().isBadRequest());
                }
        }

        @Nested
        @DisplayName("POST /api/ocr/parse/json")
        class ParseOcrJson {

                @Test
                @DisplayName("JSON 직접 입력 시 파싱 결과를 반환한다")
                void parseOcrJson_success() throws Exception {
                        // given
                        String jsonContent = """
                                        {
                                            "text": "계량증명서\\n계량일자: 2026-02-02",
                                            "confidence": 0.90
                                        }
                                        """;

                        ParsedResultResponse mockResponse = ParsedResultResponse.builder()
                                        .documentType("계량증명서")
                                        .date("2026-02-02")
                                        .confidence(0.90)
                                        .build();

                        given(ocrParseService.parseFromJson(any())).willReturn(mockResponse);

                        // when & then
                        mockMvc.perform(post("/api/ocr/parse/json")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(jsonContent))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.documentType").value("계량증명서"));
                }
        }

        @Nested
        @DisplayName("GET /api/ocr/health")
        class HealthCheck {

                @Test
                @DisplayName("헬스체크 성공")
                void health_returnsOk() throws Exception {
                        mockMvc.perform(get("/api/ocr/health"))
                                        .andExpect(status().isOk())
                                        .andExpect(content().string("OK"));
                }
        }
}
