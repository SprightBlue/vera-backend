package com.unlam.verabackend.domain.port.in;

public interface CheckProtectedUserStatusUseCase {
    boolean execute(String email);
}
