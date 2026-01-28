package com.healthcarenow.core.service;

import com.healthcarenow.core.dto.SleepAnalysisDTO;
import com.healthcarenow.core.dto.SleepSyncRequest;
import com.healthcarenow.core.model.mongo.SleepSession;
import com.healthcarenow.core.repository.mongo.SleepSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SleepSessionService {
  private final SleepSessionRepository sleepSessionRepository;

  public SleepSession syncSleep(String userId, SleepSyncRequest request) {
    SleepSession session = new SleepSession();
    session.setUserId(userId);
    session.setBedtimeStart(request.getBedtimeStart());
    session.setWakeupEnd(request.getWakeupEnd());
    session.setStages(request.getStages());
    session.setHeartRate(request.getHeartRate());
    session.setSource(request.getSource());

    double hours = Duration.between(request.getBedtimeStart(), request.getWakeupEnd()).toMinutes() / 60.0;
    session.setDurationHours(hours);

    // Efficiency dummy calculation
    session.setEfficiencyPercent(85);

    return sleepSessionRepository.save(session);
  }

  public SleepAnalysisDTO analyzeSleep(String userId) {
    List<SleepSession> sessions = sleepSessionRepository.findByUserId(userId);
    if (sessions.isEmpty())
      return SleepAnalysisDTO.builder().build();

    double avgDuration = sessions.stream().mapToDouble(SleepSession::getDurationHours).average().orElse(0);
    int avgEfficiency = (int) sessions.stream().mapToInt(SleepSession::getEfficiencyPercent).average().orElse(0);

    return SleepAnalysisDTO.builder()
        .avgDurationHours(avgDuration)
        .avgEfficiency(avgEfficiency)
        .qualityAssessment(avgEfficiency > 80 ? "Good" : "Needs Improvement")
        .build();
  }
}
