package com.jaeseok.groupStudy.member.infrastructure.persistence;

import com.jaeseok.groupStudy.member.infrastructure.persistence.entity.MemberEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMemberRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findByMemberInfoEntity_Email(String email);

    boolean existsByMemberInfoEntity_Nickname(String nickname);

    boolean existsByMemberInfoEntity_Email(String email);
}
