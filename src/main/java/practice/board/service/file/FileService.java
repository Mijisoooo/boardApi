package practice.board.service.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String save(MultipartFile file);

    void delete(String filePath);
}
