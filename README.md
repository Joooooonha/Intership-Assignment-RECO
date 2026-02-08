# OCR 텍스트 파싱 API

계근지/영수증 OCR 결과를 파싱하여 구조화된 JSON으로 변환하는 Spring Boot REST API

## 기술 스택

- Java 17
- Spring Boot 3.x
- Gradle
- JUnit 5

## 아키텍처

### 4계층 구조

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────────┐  ┌─────────────────────────────────┐   │
│  │ ParserController│  │ DTO (ParseRequest/ParseResponse)│   │
│  └─────────────────┘  └─────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                    Application Layer                        │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                  ParserService                       │    │
│  │         (비즈니스 로직 조율 및 파이프라인 관리)         │    │
│  └─────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────┤
│                      Domain Layer                           │
│  ┌──────────────┐ ┌────────────┐ ┌───────────────────────┐  │
│  │FieldExtractor│ │ Normalizer │ │     Validator         │  │
│  │  (필드 추출)  │ │ (정규화)   │ │ (중량 검증 등)         │  │
│  └──────────────┘ └────────────┘ └───────────────────────┘  │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              WeightSlip (도메인 모델)                 │    │
│  └─────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────┤
│                   Infrastructure Layer                      │
│  ┌─────────────────┐    ┌─────────────────────────────┐     │
│  │  OcrFileReader  │    │      JsonExporter           │     │
│  │ (OCR 파일 읽기)  │    │    (결과 JSON 저장)          │     │
│  └─────────────────┘    └─────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

### 데이터 흐름

```
1. 파일 읽기      [Infrastructure] OcrFileReader
        ↓
2. 텍스트 추출    [Application] ParserService
        ↓
3. 노이즈 제거    [Domain] FieldExtractor
        ↓
4. 필드 추출      [Domain] FieldExtractor (정규표현식)
        ↓
5. 정규화        [Domain] DateNormalizer, WeightNormalizer
        ↓
6. 검증          [Domain] WeightValidator
        ↓
7. 결과 생성      [Infrastructure] JsonExporter
```

## 프로젝트 구조

```
src/main/java/RECO/Internship/Assignment/
├── presentation/
│   ├── controller/
│   │   └── ParserController.java
│   └── dto/
│       ├── ParseRequest.java
│       └── ParseResponse.java
├── application/
│   └── service/
│       └── ParserService.java
├── domain/
│   ├── parser/
│   │   └── FieldExtractor.java
│   ├── normalizer/
│   │   ├── DateNormalizer.java
│   │   └── WeightNormalizer.java
│   ├── validator/
│   │   └── WeightValidator.java
│   └── model/
│       └── WeightSlip.java
├── infrastructure/
│   ├── file/
│   │   └── OcrFileReader.java
│   └── output/
│       └── JsonExporter.java
└── AssignmentApplication.java
```

## 실행 방법

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 테스트
./gradlew test
```

## API 사용법

### 단일 파싱

```http
POST /api/v1/parse
Content-Type: application/json

{
  "text": "계량증명서 계량일자: 2026-02-02..."
}
```

### 응답 예시

```json
{
  "success": true,
  "data": {
    "documentType": "계량증명서",
    "measurementDate": "2026-02-02",
    "vehicleNumber": "8713",
    "totalWeight": 12480,
    "emptyWeight": 7470,
    "netWeight": 5010
  },
  "validation": {
    "weightValid": true
  }
}
```

## Git 브랜치 전략

```
main ← develop ← feat/#1, feat/#2...
```

## 라이선스

MIT License
