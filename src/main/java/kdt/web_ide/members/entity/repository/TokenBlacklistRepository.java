package kdt.web_ide.members.entity.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import kdt.web_ide.members.entity.TokenBlacklist;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

  boolean existsByRefreshToken(String refreshToken);

  @Modifying
  @Transactional
  @Query("""
    DELETE
    FROM TokenBlacklist t
    WHERE t.expiredAt <= :now
    """)
  int deleteExpiredTokens(@Param("now") LocalDateTime now);

  @Query("""
    SELECT COUNT(t)
    FROM TokenBlacklist t
    WHERE t.expiredAt <= :now
    """)
  long countExpiredTokens(@Param("now") LocalDateTime now);
}
