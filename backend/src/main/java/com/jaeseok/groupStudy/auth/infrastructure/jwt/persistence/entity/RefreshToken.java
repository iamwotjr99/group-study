package com.jaeseok.groupStudy.auth.infrastructure.jwt.persistence.entity;

import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.infrastructure.persistence.entity.MemberEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private MemberEntity memberEntity;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    public RefreshToken(Member member, String token, Instant expiryDate) {
        this.memberEntity = MemberEntity.fromDomain(member);
        this.token = token;
        this.expiryDate = expiryDate;
    }

    // 토큰 갱신 시, 새 토큰으로 업데이트
    public void updateToken(String newToken, Instant newExpiryDate) {
        this.token = newToken;
        this.expiryDate = newExpiryDate;
    }

    // 만료 여부 확인
    public boolean isExpired(Instant now) {
        return now.isAfter(this.expiryDate);
    }
}
