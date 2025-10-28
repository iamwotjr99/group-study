package com.jaeseok.groupStudy.auth.infrastructure.jwt.persistence;

import com.jaeseok.groupStudy.auth.infrastructure.jwt.persistence.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // User ID로 토큰 찾기
    Optional<RefreshToken> findByMemberEntity_Id(Long memberEntityId);

    // 토큰 값으로 찾기 (재발급 시 검증용)
    Optional<RefreshToken> findByToken(String token);

    // 로그아웃 시 User ID로 삭제
    void deleteByMemberEntity_Id(Long memberEntityId);
}
