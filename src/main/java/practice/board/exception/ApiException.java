package practice.board.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private String message;

    public ApiException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }

}
