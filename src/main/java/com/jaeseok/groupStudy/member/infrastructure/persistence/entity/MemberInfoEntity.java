package com.jaeseok.groupStudy.member.infrastructure.persistence.entity;

import com.jaeseok.groupStudy.member.domain.vo.Email;
import com.jaeseok.groupStudy.member.domain.vo.Nickname;
import com.jaeseok.groupStudy.member.domain.vo.Password;
import com.jaeseok.groupStudy.member.domain.vo.MemberInfo;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class MemberInfoEntity {
    private String email;
    private String nickname;
    private String password;

    public static MemberInfoEntity fromDomain(MemberInfo info) {
        return new MemberInfoEntity(
                info.email().value(),
                info.nickname().value(),
                info.password().encodedValue()
        );
    }

    public MemberInfo toDomain() {
        return new MemberInfo(
                new Email(this.email),
                new Nickname(this.nickname),
                new Password(this.password)
        );
    }

}
