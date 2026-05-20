package com.healthcarenow.core.service;

import org.springframework.stereotype.Service;

import com.healthcarenow.core.dto.UpdateProfileRequest;
import com.healthcarenow.core.dto.UserProfileResponse;
import com.healthcarenow.core.dto.UserContactResponse;
import com.healthcarenow.core.exception.ResourceNotFoundException;
import com.healthcarenow.core.model.mongo.PatientProfile;
import com.healthcarenow.core.model.mongo.User;
import com.healthcarenow.core.repository.mongo.PatientProfileRepository;
import com.healthcarenow.core.repository.mongo.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final PatientProfileRepository patientProfileRepository;
  private final S3Service s3Service;
  private final WaterIntakeService waterIntakeService;

  public UserProfileResponse getProfile(String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    PatientProfile profile = patientProfileRepository.findByUserId(userId)
        .orElseGet(() -> {
          PatientProfile newProfile = new PatientProfile();
          newProfile.setId(userId);
          newProfile.setUserId(userId);
          newProfile.setCreatedAt(java.time.LocalDateTime.now());
          newProfile.setUpdatedAt(java.time.LocalDateTime.now());
          return patientProfileRepository.save(newProfile);
        });

    return UserProfileResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .fullName(profile.getFullName())
        .dateOfBirth(profile.getDateOfBirth())
        .gender(profile.getGender())
        .height(profile.getHeightCm())
        .weight(profile.getWeightKg())
        .avatarUrl(profile.getAvatarUrl())
        .medicalHistory(profile.getMedicalHistory())
        .forbiddenFoods(profile.getRestrictedFoods() != null ? 
            profile.getRestrictedFoods().stream().map(PatientProfile.RestrictedFood::getFoodName).toList() : null)
        .subscriptionPlan(user.getSubscriptionPlan())
        .privacySettings(profile.getPrivacySettings())
        .build();
  }

  public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
    PatientProfile profile = patientProfileRepository.findByUserId(userId)
        .orElseGet(() -> {
          PatientProfile newProfile = new PatientProfile();
          newProfile.setUserId(userId);
          newProfile.setId(userId);
          newProfile.setCreatedAt(java.time.LocalDateTime.now());
          return newProfile;
        });

    profile.setUpdatedAt(java.time.LocalDateTime.now());

    System.out.println("Updating profile for user: " + userId);
    System.out.println("Request: weight=" + request.getWeight() + ", height=" + request.getHeight() + ", gender="
        + request.getGender());

    if (request.getFullName() != null)
      profile.setFullName(request.getFullName());
    if (request.getDateOfBirth() != null)
      profile.setDateOfBirth(request.getDateOfBirth());
    if (request.getGender() != null)
      profile.setGender(request.getGender());
    if (request.getHeight() != null) {
      System.out.println("Setting height to: " + request.getHeight());
      profile.setHeightCm(request.getHeight());
    }
    if (request.getWeight() != null) {
      System.out.println("Setting weight to: " + request.getWeight());
      profile.setWeightKg(request.getWeight());
    }

    if (request.getMedicalHistory() != null) {
      // Append new medical history if it doesn't already exist or just replace?
      // Usually diagnosis is added to a list.
      java.util.List<String> currentHistory = profile.getMedicalHistory();
      if (currentHistory == null) currentHistory = new java.util.ArrayList<>();
      for (String history : request.getMedicalHistory()) {
          if (!currentHistory.contains(history)) {
              currentHistory.add(history);
          }
      }
      profile.setMedicalHistory(currentHistory);
    }

    if (request.getForbiddenFoods() != null) {
        java.util.List<PatientProfile.RestrictedFood> currentRestricted = profile.getRestrictedFoods();
        if (currentRestricted == null) currentRestricted = new java.util.ArrayList<>();
        
        String sourceId = request.getSourceId() != null ? request.getSourceId() : "manual";

      // Replace all foods from the same source to keep client and DB in sync.
      currentRestricted.removeIf(rf -> sourceId.equals(rf.getSourceId()));
        
        for (String foodName : request.getForbiddenFoods()) {
            boolean exists = currentRestricted.stream()
                .anyMatch(rf -> rf.getFoodName().equalsIgnoreCase(foodName) && sourceId.equals(rf.getSourceId()));
            
            if (!exists) {
                PatientProfile.RestrictedFood rf = new PatientProfile.RestrictedFood();
                rf.setFoodName(foodName);
                rf.setSourceId(sourceId);
                currentRestricted.add(rf);
            }
        }
        profile.setRestrictedFoods(currentRestricted);
    }

    patientProfileRepository.save(profile);
    System.out.println("Profile saved successfully");

    if (request.getWeight() != null || request.getHeight() != null) {
      try {
        waterIntakeService.recalculateGoal(userId);
      } catch (Exception e) {
        log.warn("Failed to recalculate water goal for user: {}", userId, e);
      }
    }

    return getProfile(userId);
  }

  public void updateDeviceToken(String userId, String deviceToken) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    user.setDeviceToken(deviceToken);
    userRepository.save(user);
  }

  public void removeDeviceToken(String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    user.setDeviceToken(null);
    userRepository.save(user);
  }

  public String updateAvatar(String userId, org.springframework.web.multipart.MultipartFile file) {
    try {
      String url = s3Service.uploadFile(file, "avatars/" + userId);
      PatientProfile profile = patientProfileRepository.findByUserId(userId)
          .orElseGet(() -> {
            PatientProfile newProfile = new PatientProfile();
            newProfile.setUserId(userId);
            newProfile.setId(userId);
            newProfile.setCreatedAt(java.time.LocalDateTime.now());
            return newProfile;
          });
      profile.setAvatarUrl(url);
      profile.setUpdatedAt(java.time.LocalDateTime.now());
      patientProfileRepository.save(profile);
      return url;
    } catch (Exception e) {
      throw new RuntimeException("Failed to upload avatar", e);
    }
  }

  public UserContactResponse getContactInfo(String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return UserContactResponse.builder()
        .email(user.getEmail())
        .deviceToken(user.getDeviceToken())
        .build();
  }

  public void updateTracking(String userId, com.healthcarenow.core.dto.TrackingRequest request) {
    PatientProfile profile = patientProfileRepository.findByUserId(userId)
        .orElseGet(() -> {
          PatientProfile newProfile = new PatientProfile();
          newProfile.setUserId(userId);
          newProfile.setId(userId);
          newProfile.setCreatedAt(java.time.LocalDateTime.now());
          return newProfile;
        });

    if (request.getStatus() != null) {
        profile.setActiveStatus(request.getStatus());
    }
    
    if (request.getLat() != null && request.getLng() != null) {
        PatientProfile.LocationInfo loc = new PatientProfile.LocationInfo();
        loc.setLat(request.getLat());
        loc.setLng(request.getLng());
        loc.setUpdatedAt(java.time.LocalDateTime.now());
        profile.setLastKnownLocation(loc);
    }
    
    profile.setLastActiveAt(java.time.LocalDateTime.now());
    profile.setUpdatedAt(java.time.LocalDateTime.now());
    patientProfileRepository.save(profile);
  }

  public void clearForbiddenFoods(String userId) {
    patientProfileRepository.findByUserId(userId).ifPresent(profile -> {
        profile.setRestrictedFoods(new java.util.ArrayList<>());
        profile.setUpdatedAt(java.time.LocalDateTime.now());
        patientProfileRepository.save(profile);
    });
  }

  public void removeForbiddenFoodsBySource(String userId, String sourceId) {
    patientProfileRepository.findByUserId(userId).ifPresent(profile -> {
        if (profile.getRestrictedFoods() != null) {
            profile.getRestrictedFoods().removeIf(rf -> sourceId.equals(rf.getSourceId()));
            profile.setUpdatedAt(java.time.LocalDateTime.now());
            patientProfileRepository.save(profile);
            System.out.println("Removed forbidden foods for source: " + sourceId);
        }
    });
  }

  public void deleteAccount(String userId) {
    userRepository.findById(userId).ifPresent(user -> {
        user.setStatus("DELETED");
        user.setDeletedAt(java.time.LocalDateTime.now());
        user.setDeviceToken(null);
        userRepository.save(user);
        System.out.println("User soft deleted: " + userId);
    });
  }
}
