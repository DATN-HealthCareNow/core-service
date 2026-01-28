package com.healthcarenow.core.service;

import com.healthcarenow.core.dto.SessionDTO;
import com.healthcarenow.core.model.mongo.Session;
import com.healthcarenow.core.repository.mongo.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {
  private final SessionRepository sessionRepository;

  public List<SessionDTO> getActiveSessions(String userId) {
    // Find all non-revoked sessions (logic simplified as repo method not fully
    // defined in architecture for "findByUserIdAndRevokedFalse")
    // We will assume fetching all and filtering for now, or you would add the
    // method to repo.
    return sessionRepository.findAll().stream() // Ideally findByUserId
        .filter(s -> s.getUserId().equals(userId) && !s.isRevoked())
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  public void revokeSession(String sessionId) {
    sessionRepository.findById(sessionId).ifPresent(s -> {
      s.setRevoked(true);
      sessionRepository.save(s);
    });
  }

  private SessionDTO mapToDTO(Session session) {
    SessionDTO dto = new SessionDTO();
    dto.setId(session.getId());
    dto.setCreatedAt(session.getCreatedAt());
    if (session.getDeviceInfo() != null) {
      dto.setUserAgent(session.getDeviceInfo().getUserAgent());
      dto.setIpAddress(session.getDeviceInfo().getIpAddress());
    }
    return dto;
  }
}
