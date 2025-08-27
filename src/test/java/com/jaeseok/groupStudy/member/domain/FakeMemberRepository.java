package com.jaeseok.groupStudy.member.domain;

import java.util.HashMap;
import java.util.Optional;


public class FakeMemberRepository implements MemberRepository{

    private final HashMap<Long, Member> store;
    private long sequence = 0L;

    public FakeMemberRepository() {
        this.store = new HashMap<>();
    }


    @Override
    public Member save(Member member) {
        if (member.getId() == null || member.getId() == 0) {
            sequence++;
            Member newMember = Member.from(sequence, member.getMemberInfo());
            store.put(sequence, newMember);
            return newMember;
        }

        store.put(member.getId(), member);
        return member;
    }

    @Override
    public Optional<Member> findById(Long userId) {
        return store.values().stream()
                .filter(m -> m.getId().equals(userId))
                .findFirst();
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return store.values().stream()
                .filter(m -> m.getUserInfoEmail().equals(email))
                .findFirst();
    }

    @Override
    public boolean existByNickname(String nickname) {
        return store.values().stream()
                .anyMatch(m -> m.getUserInfoNickname().equals(nickname));
    }

    @Override
    public boolean existByEmail(String email) {
        return store.values().stream()
                .anyMatch(m -> m.getUserInfoEmail().equals(email));
    }
}
