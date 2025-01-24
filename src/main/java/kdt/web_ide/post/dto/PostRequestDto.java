package kdt.web_ide.post.dto;

import kdt.web_ide.post.entity.Language;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PostRequestDto {
    private Integer boardId;
    private String name;
    private Language language;
}
