package com.jaeseok.groupStudy.user.infrastructure.persistence.entity;

import com.jaeseok.groupStudy.user.domain.vo.Email;
import com.jaeseok.groupStudy.user.domain.vo.Nickname;
import com.jaeseok.groupStudy.user.domain.vo.Password;
import com.jaeseok.groupStudy.user.domain.vo.UserInfo;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInfoEntity {
    private String email;
    private String nickname;
    private String password;

    public static UserInfoEntity fromDomain(UserInfo info) {
        return new UserInfoEntity(
                info.email().value(),
                info.nickname().value(),
                info.password().encodedValue()
        );
    }

    public UserInfo toDomain() {
        return new UserInfo(
                new Email(this.email),
                new Nickname(this.nickname),
                new Password(this.password)
        );
    }

}
