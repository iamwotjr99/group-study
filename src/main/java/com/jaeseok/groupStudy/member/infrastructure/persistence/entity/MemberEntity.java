package com.jaeseok.groupStudy.member.infrastructure.persistence.entity;

import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.common.BaseTimeEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class MemberEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Embedded
    MemberInfoEntity memberInfoEntity;

    public static MemberEntity fromDomain(Member domain) {
        return new MemberEntity(domain.getId(), MemberInfoEntity.fromDomain(domain.getMemberInfo()));
    }

    public Member toDomain() {
        return Member.from(this.id, this.memberInfoEntity.toDomain());
    }
}
