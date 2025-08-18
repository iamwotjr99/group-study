package com.jaeseok.groupStudy.user.infrastructure.persistence.entity;

import com.jaeseok.groupStudy.user.domain.User;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Embedded
    UserInfoEntity userInfoEntity;

    public static UserEntity fromDomain(User domain) {
        return new UserEntity(domain.getId(), UserInfoEntity.fromDomain(domain.getUserInfo()));
    }

    public User toDomain() {
        return User.from(this.id, this.userInfoEntity.toDomain());
    }
}
