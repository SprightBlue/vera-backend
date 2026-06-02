package com.unlam.verabackend.application.usecase;

import org.springframework.stereotype.Service;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.domain.repository.UserRepository;

@Service
public class RegisterUserUseCase {

    private final UserRepository userRepository;

    public RegisterUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(User user) {
        return userRepository.save(user);
    }
}
