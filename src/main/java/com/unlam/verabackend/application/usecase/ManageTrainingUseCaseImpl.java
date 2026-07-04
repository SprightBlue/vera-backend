package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.port.in.ManageTrainingUseCase;
import com.unlam.verabackend.domain.port.out.TrainingRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.presentation.dto.TrainingProgressResponse;
import com.unlam.verabackend.presentation.dto.TrainingProgressResponse.DailyProgressPoint;
import com.unlam.verabackend.presentation.dto.TrainingProgressResponse.RecentSessionSummary;
import com.unlam.verabackend.presentation.dto.TrainingStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManageTrainingUseCaseImpl implements ManageTrainingUseCase {

    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;
    private final TrustContactRepository trustContactRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TrainingScenario> getAvailableScenarios(String userEmail) {
        return trainingRepository.findActiveScenarios();
    }

    @Override
    @Transactional(readOnly = true)
    public TrainingScenario getScenarioById(UUID scenarioId) {
        return trainingRepository.findScenarioById(scenarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Escenario no encontrado: " + scenarioId));
    }

    @Override
    @Transactional
    public TrainingSession submitAnswer(String userEmail, UUID scenarioId, UUID selectedOptionId) {
        User user = findUserByEmail(userEmail);
        TrainingScenario scenario = trainingRepository.findScenarioById(scenarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Escenario no encontrado: " + scenarioId));

        ScenarioOption selected = scenario.getOptions().stream()
                .filter(o -> o.getId().equals(selectedOptionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Opción no encontrada: " + selectedOptionId));

        TrainingSession session = TrainingSession.builder()
                .user(user)
                .scenario(scenario)
                .selectedOption(selected)
                .correct(selected.isCorrect())
                .completedAt(LocalDateTime.now())
                .build();

        return trainingRepository.saveSession(session);
    }

    @Override
    @Transactional
    public void assignScenario(String carerEmail, Long trustContactId, UUID scenarioId) {
        TrustContact tc = getAndValidateTrustContact(trustContactId, carerEmail);
        User protectedUser = tc.getProtectedUser();
        User carer = tc.getCarer();

        TrainingScenario scenario = trainingRepository.findScenarioById(scenarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Escenario no encontrado: " + scenarioId));

        TrainingSession session = TrainingSession.builder()
                .user(protectedUser)
                .scenario(scenario)
                .assignedBy(carer)
                .build();

        trainingRepository.saveSession(session);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainingProgressResponse getProgressForProtected(String carerEmail, Long trustContactId) {
        TrustContact tc = getAndValidateTrustContact(trustContactId, carerEmail);
        Long userId = tc.getProtectedUser().getId();

        List<TrainingSession> completed = trainingRepository.findCompletedSessionsByUserId(userId);
        TrainingStatsResponse stats = buildStats(completed);
        List<DailyProgressPoint> dailyProgress = buildDailyProgress(completed);
        List<RecentSessionSummary> recent = completed.stream()
                .limit(10)
                .map(s -> new RecentSessionSummary(
                        s.getId().toString(),
                        s.getScenario().getTitle(),
                        s.getScenario().getScenarioType().name(),
                        s.isCorrect(),
                        s.getCompletedAt() != null ? s.getCompletedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
                ))
                .toList();

        return new TrainingProgressResponse(stats, dailyProgress, recent);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainingStatsResponse getStatsForProtected(String carerEmail, Long trustContactId) {
        TrustContact tc = getAndValidateTrustContact(trustContactId, carerEmail);
        Long userId = tc.getProtectedUser().getId();
        List<TrainingSession> completed = trainingRepository.findCompletedSessionsByUserId(userId);
        return buildStats(completed);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrainingSession> getSessionsForProtected(String carerEmail, Long trustContactId, Pageable pageable) {
        TrustContact tc = getAndValidateTrustContact(trustContactId, carerEmail);
        return trainingRepository.findSessionsByUserId(tc.getProtectedUser().getId(), pageable);
    }

    private TrainingStatsResponse buildStats(List<TrainingSession> sessions) {
        long completed = sessions.size();
        long correct = sessions.stream().filter(TrainingSession::isCorrect).count();
        long incorrect = completed - correct;
        int rate = completed > 0 ? (int) Math.round((correct * 100.0) / completed) : 0;
        long detected = sessions.stream().filter(s -> s.isCorrect() && s.getScenario().isScam()).count();
        return new TrainingStatsResponse(completed, correct, incorrect, rate, detected);
    }

    private List<DailyProgressPoint> buildDailyProgress(List<TrainingSession> sessions) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        Map<String, List<TrainingSession>> byDay = sessions.stream()
                .filter(s -> s.getCompletedAt() != null)
                .collect(Collectors.groupingBy(s -> s.getCompletedAt().format(fmt)));

        return byDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    List<TrainingSession> daySessions = entry.getValue();
                    long correct = daySessions.stream().filter(TrainingSession::isCorrect).count();
                    int rate = (int) Math.round((correct * 100.0) / daySessions.size());
                    return new DailyProgressPoint(entry.getKey(), rate, daySessions.size());
                })
                .toList();
    }

    private TrustContact getAndValidateTrustContact(Long trustContactId, String carerEmail) {
        TrustContact tc = trustContactRepository.findById(trustContactId)
                .orElseThrow(() -> new ResourceNotFoundException("Relación no encontrada: " + trustContactId));

        if (!tc.getCarer().getEmail().equals(carerEmail)) {
            throw new AccessDeniedException("No tenés permisos para acceder a estos datos.");
        }

        return tc;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
    }
}