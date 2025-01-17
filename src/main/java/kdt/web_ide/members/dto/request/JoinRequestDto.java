package kdt.web_ide.members.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import kdt.web_ide.members.entity.Member;
import kdt.web_ide.members.entity.RoleType;
import lombok.*;

import java.util.Collections;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class JoinRequestDto {
    @Email
    private final String email;
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,10}$", message = "이름은 최소 2자 이상, 10자 이하이며, 영문, 숫자, 한글만 입력하세요.")
    private final String name;


    @Pattern(regexp = "(?=.*[a-zA-Z])(?=.*[0-9])^[a-zA-Z0-9~!@#$%^&*()+|=]{8,15}$", message = "비밀번호는 최소 8자 이상, 15자 이하이며, 영문과 숫자, 특수문자만 입력하세요.")
    private final String password;

    @Pattern(regexp = "(?=.*[a-zA-Z])(?=.*[0-9])^[a-zA-Z0-9~!@#$%^&*()+|=]{8,15}$", message = "비밀번호를 확인해주세요.")
    private final String password2;

    @Builder
    public JoinRequestDto(String email,String name, String password, String password2){
        this.email = email;
        this.name = name;
        this.password = password;
        this.password2 = password2;
    }

    public Member toEntity(RoleType role, String encodedPassword){
        return Member.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .roles(Collections.singletonList(role))
                .build();
    }
}


