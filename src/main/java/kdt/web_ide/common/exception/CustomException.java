package kdt.web_ide.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class CustomException extends Throwable {
    ErrorCode errorCode;

}
