package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.User;
import com.unlam.verabackend.domain.repository.UserRepository;

public class RegisterUserUseCase {

    private final UserRepository userRepository;

    public RegisterUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(User user) {
        return userRepository.save(user);
    }
}
