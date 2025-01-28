package kdt.web_ide.post.dto;

import kdt.web_ide.post.entity.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PostResponseDto {
    private Integer id;
    private Integer boardId;
    private String name;
    private Language language;
    private String filePath;
    private String createdAt;
    private Integer roomId;
}

