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

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PatientProfileRepository patientProfileRepository;
  private final S3Service s3Service;

  public UserProfileResponse getProfile(String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    PatientProfile profile = patientProfileRepository.findByUserId(userId)
        .orElseGet(() -> {
          // Fallback: create profile if missing
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

    patientProfileRepository.save(profile);
    System.out.println("Profile saved successfully");

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
}
