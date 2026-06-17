package com.example.demo.service;

import com.example.demo.dto.ProfileDTO;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileBuilder;
import com.example.demo.model.ProfileType;
import com.example.demo.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Transactional
    public Profile createProfile(ProfileDTO profileDTO) {
        ProfileBuilder builder = new ProfileBuilder()
                .withDefaults(profileDTO.getProfileType())
                .firstName(profileDTO.getFirstName())
                .lastName(profileDTO.getLastName())
                .email(profileDTO.getEmail())
                .phoneNumber(profileDTO.getPhoneNumber())
                .profileType(profileDTO.getProfileType())
                .department(profileDTO.getDepartment())
                .position(profileDTO.getPosition())
                .dateOfBirth(profileDTO.getDateOfBirth())
                .address(profileDTO.getAddress());

        Profile profile = builder.build();
        return profileRepository.save(profile);
    }

    @Transactional
    public Profile createProfileWithPhoto(ProfileDTO profileDTO, MultipartFile photo) throws IOException {
        ProfileBuilder builder = new ProfileBuilder()
                .withDefaults(profileDTO.getProfileType())
                .firstName(profileDTO.getFirstName())
                .lastName(profileDTO.getLastName())
                .email(profileDTO.getEmail())
                .phoneNumber(profileDTO.getPhoneNumber())
                .profileType(profileDTO.getProfileType())
                .department(profileDTO.getDepartment())
                .position(profileDTO.getPosition())
                .dateOfBirth(profileDTO.getDateOfBirth())
                .address(profileDTO.getAddress());

        if (photo != null && !photo.isEmpty()) {
            validatePhoto(photo);
            builder.photo(photo.getBytes());
            builder.photoContentType(photo.getContentType());
        }

        Profile profile = builder.build();
        return profileRepository.save(profile);
    }

    @Transactional
    public Profile updateProfile(Long id, ProfileDTO profileDTO) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));

        profile.setFirstName(profileDTO.getFirstName());
        profile.setLastName(profileDTO.getLastName());
        profile.setEmail(profileDTO.getEmail());
        profile.setPhoneNumber(profileDTO.getPhoneNumber());
        profile.setProfileType(profileDTO.getProfileType());
        profile.setDepartment(profileDTO.getDepartment());
        profile.setPosition(profileDTO.getPosition());
        profile.setDateOfBirth(profileDTO.getDateOfBirth());
        profile.setAddress(profileDTO.getAddress());

        return profileRepository.save(profile);
    }

    @Transactional
    public Profile updateProfilePhoto(Long id, MultipartFile photo) throws IOException {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));

        if (photo != null && !photo.isEmpty()) {
            validatePhoto(photo);
            profile.setPhoto(photo.getBytes());
            profile.setPhotoContentType(photo.getContentType());
        }

        return profileRepository.save(profile);
    }

    @Transactional
    public void deleteProfile(Long id) {
        if (!profileRepository.existsById(id)) {
            throw new RuntimeException("Profile not found with id: " + id);
        }
        profileRepository.deleteById(id);
    }

    public Optional<Profile> getProfileById(Long id) {
        return profileRepository.findById(id);
    }

    public Optional<Profile> getProfileByUniqueId(String uniqueId) {
        return profileRepository.findByUniqueId(uniqueId);
    }

    public List<Profile> getAllProfiles() {
        return profileRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Profile> getProfilesByType(ProfileType profileType) {
        return profileRepository.findByProfileType(profileType);
    }

    public List<Profile> searchProfiles(String searchTerm) {
        return profileRepository.searchProfiles(searchTerm);
    }

    public List<ProfileDTO> convertToDTOList(List<Profile> profiles) {
        return profiles.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public ProfileDTO convertToDTO(Profile profile) {
        return ProfileDTO.builder()
                .id(profile.getId())
                .uniqueId(profile.getUniqueId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .fullName(profile.getFullName())
                .email(profile.getEmail())
                .phoneNumber(profile.getPhoneNumber())
                .profileType(profile.getProfileType())
                .department(profile.getDepartment())
                .position(profile.getPosition())
                .dateOfBirth(profile.getDateOfBirth())
                .address(profile.getAddress())
                .build();
    }

    @Transactional
    public List<Profile> batchCreateProfiles(List<ProfileDTO> profileDTOList) {
        return profileDTOList.stream()
                .map(this::createProfile)
                .collect(Collectors.toList());
    }

    /**
     * Generate unique sequence ID for batch creation
     */
    public String generateNextSequenceId(ProfileType type, String department) {
        long count = profileRepository.countByProfileTypeAndDepartment(type, department) + 1;
        return ProfileBuilder.generateSequenceId(type, department, count);
    }

    private void validatePhoto(MultipartFile photo) {
        String contentType = photo.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new RuntimeException("Only JPEG and PNG images are allowed");
        }

        // Max file size: 5MB
        if (photo.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Photo size must be less than 5MB");
        }
    }
}