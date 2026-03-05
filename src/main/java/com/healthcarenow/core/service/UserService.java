package com.healthcarenow.core.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

  @Cacheable(value = "user_profiles", key = "#userId")
  public UserProfileResponse getProfile(String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    PatientProfile profile = patientProfileRepository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

    return UserProfileResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .fullName(profile.getFullName()) // Only in profile now
        .dateOfBirth(profile.getDateOfBirth())
        .gender(profile.getGender())
        .heightCm(profile.getHeightCm())
        .weightKg(profile.getWeightKg())
        .privacySettings(profile.getPrivacySettings())
        .build();
  }

  @CacheEvict(value = "user_profiles", key = "#userId")
  public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
    PatientProfile profile = patientProfileRepository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

    if (request.getFullName() != null)
      profile.setFullName(request.getFullName());
    if (request.getDateOfBirth() != null)
      profile.setDateOfBirth(request.getDateOfBirth());
    if (request.getGender() != null)
      profile.setGender(request.getGender());
    if (request.getHeightCm() != null)
      profile.setHeightCm(request.getHeightCm());
    if (request.getWeightKg() != null)
      profile.setWeightKg(request.getWeightKg());

    patientProfileRepository.save(profile);

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

  public UserContactResponse getContactInfo(String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return UserContactResponse.builder()
        .email(user.getEmail())
        .deviceToken(user.getDeviceToken())
        .build();
  }
}
