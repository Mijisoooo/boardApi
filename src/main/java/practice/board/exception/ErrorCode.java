package practice.board.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생"),

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),

    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "파라미터에 가능한 값이 아닙니다."),

    LOGIN_FAILURE(UNAUTHORIZED, "로그인에 실패하였습니다. (아이디 혹은 비밀번호 오류)"),

    INVALID_TOKEN(UNAUTHORIZED, "토큰이 유효하지 않습니다."),

    MALFORMED_TOKEN(UNAUTHORIZED, "올바르지 않은 토큰입니다."),

    EXPIRED_ACCESS_TOKEN(UNAUTHORIZED, "만료된 토큰입니다. 리프레쉬 토큰이 필요합니다."),

    ARTICLE_NOT_FOUND(NOT_FOUND, "게시글 정보를 찾을 수 없습니다."),

    MEMBER_NOT_FOUND(NOT_FOUND, "회원 정보를 찾을 수 없습니다."),

    COMMENT_NOT_FOUND(NOT_FOUND, "댓글 정보를 찾을 수 없습니다."),

    WRONG_PASSWORD(UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),

    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST,
            "형식에 맞지 않는 비밀번호입니다. (비밀번호는 적어도 한개의 대문자, 소문자, 숫자, 특수기호(._?!*)를 포함하며, 8자리 이상 20자리 이하여야 합니다.)"),

    DUPLICATE_USERNAME_FOUND(CONFLICT, "이미 존재하는 username 입니다."),

    DUPLICATE_EMAIL_FOUND(CONFLICT, "이미 존재하는 email 입니다."),

    DUPLICATE_NICKNAME_FOUND(CONFLICT, "이미 존재하는 nickname 입니다."),

    FILE_NOT_SAVED(HttpStatus.BAD_REQUEST, "파일 저장에 실패하였습니다."),

    FILE_NOT_FOUND(NOT_FOUND, "파일이 존재하지 않습니다."),

    FILE_DELETION_FAILED(HttpStatus.BAD_REQUEST, "파일 삭제에 실패하였습니다."),

    DISLIKEARTICLE_NOT_FOUND(HttpStatus.BAD_REQUEST, "싫어요한 적이 없는 게시글입니다."),  //TODO 메세지 수정 필요

    SELF_LIKE_ARTICLE(HttpStatus.BAD_REQUEST, "본인 글에는 좋아요가 불가능합니다."),

    SELF_DISLIKE_ARTICLE(HttpStatus.BAD_REQUEST, "본인 글에는 싫어요가 불가능합니다."),

    LIKEARTICLE_NOT_FOUND(HttpStatus.BAD_REQUEST, "좋아요한 적이 없는 게시글입니다.");




    private final HttpStatus httpStatus;  //TODO final 붙은 이유
    private final String message;

    public int getCode() {
        return this.getHttpStatus().value();
    }
}
