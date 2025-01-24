package kdt.web_ide.members.entity;

import jakarta.persistence.*;
import kdt.web_ide.BaseTimeEntity;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@Builder
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, unique = true)
    private String nickName;
    @Column(nullable = false, unique = true)
    private String loginId;
    private String password;

    @Column(name = "profile_img")
    private String profileImage;

    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "member_roles", joinColumns = @JoinColumn(name = "member_id"))
    @Enumerated(EnumType.STRING)
    private List<RoleType> roles = new ArrayList<>();

    public void updateImage(String profileImage){
        this.profileImage = profileImage;
    }

    public void updateNickName(String nickName) { this.nickName = nickName; }

    public void updateLoginId(String loginId){
        this.loginId = loginId;
    }

}
