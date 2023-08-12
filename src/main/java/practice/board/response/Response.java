package practice.board.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;


@JsonInclude(JsonInclude.Include.NON_NULL)  //null값을 가지는 필드는 JSON 응답에 포함 X
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Response<T> {

    private boolean isSuccess;
    private int code;
    private Result<T> result;

    public static Response success() {
        return new Response(true, 0, null);
    }  //TODO 왜 code 에 null이 아니라 0 ??

    public static <T> Response<T> success(T data) {
        return new Response<>(true, 0, new Success<>(data));
    }

    public static Response failure(int code, String msg) {
        return new Response(false, code, new Failure(msg));
    }

}
