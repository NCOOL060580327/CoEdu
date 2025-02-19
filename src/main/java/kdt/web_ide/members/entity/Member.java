package kdt.web_ide.members.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import kdt.web_ide.BaseTimeEntity;
import lombok.*;

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

  @Column(name = "kakao_id")
  private Long kakaoId;

  @Setter
  @Column(nullable = false, unique = true)
  private String nickName;

  @Setter
  @Column(name = "profile_img")
  private String profileImage;

  @Setter
  @Column(name = "refresh_token")
  private String refreshToken;

  @Setter
  @Column(name = "kakao_refresh_token")
  private String kakaoRefreshToken;

  @Column(name = "identification_code")
  private String identificationCode;

  private String email;

  private String password;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "member_roles", joinColumns = @JoinColumn(name = "member_id"))
  @Enumerated(EnumType.STRING)
  private List<RoleType> roles = new ArrayList<>();
}
