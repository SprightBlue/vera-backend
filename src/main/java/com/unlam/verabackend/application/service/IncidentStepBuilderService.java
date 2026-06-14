package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IncidentStepBuilderService {

    public List<IncidentStep> buildSteps(IncidentActionType actionType,
                                         List<SharedDataType> sharedDataTypes) {
        return switch (actionType) {
            case SHARED_PERSONAL_OR_BANKING_DATA -> buildForSharedData(sharedDataTypes);
            case TRANSFERRED_MONEY -> buildForTransferredMoney();
            case CLICKED_SUSPICIOUS_LINK -> buildForClickedLink();
            case DOWNLOADED_FILE_OR_APP -> buildForDownload();
            case OTHER_NOT_SURE -> buildForOther();
        };
    }

    private List<IncidentStep> buildForSharedData(List<SharedDataType> dataTypes) {
        List<IncidentStepKey> keys = new ArrayList<>();

        if (dataTypes.contains(SharedDataType.BANKING_DATA)) {
            keys.add(IncidentStepKey.BLOCK_CARD);
            keys.add(IncidentStepKey.CHANGE_CARD_PIN);
        }

        if (dataTypes.contains(SharedDataType.CREDENTIALS) || dataTypes.contains(SharedDataType.BANKING_DATA)) {
            keys.add(IncidentStepKey.CHANGE_HOMEBANKING_PASSWORD);
        }

        if (dataTypes.contains(SharedDataType.CREDENTIALS)) {
            keys.add(IncidentStepKey.CHANGE_EMAIL_PASSWORD);
        }

        if (dataTypes.contains(SharedDataType.DNI)) {
            keys.add(IncidentStepKey.LOCK_DNI);
        }

        keys.add(IncidentStepKey.REVIEW_MOVEMENTS);
        keys.add(IncidentStepKey.FILE_REPORT_BANK);
        keys.add(IncidentStepKey.NOTIFY_CONTACTS);
        keys.add(IncidentStepKey.MONITOR_ACCOUNTS);

        return toSteps(keys, List.of(IncidentStepKey.REVIEW_MOVEMENTS, IncidentStepKey.FILE_REPORT_BANK));
    }

    private List<IncidentStep> buildForTransferredMoney() {
        return toSteps(List.of(
                IncidentStepKey.CONTACT_BANK_URGENTLY,
                IncidentStepKey.FILE_REPORT_BANK,
                IncidentStepKey.FILE_REPORT_POLICE,
                IncidentStepKey.NOTIFY_CONTACTS,
                IncidentStepKey.MONITOR_ACCOUNTS
        ), List.of(
                IncidentStepKey.CONTACT_BANK_URGENTLY,
                IncidentStepKey.FILE_REPORT_BANK,
                IncidentStepKey.FILE_REPORT_POLICE
        ));
    }

    private List<IncidentStep> buildForClickedLink() {
        return toSteps(List.of(
                IncidentStepKey.CHANGE_HOMEBANKING_PASSWORD,
                IncidentStepKey.CHANGE_EMAIL_PASSWORD,
                IncidentStepKey.SCAN_DEVICE,
                IncidentStepKey.FILE_REPORT_POLICE,
                IncidentStepKey.NOTIFY_CONTACTS
        ), List.of(
                IncidentStepKey.CHANGE_HOMEBANKING_PASSWORD,
                IncidentStepKey.CHANGE_EMAIL_PASSWORD,
                IncidentStepKey.SCAN_DEVICE,
                IncidentStepKey.FILE_REPORT_POLICE
        ));
    }

    private List<IncidentStep> buildForDownload() {
        return toSteps(List.of(
                IncidentStepKey.SCAN_DEVICE,
                IncidentStepKey.CHANGE_HOMEBANKING_PASSWORD,
                IncidentStepKey.REVIEW_MOVEMENTS,
                IncidentStepKey.FILE_REPORT_POLICE,
                IncidentStepKey.NOTIFY_CONTACTS
        ), List.of(
                IncidentStepKey.SCAN_DEVICE,
                IncidentStepKey.CHANGE_HOMEBANKING_PASSWORD,
                IncidentStepKey.REVIEW_MOVEMENTS
        ));
    }

    private List<IncidentStep> buildForOther() {
        return toSteps(List.of(
                IncidentStepKey.CHANGE_HOMEBANKING_PASSWORD,
                IncidentStepKey.REVIEW_MOVEMENTS,
                IncidentStepKey.NOTIFY_CONTACTS,
                IncidentStepKey.MONITOR_ACCOUNTS
        ), List.of(
                IncidentStepKey.CHANGE_HOMEBANKING_PASSWORD,
                IncidentStepKey.REVIEW_MOVEMENTS
        ));
    }

    private List<IncidentStep> toSteps(List<IncidentStepKey> keys,
                                       List<IncidentStepKey> priorityKeys) {
        List<IncidentStep> steps = new ArrayList<>();

        for (int i = 0; i < keys.size(); i++) {
            IncidentStepKey key = keys.get(i);
            steps.add(step(key, i + 1, priorityKeys.contains(key)));
        }

        return steps;
    }

    private IncidentStep step(IncidentStepKey key, int order, boolean priority) {
        return IncidentStep.builder()
                .stepKey(key)
                .title(key.getTitle())
                .description(key.getDescription())
                .stepOrder(order)
                .priority(priority)
                .completed(false)
                .build();
    }
}