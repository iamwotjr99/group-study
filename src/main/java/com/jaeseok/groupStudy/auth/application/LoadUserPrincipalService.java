package com.jaeseok.groupStudy.auth.application;

import org.springframework.security.core.userdetails.UserDetails;

public interface LoadUserPrincipalService {
    UserDetails loadUserById(Long id);

}
