package RECO.Internship.Assignment.application;

import RECO.Internship.Assignment.api.dto.ParsedResultResponse;
import RECO.Internship.Assignment.domain.parser.FieldExtractor;
import RECO.Internship.Assignment.domain.validator.DateTimeValidator;
import RECO.Internship.Assignment.domain.validator.GpsValidator;
import RECO.Internship.Assignment.domain.validator.VehicleValidator;
import RECO.Internship.Assignment.domain.validator.WeightValidator;
import RECO.Internship.Assignment.infrastructure.file.OcrFileReader;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * OCR 파싱 서비스
 * 파일 읽기 → 필드 추출 → 검증 → 결과 반환
 */
@Service
@RequiredArgsConstructor
public class OcrParseService {

    private static final Logger log = LoggerFactory.getLogger(OcrParseService.class);

    private final OcrFileReader ocrFileReader;
    private final FieldExtractor fieldExtractor;
    private final WeightValidator weightValidator;
    private final DateTimeValidator dateTimeValidator;
    private final GpsValidator gpsValidator;
    private final VehicleValidator vehicleValidator;

    /**
     * MultipartFile로부터 OCR 파싱 수행
     */
    public ParsedResultResponse parseFromFile(MultipartFile file) throws IOException {
        log.info("OCR 파싱 시작 - 파일명: {}", file.getOriginalFilename());

        // 1. JSON 파일에서 텍스트 추출
        String ocrText = ocrFileReader.readFromMultipartFile(file);
        Double confidence = ocrFileReader.extractConfidence(file);

        // 2. 필드 추출
        return extractAndValidate(ocrText, confidence);
    }

    /**
     * JSON 문자열로부터 OCR 파싱 수행
     */
    public ParsedResultResponse parseFromJson(String jsonContent) {
        log.info("OCR 파싱 시작 - JSON 직접 입력");

        String ocrText = ocrFileReader.readFromString(jsonContent);
        Double confidence = ocrFileReader.extractConfidenceFromString(jsonContent);

        return extractAndValidate(ocrText, confidence);
    }

    /**
     * 필드 추출 및 검증 수행
     */
    private ParsedResultResponse extractAndValidate(String ocrText, Double confidence) {
        // 필드 추출
        String documentType = fieldExtractor.extractDocumentType(ocrText);
        String date = fieldExtractor.extractDate(ocrText);
        String time = fieldExtractor.extractTime(ocrText);
        String vehicleNumber = fieldExtractor.extractVehicleNumber(ocrText);
        Integer totalWeight = fieldExtractor.extractTotalWeight(ocrText);
        Integer emptyWeight = fieldExtractor.extractEmptyWeight(ocrText);
        Integer netWeight = fieldExtractor.extractNetWeight(ocrText);
        String customer = fieldExtractor.extractCustomer(ocrText);
        String productName = fieldExtractor.extractProductName(ocrText);
        String issuer = fieldExtractor.extractIssuer(ocrText);
        double[] gpsCoords = fieldExtractor.extractGpsCoordinates(ocrText);

        // === 모든 검증 수행 ===

        // 1. 중량 검증
        WeightValidator.ValidationResult weightResult = weightValidator.validateWeightCalculation(
                totalWeight, emptyWeight, netWeight);
        ParsedResultResponse.FieldValidation weightValidation = ParsedResultResponse.FieldValidation.builder()
                .status(weightResult.status().name())
                .message(weightResult.message())
                .value(weightResult.calculatedNetWeight())
                .build();

        // 2. 날짜/시간 검증
        DateTimeValidator.ValidationResult dateResult = dateTimeValidator.validateDate(date);
        DateTimeValidator.ValidationResult timeResult = dateTimeValidator.validateTime(time);
        String dateTimeStatus = combineStatus(dateResult.status().name(), timeResult.status().name());
        String dateTimeMessage = combineMessages(dateResult.message(), timeResult.message());
        ParsedResultResponse.FieldValidation dateTimeValidation = ParsedResultResponse.FieldValidation.builder()
                .status(dateTimeStatus)
                .message(dateTimeMessage)
                .build();

        // 3. GPS 검증
        GpsValidator.ValidationResult gpsResult = gpsValidator.validateCoordinates(gpsCoords);
        ParsedResultResponse.FieldValidation gpsValidation = ParsedResultResponse.FieldValidation.builder()
                .status(gpsResult.status().name())
                .message(gpsResult.message())
                .build();

        // 4. 차량번호 검증
        VehicleValidator.ValidationResult vehicleResult = vehicleValidator.validateVehicleNumber(vehicleNumber);
        ParsedResultResponse.FieldValidation vehicleValidation = ParsedResultResponse.FieldValidation.builder()
                .status(vehicleResult.status().name())
                .message(vehicleResult.message())
                .build();

        // 전체 검증 상태 종합
        String overallStatus = determineOverallStatus(
                weightResult.status().name(),
                dateTimeStatus,
                gpsResult.status().name(),
                vehicleResult.status().name());
        String overallMessage = buildOverallMessage(overallStatus);

        // GPS 정보 구성
        ParsedResultResponse.GpsInfo gpsInfo = null;
        if (gpsCoords != null && gpsCoords.length >= 2) {
            gpsInfo = ParsedResultResponse.GpsInfo.builder()
                    .latitude(gpsCoords[0])
                    .longitude(gpsCoords[1])
                    .build();
        }

        // 검증 결과 구성
        ParsedResultResponse.ValidationInfo validationInfo = ParsedResultResponse.ValidationInfo.builder()
                .overallStatus(overallStatus)
                .overallMessage(overallMessage)
                .weight(weightValidation)
                .dateTime(dateTimeValidation)
                .gps(gpsValidation)
                .vehicle(vehicleValidation)
                .build();

        log.info("OCR 파싱 완료 - 문서종류: {}, 전체검증: {}", documentType, overallStatus);

        return ParsedResultResponse.builder()
                .documentType(documentType)
                .date(date)
                .time(time)
                .vehicleNumber(vehicleNumber)
                .totalWeight(totalWeight)
                .emptyWeight(emptyWeight)
                .netWeight(netWeight)
                .customer(customer)
                .productName(productName)
                .issuer(issuer)
                .gps(gpsInfo)
                .validation(validationInfo)
                .confidence(confidence)
                .build();
    }

    /**
     * 두 상태 중 더 심각한 상태 반환
     */
    private String combineStatus(String status1, String status2) {
        if ("INVALID".equals(status1) || "INVALID".equals(status2)) {
            return "INVALID";
        }
        if ("WARNING".equals(status1) || "WARNING".equals(status2)) {
            return "WARNING";
        }
        if ("CANNOT_VALIDATE".equals(status1) || "CANNOT_VALIDATE".equals(status2)) {
            return "CANNOT_VALIDATE";
        }
        return "VALID";
    }

    /**
     * 메시지 결합
     */
    private String combineMessages(String msg1, String msg2) {
        List<String> messages = new ArrayList<>();
        if (msg1 != null && !msg1.isBlank())
            messages.add(msg1);
        if (msg2 != null && !msg2.isBlank())
            messages.add(msg2);
        return String.join("; ", messages);
    }

    /**
     * 전체 검증 상태 결정
     */
    private String determineOverallStatus(String... statuses) {
        for (String status : statuses) {
            if ("INVALID".equals(status))
                return "INVALID";
        }
        for (String status : statuses) {
            if ("WARNING".equals(status))
                return "WARNING";
        }
        for (String status : statuses) {
            if ("CANNOT_VALIDATE".equals(status))
                return "CANNOT_VALIDATE";
        }
        return "VALID";
    }

    /**
     * 전체 검증 메시지 생성
     */
    private String buildOverallMessage(String overallStatus) {
        return switch (overallStatus) {
            case "VALID" -> "모든 검증 통과";
            case "WARNING" -> "일부 항목에 경고가 있습니다";
            case "INVALID" -> "검증 실패 항목이 있습니다";
            case "CANNOT_VALIDATE" -> "일부 항목을 검증할 수 없습니다";
            default -> "검증 완료";
        };
    }
}
