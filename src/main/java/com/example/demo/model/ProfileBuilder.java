package com.example.demo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Builder utility to create default Profile instances.
 * Also used to build profiles step by step.
 */
public class ProfileBuilder {

    private final Profile.ProfileBuilder builder;

    public ProfileBuilder() {
        this.builder = Profile.builder();
    }

    public ProfileBuilder withDefaults(ProfileType type) {
        builder.profileType(type);
        builder.uniqueId(generateUniqueId(type));
        builder.createdAt(LocalDateTime.now());
        builder.updatedAt(LocalDateTime.now());
        return this;
    }

    public ProfileBuilder firstName(String firstName) {
        builder.firstName(firstName);
        return this;
    }

    public ProfileBuilder lastName(String lastName) {
        builder.lastName(lastName);
        return this;
    }

    public ProfileBuilder email(String email) {
        builder.email(email);
        return this;
    }

    public ProfileBuilder phoneNumber(String phoneNumber) {
        builder.phoneNumber(phoneNumber);
        return this;
    }

    public ProfileBuilder profileType(ProfileType profileType) {
        builder.profileType(profileType);
        builder.uniqueId(generateUniqueId(profileType));
        return this;
    }

    public ProfileBuilder department(String department) {
        builder.department(department);
        return this;
    }

    public ProfileBuilder position(String position) {
        builder.position(position);
        return this;
    }

    public ProfileBuilder dateOfBirth(LocalDate dateOfBirth) {
        builder.dateOfBirth(dateOfBirth);
        return this;
    }

    public ProfileBuilder address(String address) {
        builder.address(address);
        return this;
    }

    public ProfileBuilder photo(byte[] photo) {
        builder.photo(photo);
        return this;
    }

    public ProfileBuilder photoContentType(String contentType) {
        builder.photoContentType(contentType);
        return this;
    }

    public Profile build() {
        return builder.build();
    }

    /**
     * Generate a unique ID in format: TYPE-YEAR-XXXXXXXX
     * Example: STD-2026-7F3A2C91
     */
    public static String generateUniqueId(ProfileType type) {
        String prefix;
        switch (type) {
            case STUDENT:  prefix = "STD"; break;
            case EMPLOYEE: prefix = "EMP"; break;
            default:       prefix = "USR"; break;
        }
        int year = LocalDate.now().getYear();
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("%s-%d-%s", prefix, year, randomPart);
    }

    /**
     * Generate unique ID with custom sequence: TYPE-YEAR-DEPT-###
     */
    public static String generateSequenceId(ProfileType type, String department, long sequenceNumber) {
        String prefix;
        switch (type) {
            case STUDENT:  prefix = "STD"; break;
            case EMPLOYEE: prefix = "EMP"; break;
            default:       prefix = "USR"; break;
        }
        int year = LocalDate.now().getYear();
        String deptCode = (department != null && department.length() >= 3)
                ? department.substring(0, 3).toUpperCase()
                : "GEN";
        return String.format("%s-%d-%s-%03d", prefix, year, deptCode, sequenceNumber);
    }
}