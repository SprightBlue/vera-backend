package com.unlam.verabackend.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.unlam.verabackend.infrastructure.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chats {
    private UUID id;
    private User user;
    private Analysis analysis;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}