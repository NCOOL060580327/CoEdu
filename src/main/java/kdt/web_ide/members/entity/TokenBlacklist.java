package kdt.web_ide.members.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "token_blacklist")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenBlacklist {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "token_blacklist_id")
  private Long id;

  @Column(name = "refresh_token", nullable = false, unique = true)
  private String refreshToken;

  @Column(name = "expired_at", nullable = false)
  private LocalDateTime expiredAt;

  public static TokenBlacklist of(String refreshToken, LocalDateTime expiredAt) {
    return TokenBlacklist.builder().refreshToken(refreshToken).expiredAt(expiredAt).build();
  }
}
