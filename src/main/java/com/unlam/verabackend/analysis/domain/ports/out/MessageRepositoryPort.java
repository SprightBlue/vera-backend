package com.unlam.verabackend.analysis.domain.ports.out;

import com.unlam.verabackend.analysis.domain.model.Message;

public interface MessageRepositoryPort {
    void save(Message message);
}
