package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.DeviceToken;
import com.unlam.verabackend.infrastructure.entity.DeviceTokenEntity;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.mapper.DeviceTokenMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeviceTokenRepositoryAdapterTest {

    private JpaDeviceTokenRepository jpaRepository;
    private DeviceTokenRepositoryAdapter adapter;
    private User user;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaDeviceTokenRepository.class);
        adapter = new DeviceTokenRepositoryAdapter(jpaRepository, new DeviceTokenMapper());
        user = new User();
        user.setEmail("test@unlam.edu.ar");
    }

    @Test
    void saveOrUpdate_WhenTokenDoesNotExist_CreatesActiveToken() {
        when(jpaRepository.findByToken("token-1")).thenReturn(Optional.empty());
        when(jpaRepository.save(any(DeviceTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeviceToken saved = adapter.saveOrUpdate(user, "token-1", "android");

        assertEquals(user, saved.getUser());
        assertEquals("token-1", saved.getToken());
        assertEquals("android", saved.getPlatform());
        assertTrue(saved.isActive());
    }

    @Test
    void saveOrUpdate_WhenTokenAlreadyExists_UpdatesInsteadOfDuplicating() {
        DeviceTokenEntity existing = DeviceTokenEntity.builder()
                .token("token-1")
                .platform("ios")
                .active(false)
                .build();
        when(jpaRepository.findByToken("token-1")).thenReturn(Optional.of(existing));
        when(jpaRepository.save(existing)).thenReturn(existing);

        DeviceToken saved = adapter.saveOrUpdate(user, "token-1", "android");

        assertSame(user, existing.getUser());
        assertEquals("android", existing.getPlatform());
        assertTrue(existing.isActive());
        assertEquals("token-1", saved.getToken());
        verify(jpaRepository).save(existing);
    }
}
