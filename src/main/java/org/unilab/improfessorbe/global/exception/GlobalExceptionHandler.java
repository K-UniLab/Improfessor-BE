package org.unilab.improfessorbe.global.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.unilab.improfessorbe.global.common.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.error("Validation ERROR : {}", e.getBindingResult());
		return ResponseEntity.status(e.getStatusCode())
			.body(ApiResponse.error("400", "유효성 검사가 실패했습니다."));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ApiResponse.error("400", "요청 형식이 올바르지 않습니다."));
	}

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
		ErrorCode errorCode = e.getErrorCode();
		log.error("Custom ERROR: {}", errorCode.getMessage());
		return ResponseEntity.status(errorCode.getStatus())
			.body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
		log.error("Exception : {}", e.getMessage());
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		return ResponseEntity.status(errorCode.getStatus())
			.body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
	}





}
