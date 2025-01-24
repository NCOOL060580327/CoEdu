package kdt.web_ide.members.service;

import kdt.web_ide.members.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserInfoUpdateRequestDto {

    private String nickName;

    public UserInfoUpdateRequestDto(Member member, String profileImg){

    }
}
