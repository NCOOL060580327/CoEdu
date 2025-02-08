package kdt.web_ide.members.entity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kdt.web_ide.members.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByRefreshToken(String refreshToken);

  boolean existsByKeyUserId(String userId);

  void deleteByKeyUserId(String userId);
}
