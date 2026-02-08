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

        // 중량 검증
        WeightValidator.ValidationResult weightResult = weightValidator.validateWeightCalculation(totalWeight,
                emptyWeight, netWeight);

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
                .status(weightResult.status().name())
                .message(weightResult.message())
                .calculatedNetWeight(weightResult.calculatedNetWeight())
                .build();

        log.info("OCR 파싱 완료 - 문서종류: {}, 검증결과: {}", documentType, weightResult.status());

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
}
