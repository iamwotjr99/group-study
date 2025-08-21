package com.jaeseok.groupStudy.user.domain.vo;


public record UserInfo(Email email, Nickname nickname, Password password) {

    public static UserInfo of(String nickname, String email, String password) {
        return new UserInfo(new Email(email), new Nickname(nickname), new Password(password));
    }

    public UserInfo withNickname(Nickname newNickname) {
        return new UserInfo(this.email, newNickname, this.password);
    }
}
