package practice.board.service.file;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import practice.board.exception.ApiException;
import practice.board.exception.ErrorCode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static practice.board.exception.ErrorCode.*;

@Slf4j
@Service
public class LocalFileService implements FileService {
    //file 을 서버에 저장, 삭제하는 역할

    private static final String fileDir = "/Users/jisuham/files/";  //TODO 다른 좋은 방법 있을까? @Value 보다는 이 방식이 나은 것 같음


    /**
     * fileDir 경로에 파일 없으면 생성
     */
    @PostConstruct
    void postConstruct() {
        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    /**
     * 파일을 서버에 저장
     */
    @Override
    public String save(MultipartFile file) {

        String filePath = fileDir + UUID.randomUUID();

        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            log.error("[LocalFileService.save() 예외 발생]", e);
            throw new ApiException(FILE_NOT_SAVED, e.getMessage());
        }

        return filePath;
    }

    /**
     * 파일 삭제
     */
    @Override
    public void delete(String filePath) {
        try {
            Path path = Path.of(filePath);
            if (Files.deleteIfExists(path)) {
                log.info("파일 삭제 성공. filePath={}", filePath);
            } else {
                log.info("파일이 존재하지 않음. filePath={}", filePath);
                throw new ApiException(ErrorCode.FILE_NOT_FOUND);
            }
        } catch(IOException e) {
            log.error("[LocalFileService.delete() 예외 발생]", e);
            throw new ApiException(FILE_DELETION_FAILED, e.getMessage());
        }


    }

}
