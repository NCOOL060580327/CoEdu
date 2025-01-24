package kdt.web_ide.members.dto.request;

import kdt.web_ide.members.entity.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserInfoDto {
    Long memberId;
    String loginId;
    List<RoleType> roles; // 역할을 리스트로 변경
}
