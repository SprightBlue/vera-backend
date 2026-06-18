package com.unlam.verabackend.application.service;

import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.JpaAlertsRepository;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.provider.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyReportSchedulerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private TrustContactRepository trustContactRepository;

    @Mock
    private JpaAlertsRepository alertsRepository;

    @InjectMocks
    private WeeklyReportScheduler scheduler;

    @Test
    void shouldSendEmailWhenContactsHaveAlerts() {
        TrustContact contact = new TrustContact();
        contact.setId(1L);

        User carer = new User();
        carer.setEmail("carer@test.com");
        carer.setFullName("Protector Test");

        User protectedUser = new User();
        protectedUser.setFullName("Protegido Test");

        contact.setCarer(carer);
        contact.setProtectedUser(protectedUser);

        when(trustContactRepository.findByReceiveAlertSummariesTrue()).thenReturn(List.of(contact));
        when(alertsRepository.countByTrustContactIdAndRiskLevelAndCreatedAtAfter(eq(1L), eq("ALTO"), any(LocalDateTime.class))).thenReturn(2L);
        when(alertsRepository.countByTrustContactIdAndRiskLevelAndCreatedAtAfter(eq(1L), eq("MEDIO"), any(LocalDateTime.class))).thenReturn(0L);
        when(alertsRepository.findTop3ByTrustContactIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(1L), any(LocalDateTime.class))).thenReturn(new ArrayList<>());

        scheduler.generarYEnviarReportesSemanales();

        verify(emailService, times(1)).enviarEmailResumenSemanal(
                eq("carer@test.com"),
                eq("Protector Test"),
                eq("Protegido Test"),
                eq(2L),
                eq(0L),
                anyList()
        );
    }

    @Test
    void shouldNotSendEmailWhenNoAlertsFound() {
        TrustContact contact = new TrustContact();
        contact.setId(1L);

        when(trustContactRepository.findByReceiveAlertSummariesTrue()).thenReturn(List.of(contact));
        when(alertsRepository.countByTrustContactIdAndRiskLevelAndCreatedAtAfter(eq(1L), eq("ALTO"), any(LocalDateTime.class))).thenReturn(0L);
        when(alertsRepository.countByTrustContactIdAndRiskLevelAndCreatedAtAfter(eq(1L), eq("MEDIO"), any(LocalDateTime.class))).thenReturn(0L);
        when(alertsRepository.findTop3ByTrustContactIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(1L), any(LocalDateTime.class))).thenReturn(new ArrayList<>());

        scheduler.generarYEnviarReportesSemanales();

        verify(emailService, never()).enviarEmailResumenSemanal(
                anyString(),
                anyString(),
                anyString(),
                anyLong(),
                anyLong(),
                anyList()
        );
    }
}