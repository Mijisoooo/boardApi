package practice.board.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import practice.board.exception.ApiException;
import practice.board.service.file.LocalFileService;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class FileServiceTest {

    private LocalFileService localFileService = new LocalFileService();

    @DisplayName("파일 저장 성공")
    @Test
    void save_success() {

        //given
        //MockMultipartFile 을 사용하여 임시 파일 생성
        String fileName = "test.txt";
        String content = "This is a test file";
        MultipartFile file = new MockMultipartFile(fileName, content.getBytes());

        //when
        String filePath = localFileService.save(file);

        //then
        assertThat(Files.exists(Path.of(filePath))).isTrue();
    }

    @DisplayName("파일 삭제 성공")
    @Test
    void delete_success() {

        //given
        //MockMultipartFile 을 사용하여 임시 파일 생성
        String fileName = "test.txt";
        String content = "This is a test file";
        MultipartFile file = new MockMultipartFile(fileName, content.getBytes());
        String filePath = localFileService.save(file);

        //when
        localFileService.delete(filePath);

        //then
        assertThat(Files.exists(Path.of(filePath))).isFalse();
        assertThatThrownBy(() -> localFileService.delete(filePath))
                .isInstanceOf(ApiException.class);

    }

    @DisplayName("똑같은 이름, 내용의 파일을 저장해도 성공")
    @Test
    void save_success_same_file() {
        //given
        String fileName = "test.txt";
        String content = "This is a test file";
        MultipartFile file1 = new MockMultipartFile(fileName, content.getBytes());
        MultipartFile file2 = new MockMultipartFile(fileName, content.getBytes());

        //when
        String filePath1 = localFileService.save(file1);
        String filePath2 = localFileService.save(file2);

        //then
        assertThat(Files.exists(Path.of(filePath1))).isTrue();
        assertThat(Files.exists(Path.of(filePath2))).isTrue();
    }





}