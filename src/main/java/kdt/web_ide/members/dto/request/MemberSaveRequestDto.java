package kdt.web_ide.members.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberSaveRequestDto {
    String email;
    String userName;
    String password;
}
