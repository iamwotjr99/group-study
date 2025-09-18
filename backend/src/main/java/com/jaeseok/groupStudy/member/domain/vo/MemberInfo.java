package com.jaeseok.groupStudy.member.domain.vo;


public record MemberInfo(Email email, Nickname nickname, Password password) {

    public static MemberInfo of(String nickname, String email, String password) {
        return new MemberInfo(new Email(email), new Nickname(nickname), new Password(password));
    }

    public MemberInfo withNickname(Nickname newNickname) {
        return new MemberInfo(this.email, newNickname, this.password);
    }
}
