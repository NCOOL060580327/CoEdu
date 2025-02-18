package kdt.web_ide.members.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kdt.web_ide.members.entity.TokenBlacklist;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

  boolean existsByRefreshToken(String refreshToken);
}
