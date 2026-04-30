package com.healthcarenow.core.service;

import com.healthcarenow.core.dto.UserAdminResponse;
import com.healthcarenow.core.exception.UnauthorizedException;
import com.healthcarenow.core.exception.ResourceNotFoundException;
import com.healthcarenow.core.model.mongo.PatientProfile;
import com.healthcarenow.core.model.mongo.Role;
import com.healthcarenow.core.model.mongo.User;
import com.healthcarenow.core.repository.mongo.PatientProfileRepository;
import com.healthcarenow.core.repository.mongo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {
  
  private final UserRepository userRepository;
  private final PatientProfileRepository patientProfileRepository;

  private void checkAdmin(String adminId) {
    User admin = userRepository.findById(adminId)
        .orElseThrow(() -> new UnauthorizedException("User not found"));
    if (admin.getRole() != Role.ADMIN) {
      throw new UnauthorizedException("Only admins can perform this action");
    }
  }

  public List<UserAdminResponse> getAllUsers(String adminId) {
    checkAdmin(adminId);
    
    return userRepository.findAll().stream().map(user -> {
      PatientProfile profile = patientProfileRepository.findByUserId(user.getId()).orElse(null);
      return UserAdminResponse.builder()
          .id(user.getId())
          .email(user.getEmail())
          .fullName(profile != null ? profile.getFullName() : "")
          .role(user.getRole() != null ? user.getRole().name() : "USER")
          .status(user.getStatus())
          .dateOfBirth(profile != null ? profile.getDateOfBirth() : null)
          .heightCm(
              profile != null && profile.getHeightCm() != null
                  ? profile.getHeightCm().doubleValue()
                  : null
          )

          .weightKg(
              profile != null && profile.getWeightKg() != null
                  ? profile.getWeightKg().doubleValue()
                  : null
          )
          .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
          .createdAt(user.getCreatedAt())
          .build();
    }).collect(Collectors.toList());
  }

  public UserAdminResponse changeRole(String adminId, String userId, String roleStr) {
    checkAdmin(adminId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    
    try {
      Role role = Role.valueOf(roleStr.toUpperCase());
      user.setRole(role);
      userRepository.save(user);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Invalid role");
    }

    PatientProfile profile = patientProfileRepository.findByUserId(user.getId()).orElse(null);
    return UserAdminResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .fullName(profile != null ? profile.getFullName() : "")
        .role(user.getRole().name())
        .status(user.getStatus())
        .dateOfBirth(profile != null ? profile.getDateOfBirth() : null)
        .heightCm(profile != null && profile.getHeightCm() != null 
            ? profile.getHeightCm().doubleValue() 
            : null)

        .weightKg(profile != null && profile.getWeightKg() != null 
            ? profile.getWeightKg().doubleValue() 
            : null)
        .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
        .createdAt(user.getCreatedAt())
        .build();
  }

  public UserAdminResponse changeStatus(String adminId, String userId, String status) {
    checkAdmin(adminId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    
    String upperStatus = status != null ? status.toUpperCase() : "ACTIVE";
    if (!List.of("ACTIVE", "SUSPENDED", "DELETED").contains(upperStatus)) {
        throw new RuntimeException("Invalid status");
    }
    
    user.setStatus(upperStatus);
    userRepository.save(user);

    PatientProfile profile = patientProfileRepository.findByUserId(user.getId()).orElse(null);
    return UserAdminResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .fullName(profile != null ? profile.getFullName() : "")
        .role(user.getRole().name())
        .status(user.getStatus())
        .dateOfBirth(profile != null ? profile.getDateOfBirth() : null)
        .heightCm(profile != null && profile.getHeightCm() != null 
            ? profile.getHeightCm().doubleValue() 
            : null)
        .weightKg(profile != null && profile.getWeightKg() != null 
            ? profile.getWeightKg().doubleValue() 
            : null)
        .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
        .createdAt(user.getCreatedAt())
        .build();
  }
}
