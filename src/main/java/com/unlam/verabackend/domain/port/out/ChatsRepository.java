package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.Chats;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatsRepository {
    Chats save(Chats chat);
    Optional<Chats> findById(UUID id);
    List<Chats> findByUserEmail(String email);
    Optional<Chats> findByAnalysisId(UUID analysisId);
    Optional<Chats> findByAlertId(UUID alertId);
    void deleteById(UUID id);
}