package RECO.Internship.Assignment.api.exception;

import RECO.Internship.Assignment.api.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 전역 예외 처리기
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        /**
         * IllegalArgumentException 처리 (잘못된 인자)
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(
                        IllegalArgumentException e, HttpServletRequest request) {

                log.warn("잘못된 요청: {}", e.getMessage());

                return ResponseEntity.badRequest()
                                .body(ErrorResponse.of(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "Bad Request",
                                                e.getMessage(),
                                                request.getRequestURI()));
        }

        /**
         * IOException 처리 (파일 읽기 오류)
         */
        @ExceptionHandler(IOException.class)
        public ResponseEntity<ErrorResponse> handleIOException(
                        IOException e, HttpServletRequest request) {

                log.error("파일 처리 오류: {}", e.getMessage());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ErrorResponse.of(
                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                "File Processing Error",
                                                "파일 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                                request.getRequestURI()));
        }

        /**
         * MaxUploadSizeExceededException 처리 (파일 크기 초과)
         */
        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ErrorResponse> handleMaxUploadSize(
                        MaxUploadSizeExceededException e, HttpServletRequest request) {

                log.warn("파일 크기 초과: {}", e.getMessage());

                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                                .body(ErrorResponse.of(
                                                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                                                "Payload Too Large",
                                                "업로드 파일 크기가 제한을 초과했습니다",
                                                request.getRequestURI()));
        }

        /**
         * 기타 예외 처리
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleException(
                        Exception e, HttpServletRequest request) {

                log.error("서버 오류: {}", e.getMessage(), e);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ErrorResponse.of(
                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                "Internal Server Error",
                                                "서버 내부 오류가 발생했습니다",
                                                request.getRequestURI()));
        }
}
