package com.jaeseok.groupStudy.user.infrastructure.persistence;

import com.jaeseok.groupStudy.user.domain.User;
import com.jaeseok.groupStudy.user.domain.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromDomain(user);
        return jpaUserRepository.save(entity).toDomain();
    }

    @Override
    public Optional<User> findById(Long userId) {
        return jpaUserRepository.findById(userId).map(UserEntity::toDomain);
    }

    @Override
    public boolean existByNickname(String nickname) {
        return jpaUserRepository.existsByUserInfoEntity_Nickname(nickname);
    }
}
