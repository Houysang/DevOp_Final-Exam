package com.example.demo.controller;

import com.example.demo.dto.ProfileDTO;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // ========== REST API ENDPOINTS ==========

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<ProfileDTO>> getAllProfiles() {
        List<Profile> profiles = profileService.getAllProfiles();
        return ResponseEntity.ok(profileService.convertToDTOList(profiles));
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ProfileDTO> getProfileById(@PathVariable Long id) {
        return profileService.getProfileById(id)
                .map(profile -> ResponseEntity.ok(profileService.convertToDTO(profile)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/unique/{uniqueId}")
    @ResponseBody
    public ResponseEntity<ProfileDTO> getProfileByUniqueId(@PathVariable String uniqueId) {
        return profileService.getProfileByUniqueId(uniqueId)
                .map(profile -> ResponseEntity.ok(profileService.convertToDTO(profile)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<ProfileDTO> createProfile(
            @RequestPart("profile") @Valid ProfileDTO profileDTO,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        try {
            Profile profile;
            if (photo != null && !photo.isEmpty()) {
                profile = profileService.createProfileWithPhoto(profileDTO, photo);
            } else {
                profile = profileService.createProfile(profileDTO);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(profileService.convertToDTO(profile));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload photo", e);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<ProfileDTO> updateProfile(
            @PathVariable Long id,
            @RequestPart("profile") @Valid ProfileDTO profileDTO,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        try {
            Profile profile = profileService.updateProfile(id, profileDTO);
            if (photo != null && !photo.isEmpty()) {
                profile = profileService.updateProfilePhoto(id, photo);
            }
            return ResponseEntity.ok(profileService.convertToDTO(profile));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload photo", e);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        profileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<ProfileDTO>> searchProfiles(@RequestParam("q") String searchTerm) {
        List<Profile> profiles = profileService.searchProfiles(searchTerm);
        return ResponseEntity.ok(profileService.convertToDTOList(profiles));
    }

    @GetMapping("/type/{profileType}")
    @ResponseBody
    public ResponseEntity<List<ProfileDTO>> getProfilesByType(@PathVariable ProfileType profileType) {
        List<Profile> profiles = profileService.getProfilesByType(profileType);
        return ResponseEntity.ok(profileService.convertToDTOList(profiles));
    }

    @PostMapping("/batch")
    @ResponseBody
    public ResponseEntity<List<ProfileDTO>> batchCreateProfiles(@RequestBody @Valid List<ProfileDTO> profileDTOList) {
        List<Profile> profiles = profileService.batchCreateProfiles(profileDTOList);
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.convertToDTOList(profiles));
    }

    @GetMapping("/{id}/photo")
    @ResponseBody
    public ResponseEntity<byte[]> getProfilePhoto(@PathVariable Long id) {
        return profileService.getProfileById(id)
                .filter(p -> p.getPhoto() != null)
                .map(profile -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(
                                profile.getPhotoContentType() != null ? profile.getPhotoContentType() : "image/jpeg"))
                        .body(profile.getPhoto()))
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== THYMELEAF VIEW ENDPOINTS ==========

    @GetMapping("/view/all")
    public String viewAllProfiles(Model model) {
        List<Profile> profiles = profileService.getAllProfiles();
        List<ProfileDTO> profileDTOS = profileService.convertToDTOList(profiles);
        // Add Base64 photo data for display
        Map<Long, String> photoMap = new HashMap<>();
        for (Profile p : profiles) {
            if (p.getPhoto() != null) {
                String base64 = Base64.getEncoder().encodeToString(p.getPhoto());
                String contentType = p.getPhotoContentType() != null ? p.getPhotoContentType() : "image/jpeg";
                photoMap.put(p.getId(), "data:" + contentType + ";base64," + base64);
            }
        }
        model.addAttribute("profiles", profileDTOS);
        model.addAttribute("photos", photoMap);
        model.addAttribute("profileTypes", ProfileType.values());
        return "profiles/list";
    }

    @GetMapping("/view/create")
    public String showCreateForm(Model model) {
        model.addAttribute("profileDTO", new ProfileDTO());
        model.addAttribute("profileTypes", ProfileType.values());
        return "profiles/create";
    }

    @PostMapping("/view/create")
    public String createProfileFromForm(
            @Valid @ModelAttribute("profileDTO") ProfileDTO profileDTO,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            Model model) {
        try {
            Profile profile;
            if (photo != null && !photo.isEmpty()) {
                profile = profileService.createProfileWithPhoto(profileDTO, photo);
            } else {
                profile = profileService.createProfile(profileDTO);
            }
            return "redirect:/api/profiles/view/" + profile.getId();
        } catch (IOException e) {
            model.addAttribute("error", "Failed to upload photo: " + e.getMessage());
            model.addAttribute("profileTypes", ProfileType.values());
            return "profiles/create";
        }
    }

    @GetMapping("/view/{id}")
    public String viewProfileDetail(@PathVariable Long id, Model model) {
        return profileService.getProfileById(id)
                .map(profile -> {
                    model.addAttribute("profile", profileService.convertToDTO(profile));
                    // Add photo as Base64
                    if (profile.getPhoto() != null) {
                        String base64 = Base64.getEncoder().encodeToString(profile.getPhoto());
                        String contentType = profile.getPhotoContentType() != null ? profile.getPhotoContentType() : "image/jpeg";
                        model.addAttribute("photo", "data:" + contentType + ";base64," + base64);
                    }
                    model.addAttribute("profileTypes", ProfileType.values());
                    return "profiles/detail";
                })
                .orElse("redirect:/api/profiles/view/all");
    }

    @GetMapping("/view/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        return profileService.getProfileById(id)
                .map(profile -> {
                    model.addAttribute("profileDTO", profileService.convertToDTO(profile));
                    model.addAttribute("profileId", id);
                    model.addAttribute("profileTypes", ProfileType.values());
                    return "profiles/edit";
                })
                .orElse("redirect:/api/profiles/view/all");
    }

    @PostMapping("/view/edit/{id}")
    public String updateProfileFromForm(
            @PathVariable Long id,
            @Valid @ModelAttribute("profileDTO") ProfileDTO profileDTO,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            Model model) {
        try {
            profileService.updateProfile(id, profileDTO);
            if (photo != null && !photo.isEmpty()) {
                profileService.updateProfilePhoto(id, photo);
            }
            return "redirect:/api/profiles/view/" + id;
        } catch (IOException e) {
            model.addAttribute("error", "Failed to upload photo: " + e.getMessage());
            model.addAttribute("profileId", id);
            model.addAttribute("profileTypes", ProfileType.values());
            return "profiles/edit";
        }
    }

    @PostMapping("/view/delete/{id}")
    public String deleteProfileFromForm(@PathVariable Long id) {
        profileService.deleteProfile(id);
        return "redirect:/api/profiles/view/all";
    }

    @GetMapping("/view/batch")
    public String showBatchCreateForm(Model model) {
        model.addAttribute("profileTypes", ProfileType.values());
        return "profiles/batch";
    }

    @GetMapping("/view/live-preview/{id}")
    public String showLivePreview(@PathVariable Long id, Model model) {
        return profileService.getProfileById(id)
                .map(profile -> {
                    model.addAttribute("profile", profile);
                    model.addAttribute("profileDTO", profileService.convertToDTO(profile));
                    // Add photo as Base64
                    if (profile.getPhoto() != null) {
                        String base64 = Base64.getEncoder().encodeToString(profile.getPhoto());
                        String contentType = profile.getPhotoContentType() != null ? profile.getPhotoContentType() : "image/jpeg";
                        model.addAttribute("photo", "data:" + contentType + ";base64," + base64);
                    }
                    return "preview/live-preview";
                })
                .orElse("redirect:/api/profiles/view/all");
    }
}