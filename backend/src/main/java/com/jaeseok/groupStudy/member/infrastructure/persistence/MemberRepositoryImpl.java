package com.jaeseok.groupStudy.member.infrastructure.persistence;

import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import com.jaeseok.groupStudy.member.infrastructure.persistence.entity.MemberEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final JpaMemberRepository jpaMemberRepository;

    @Override
    public Member save(Member member) {
        MemberEntity entity = MemberEntity.fromDomain(member);
        return jpaMemberRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Member> findById(Long userId) {
        return jpaMemberRepository.findById(userId).map(MemberEntity::toDomain);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return jpaMemberRepository.findByMemberInfoEntity_Email(email).map(MemberEntity::toDomain);
    }

    @Override
    public boolean existByNickname(String nickname) {
        return jpaMemberRepository.existsByMemberInfoEntity_Nickname(nickname);
    }

    @Override
    public boolean existByEmail(String email) {
        return jpaMemberRepository.existsByMemberInfoEntity_Email(email);
    }
}
