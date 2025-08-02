package com.jaeseok.groupStudy.user.domain;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long userId);
    boolean existByNickname(String nickname);
}
