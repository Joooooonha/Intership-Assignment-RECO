package RECO.Internship.Assignment.domain.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OCR 텍스트에서 필드를 추출하는 클래스
 * 정규표현식을 사용하여 각 필드 값을 추출
 */
@Component
public class FieldExtractor {

    private static final Logger log = LoggerFactory.getLogger(FieldExtractor.class);

    // 정규표현식 패턴 (컴파일 캐싱으로 성능 최적화)
    private static final Pattern DOCUMENT_TYPE_PATTERN = Pattern
            .compile("(계\\s*량\\s*증\\s*명\\s*서|계\\s*[근그]\\s*표|계\\s*량\\s*확\\s*인\\s*서|계\\s*량\\s*증\\s*명\\s*표)");

    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})[-./](\\d{1,2})[-./](\\d{1,2})");

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2})");

    private static final Pattern VEHICLE_NUMBER_PATTERN = Pattern
            .compile("(?:차량\\s*(?:번호|No\\.?)|차\\s*번\\s*호)[:\\s]*([\\d가-힣]+)|([0-9]{2,4}[가-힣][0-9]{4})");

    private static final Pattern TOTAL_WEIGHT_PATTERN = Pattern.compile(
            "총\\s*중\\s*량[^0-9]*(?:\\d{2}:\\d{2}:\\d{2}\\s+)?([\\d,]+)\\s*(?:kg|KG)?", Pattern.CASE_INSENSITIVE);

    private static final Pattern EMPTY_WEIGHT_PATTERN = Pattern.compile(
            "(?:공\\s*차\\s*중\\s*량|차\\s*중\\s*량)[^0-9]*(?:\\d{2}:\\d{2}:\\d{2}\\s+)?([\\d,]+)\\s*(?:kg|KG)?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern NET_WEIGHT_PATTERN = Pattern.compile("실\\s*중\\s*량[^0-9]*([\\d,]+)\\s*(?:kg|KG)?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CUSTOMER_PATTERN = Pattern
            .compile("(?:거\\s*래\\s*처|상\\s*호)[:\\s]*([가-힣a-zA-Z0-9()\\s]+?)(?:\\s*(?:품|총|공차|실|차량|계량)|$)");

    private static final Pattern PRODUCT_PATTERN = Pattern.compile("품\\s*명[:\\s]*([가-힣a-zA-Z0-9]+)");

    private static final Pattern ISSUER_PATTERN = Pattern.compile("([가-힣]+(?:\\([주株]\\)|\\(주\\)|주식회사))");

    private static final Pattern GPS_PATTERN = Pattern.compile("(\\d{2,3}\\.\\d+)[,\\s]+(\\d{2,3}\\.\\d+)");

    /**
     * 텍스트 전처리 - 노이즈 제거
     */
    public String preprocessText(String text) {
        if (text == null) {
            return "";
        }
        log.debug("텍스트 전처리 시작 - 원본 길이: {} 글자", text.length());

        // 이스케이프된 줄바꿈을 실제 줄바꿈으로 변환
        String processed = text.replace("\\n", "\n");

        log.debug("텍스트 전처리 완료");
        return processed;
    }

    /**
     * 문서 종류 추출
     */
    public String extractDocumentType(String text) {
        Matcher matcher = DOCUMENT_TYPE_PATTERN.matcher(text);
        if (matcher.find()) {
            String result = matcher.group(1).replaceAll("\\s+", "");
            log.debug("문서종류 추출: {}", result);
            return result;
        }
        log.warn("문서종류를 찾을 수 없습니다");
        return null;
    }

    /**
     * 날짜 추출 (yyyy-MM-dd 형식)
     */
    public String extractDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        if (matcher.find()) {
            String year = matcher.group(1);
            String month = String.format("%02d", Integer.parseInt(matcher.group(2)));
            String day = String.format("%02d", Integer.parseInt(matcher.group(3)));
            String result = year + "-" + month + "-" + day;
            log.debug("날짜 추출: {}", result);
            return result;
        }
        log.warn("날짜를 찾을 수 없습니다");
        return null;
    }

    /**
     * 시간 추출 (HH:mm:ss 형식)
     */
    public String extractTime(String text) {
        Matcher matcher = TIME_PATTERN.matcher(text);
        if (matcher.find()) {
            String result = matcher.group(0);
            log.debug("시간 추출: {}", result);
            return result;
        }
        log.debug("시간을 찾을 수 없습니다");
        return null;
    }

    /**
     * 차량번호 추출
     */
    public String extractVehicleNumber(String text) {
        Matcher matcher = VEHICLE_NUMBER_PATTERN.matcher(text);
        if (matcher.find()) {
            String result = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (result != null) {
                result = result.trim();
                log.debug("차량번호 추출: {}", result);
                return result;
            }
        }
        log.warn("차량번호를 찾을 수 없습니다");
        return null;
    }

    /**
     * 총중량 추출 (kg 단위 정수)
     */
    public Integer extractTotalWeight(String text) {
        Matcher matcher = TOTAL_WEIGHT_PATTERN.matcher(text);
        if (matcher.find()) {
            String weightStr = matcher.group(1).replace(",", "");
            try {
                int result = Integer.parseInt(weightStr);
                log.debug("총중량 추출: {} kg", result);
                return result;
            } catch (NumberFormatException e) {
                log.warn("총중량 파싱 실패: {}", weightStr);
            }
        }
        log.warn("총중량을 찾을 수 없습니다");
        return null;
    }

    /**
     * 공차중량 추출 (kg 단위 정수)
     */
    public Integer extractEmptyWeight(String text) {
        Matcher matcher = EMPTY_WEIGHT_PATTERN.matcher(text);
        if (matcher.find()) {
            String weightStr = matcher.group(1).replace(",", "");
            try {
                int result = Integer.parseInt(weightStr);
                log.debug("공차중량 추출: {} kg", result);
                return result;
            } catch (NumberFormatException e) {
                log.warn("공차중량 파싱 실패: {}", weightStr);
            }
        }
        log.warn("공차중량을 찾을 수 없습니다");
        return null;
    }

    /**
     * 실중량 추출 (kg 단위 정수)
     */
    public Integer extractNetWeight(String text) {
        Matcher matcher = NET_WEIGHT_PATTERN.matcher(text);
        if (matcher.find()) {
            String weightStr = matcher.group(1).replace(",", "");
            try {
                int result = Integer.parseInt(weightStr);
                log.debug("실중량 추출: {} kg", result);
                return result;
            } catch (NumberFormatException e) {
                log.warn("실중량 파싱 실패: {}", weightStr);
            }
        }
        log.warn("실중량을 찾을 수 없습니다");
        return null;
    }

    /**
     * 거래처/상호 추출
     */
    public String extractCustomer(String text) {
        Matcher matcher = CUSTOMER_PATTERN.matcher(text);
        if (matcher.find()) {
            String result = matcher.group(1).trim();
            log.debug("거래처 추출: {}", result);
            return result;
        }
        log.debug("거래처를 찾을 수 없습니다");
        return null;
    }

    /**
     * 품명 추출
     */
    public String extractProductName(String text) {
        Matcher matcher = PRODUCT_PATTERN.matcher(text);
        if (matcher.find()) {
            String result = matcher.group(1).trim();
            log.debug("품명 추출: {}", result);
            return result;
        }
        log.debug("품명을 찾을 수 없습니다");
        return null;
    }

    /**
     * 발행업체 추출
     */
    public String extractIssuer(String text) {
        Matcher matcher = ISSUER_PATTERN.matcher(text);
        if (matcher.find()) {
            String result = matcher.group(1);
            log.debug("발행업체 추출: {}", result);
            return result;
        }
        log.debug("발행업체를 찾을 수 없습니다");
        return null;
    }

    /**
     * GPS 좌표 추출
     * 
     * @return double[] {위도, 경도} 또는 null
     */
    public double[] extractGpsCoordinates(String text) {
        Matcher matcher = GPS_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                double latitude = Double.parseDouble(matcher.group(1));
                double longitude = Double.parseDouble(matcher.group(2));
                log.debug("GPS 추출: {}, {}", latitude, longitude);
                return new double[] { latitude, longitude };
            } catch (NumberFormatException e) {
                log.warn("GPS 파싱 실패");
            }
        }
        log.debug("GPS를 찾을 수 없습니다");
        return null;
    }
}
