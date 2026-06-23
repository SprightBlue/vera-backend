package com.unlam.verabackend.application.service;

import com.unlam.verabackend.infrastructure.entity.AlertsEntity;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.repository.JpaAlertsRepository;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.provider.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WeeklyReportScheduler {

    @Autowired
    private EmailService emailService;

    @Autowired
    private TrustContactRepository trustContactRepository;

    @Autowired
    private JpaAlertsRepository alertsRepository;

    // Ejecuta todos los LUNES a las 8:00 AM (Para probar ahora podés poner "0 * * * * ?" para que corra cada minuto)
    @Scheduled(cron = "0 0 8 ? * MON")
    public void generarYEnviarReportesSemanales() {
        System.out.println("⏰ Iniciando la generación de reportes semanales...");

        
        List<TrustContact> contactosInteresados = trustContactRepository.findByReceiveAlertSummariesTrue();
        
        
        LocalDateTime haceUnaSemana = LocalDateTime.now().minusDays(7);

        
        for (TrustContact contacto : contactosInteresados) {
            Long contactId = contacto.getId();

            
            long alertasAltas = alertsRepository.countByTrustContactIdAndRiskLevelAndCreatedAtAfter(contactId, "ALTO", haceUnaSemana);
            long alertasMedias = alertsRepository.countByTrustContactIdAndRiskLevelAndCreatedAtAfter(contactId, "MEDIO", haceUnaSemana);

            
            List<AlertsEntity> top3Alertas = alertsRepository.findTop3ByTrustContactIdAndCreatedAtAfterOrderByCreatedAtDesc(contactId, haceUnaSemana);

            
            if (alertasAltas > 0 || alertasMedias > 0 || !top3Alertas.isEmpty()) {
                
                String emailProtector = contacto.getCarer().getEmail();
                String nombreProtector = contacto.getCarer().getFullName();
                String nombreProtegido = contacto.getProtectedUser().getFullName();

                
                emailService.enviarEmailResumenSemanal(
                        emailProtector,
                        nombreProtector,
                        nombreProtegido,
                        alertasAltas,
                        alertasMedias,
                        top3Alertas
                );
            }
        }
        
        System.out.println("✅ Proceso de reportes semanales finalizado.");
    }
}