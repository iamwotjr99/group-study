package com.jaeseok.groupStudy.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByUserInfoEntity_Nickname(String nickname);
}
