package kdt.web_ide.members.entity.repository;

import kdt.web_ide.members.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    boolean existsByKeyUserId(String userId);

    void deleteByKeyUserId(String userId);
}
