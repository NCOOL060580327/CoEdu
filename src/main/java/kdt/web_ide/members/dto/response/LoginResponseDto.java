package kdt.web_ide.members.dto.response;

import kdt.web_ide.members.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResponseDto {

    private Long memberId;
    private String name;
    private String email;
    private TokenResponse tokenResponse;

    @Builder
    public LoginResponseDto(Member member,TokenResponse tokenResponse){
        this.memberId = member.getMemberId();
        this.name = member.getName();
        this.email = member.getEmail();
        this.tokenResponse = tokenResponse;
    }
}
