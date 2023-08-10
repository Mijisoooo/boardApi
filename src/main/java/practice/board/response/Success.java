package practice.board.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)  //null 값을 가지는 필드 JSON 응답에 포함 X
@Getter
@AllArgsConstructor
public class Success<T> implements Result {

    private T data;

}
