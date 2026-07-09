package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.IncidentActionType;
import com.unlam.verabackend.domain.model.IncidentStep;
import com.unlam.verabackend.domain.model.IncidentStepKey;
import com.unlam.verabackend.domain.model.SharedDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IncidentStepBuilderServiceTest {

    private IncidentStepBuilderService builderService;

    @BeforeEach
    void setUp() {
        builderService = new IncidentStepBuilderService();
    }

    @Test
    void deberiaConstruirPasosParaDineroTransferido() {
        List<IncidentStep> steps = builderService.buildSteps(IncidentActionType.TRANSFERRED_MONEY, List.of());

        assertNotNull(steps);
        assertEquals(5, steps.size()); 

        assertEquals(IncidentStepKey.CONTACT_BANK_URGENTLY, steps.get(0).getStepKey());
        assertTrue(steps.get(0).isPriority());
    }

    @Test
    void deberiaConstruirPasosParaLinkSospechoso() {
        List<IncidentStep> steps = builderService.buildSteps(IncidentActionType.CLICKED_SUSPICIOUS_LINK, List.of());

        assertEquals(5, steps.size());
        assertEquals(IncidentStepKey.CHANGE_HOMEBANKING_PASSWORD, steps.get(0).getStepKey());
    }

    @Test
    void deberiaConstruirPasosParaAppDescargada() {
        List<IncidentStep> steps = builderService.buildSteps(IncidentActionType.DOWNLOADED_FILE_OR_APP, List.of());

        assertEquals(5, steps.size());
        assertEquals(IncidentStepKey.SCAN_DEVICE, steps.get(0).getStepKey());
    }

    @Test
    void deberiaConstruirPasosParaOtrosCasos() {
        List<IncidentStep> steps = builderService.buildSteps(IncidentActionType.OTHER_NOT_SURE, List.of());

        assertEquals(4, steps.size());
        assertEquals(IncidentStepKey.CHANGE_HOMEBANKING_PASSWORD, steps.get(0).getStepKey());
    }

    @Test
    void deberiaConstruirPasosParaDatosCompartidosBancariosYDNI() {
        List<SharedDataType> datosExpuestos = List.of(SharedDataType.BANKING_DATA, SharedDataType.DNI);

        List<IncidentStep> steps = builderService.buildSteps(IncidentActionType.SHARED_PERSONAL_OR_BANKING_DATA, datosExpuestos);

        List<IncidentStepKey> keysResultantes = steps.stream().map(IncidentStep::getStepKey).toList();
        
        assertTrue(keysResultantes.contains(IncidentStepKey.BLOCK_CARD));
        assertTrue(keysResultantes.contains(IncidentStepKey.CHANGE_CARD_PIN));
        assertTrue(keysResultantes.contains(IncidentStepKey.LOCK_DNI));
        assertTrue(keysResultantes.contains(IncidentStepKey.CHANGE_HOMEBANKING_PASSWORD));
        
        assertFalse(keysResultantes.contains(IncidentStepKey.CHANGE_EMAIL_PASSWORD));
    }

    @Test
    void deberiaConstruirPasosParaDatosCompartidosCredenciales() {
        List<SharedDataType> datosExpuestos = List.of(SharedDataType.CREDENTIALS);

        List<IncidentStep> steps = builderService.buildSteps(IncidentActionType.SHARED_PERSONAL_OR_BANKING_DATA, datosExpuestos);

        List<IncidentStepKey> keysResultantes = steps.stream().map(IncidentStep::getStepKey).toList();
        
        assertTrue(keysResultantes.contains(IncidentStepKey.CHANGE_EMAIL_PASSWORD));
        assertTrue(keysResultantes.contains(IncidentStepKey.CHANGE_HOMEBANKING_PASSWORD));
        
        assertFalse(keysResultantes.contains(IncidentStepKey.LOCK_DNI));
        assertFalse(keysResultantes.contains(IncidentStepKey.BLOCK_CARD));
    }
}