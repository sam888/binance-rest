package com.orderbook.rest.exception.handler;

import com.orderbook.rest.dto.BaseResponse;
import com.orderbook.rest.exception.InternalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

	@SuppressWarnings("unchecked")
	@ExceptionHandler({ InternalException.class })
	protected ResponseEntity<BaseResponse> handleOutcomeCode(Exception ex) {
		log.error("InternalException caught! ", ex );  // log stacktrace and code throwing the exception

		BaseResponse errorResponse = new BaseResponse();
		InternalException internalException = (InternalException) ex;

		errorResponse.setOutcomeCode( internalException.getOutcomeCode() );
		errorResponse.setOutcomeMessage( internalException.getOutcomeMessage() );
		errorResponse.setOutcomeUserMessage( internalException.getInternalMessage() );
		return new ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(RuntimeException.class)
	public final ResponseEntity<Object> runtimeExceptions(RuntimeException ex) {
		log.error( "RuntimeException caught! ", ex ); // log stacktrace and code throwing the exception

		BaseResponse errorResponse = new BaseResponse();
		errorResponse.setOutcomeCode("400");
		errorResponse.setOutcomeMessage(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
		errorResponse.setOutcomeUserMessage(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
		return new ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST);
	}


	@ExceptionHandler({MethodArgumentNotValidException.class})
	public ResponseEntity<BaseResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
		log.error( "MethodArgumentNotValidException caught! ", ex );  // log stacktrace and code throwing the exception

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{ ## ");
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			if ( errorMessage.startsWith("must not")) {
				errorMessage = fieldName + " " + errorMessage + " " + System.lineSeparator();
			}
			stringBuilder.append( errorMessage + " ## ");
		});
		stringBuilder.append("}");

		BaseResponse errorResponse = new BaseResponse();
		errorResponse.setOutcomeCode( "-1" );
		errorResponse.setOutcomeMessage( "Invalid input data" );
		errorResponse.setOutcomeUserMessage( stringBuilder.toString() );

		return new ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST);
	}
}
