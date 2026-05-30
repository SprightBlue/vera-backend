package com.unlam.verabackend.domain.repository;

import com.unlam.verabackend.domain.model.User;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(String email);
}