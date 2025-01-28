package kdt.web_ide.boards.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardUpdateRequestDto {

    private String title;

    @Size(min = 1, max = 5, message = "제목 텍스트는 1~5글자여야 합니다.")
    private String titleText;
}
