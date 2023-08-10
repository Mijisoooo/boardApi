package practice.board.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import practice.board.response.Response;

import java.net.BindException;

import static org.springframework.http.HttpStatus.*;


@RestControllerAdvice
@Slf4j
public class ApiExceptionAdvice {

    //TODO 응답 객체로 ResponseEntity<Reponse> 사용했는데, ErrorResponse 도 사용 가능
    //TODO exception을 하나하나 다 만들어줘야하나? ex) MemberNotFoundException extends ApiException {} 요렇게??
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Response> apiException(ApiException e) {
        log.error("[ApiExceptionAdvice.apiException 호출] {}", e.getMessage());
        HttpStatus status = e.getErrorCode().getHttpStatus();

        return ResponseEntity
                .status(status)
                .body(Response.failure(e.getErrorCode().getCode(), e.getMessage()));
    }


    //@Valid 에서 예외 발생
    @ExceptionHandler(BindException.class)
    @ResponseStatus(BAD_REQUEST)
    public Response bindException(BindException e) {
        log.error("@Valid 예외 발생", e.getMessage());
        return Response.failure(400, e.getMessage());
    }

    //JSON 파싱 예외
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(BAD_REQUEST)
    public Response httpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("[JSON 파싱 예외 발생] {}", e.getMessage());
        return Response.failure(400, e.getMessage());

    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public Response exception(Exception e) {
        log.error("[ApiExceptionAdvice.exception 호출] {}", e.getMessage());
        return Response.failure(500, e.getMessage());
    }
    




}
