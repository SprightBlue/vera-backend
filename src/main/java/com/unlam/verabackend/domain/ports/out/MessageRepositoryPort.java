package com.unlam.verabackend.domain.ports.out;

import com.unlam.verabackend.domain.model.Message;

public interface MessageRepositoryPort {
    void save(Message message);
}
