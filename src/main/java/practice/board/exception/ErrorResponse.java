package practice.board.exception;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {

    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final String code;
    private final String message;

    public ErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getHttpStatus().value();
        this.error = errorCode.getHttpStatus().name();
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
    }

    /**
     * message 직접 입력
     */
    public ErrorResponse(ErrorCode errorCode, String message) {
        this.status = errorCode.getHttpStatus().value();
        this.error = errorCode.getHttpStatus().name();
        this.code = errorCode.name();
        this.message = message;
    }
}
