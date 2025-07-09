package com.jaeseok.groupStudy.user.domain.vo;

public record UserInfo(Email email, Nickname nickname, Password password) {

    public UserInfo withNickname(Nickname newNickname) {
        return new UserInfo(this.email, newNickname, this.password);
    }
}
