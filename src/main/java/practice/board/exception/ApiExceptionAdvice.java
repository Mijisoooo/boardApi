package practice.board.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import practice.board.response.Response;

import java.net.BindException;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;


@RestControllerAdvice
@Slf4j
public class ApiExceptionAdvice {

    //TODO 응답 객체로 ResponseEntity<Reponse> 사용했는데, ErrorResponse 도 사용 가능
    //TODO exception을 하나하나 다 만들어줘야하나? ex) MemberNotFoundException extends ApiException {} 요렇게??


    //400 응답
    //JWT 관련 예외
    @ExceptionHandler({SignatureException.class, MalformedJwtException.class, ExpiredJwtException.class, UnsupportedJwtException.class})
    @ResponseStatus(UNAUTHORIZED)
    public Response malformedJwtException(Exception e) {
        return Response.failure(400, e.getMessage());
    }

    //ApiException
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Response> apiException(ApiException e) {
        log.error("[ApiExceptionAdvice.apiException 호출] {}", e.getMessage());
        HttpStatus status = e.getErrorCode().getHttpStatus();

        return ResponseEntity
                .status(status)
                .body(Response.failure(e.getErrorCode().getCode(), e.getMessage()));
    }


    //400 응답
    //@Valid 에서 예외 발생
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(BAD_REQUEST)
    public Response validException(Exception e) {
        log.error("@Valid 예외 발생", e.getMessage());
        return Response.failure(400, e.getMessage());
    }

    //400 응답
    //JSON 파싱 예외 (json 형식에 오류 있는 경우)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(BAD_REQUEST)
    public Response httpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("[JSON 파싱 예외 발생] {}", e);
        return Response.failure(400, e.getMessage());
    }

    //500 응답
    //기타 예외
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response exception(Exception e) {
        log.error("[ApiExceptionAdvice.exception 호출] {}", e.getMessage());
        e.printStackTrace();
        return Response.failure(500, e.getMessage());
    }
    




}
