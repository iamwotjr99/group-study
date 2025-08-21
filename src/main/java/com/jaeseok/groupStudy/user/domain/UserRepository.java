package com.jaeseok.groupStudy.user.domain;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long userId);
    Optional<User> findByEmail(String email);
    boolean existByNickname(String nickname);
    boolean existByEmail(String email);
}
