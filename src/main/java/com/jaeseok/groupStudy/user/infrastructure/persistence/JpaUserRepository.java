package com.jaeseok.groupStudy.user.infrastructure.persistence;

import com.jaeseok.groupStudy.user.infrastructure.persistence.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUserInfoEntity_Email(String email);

    boolean existsByUserInfoEntity_Nickname(String nickname);

    boolean existsByUserInfoEntity_Email(String email);
}
