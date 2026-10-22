package com.microloan.backend.service;

import com.microloan.backend.dto.BorrowerProfileRequest;
import com.microloan.backend.dto.BorrowerProfileResponse;
import com.microloan.backend.entity.BorrowerProfile;
import com.microloan.backend.entity.User;
import com.microloan.backend.repository.BorrowerProfileRepository;
import com.microloan.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BorrowerProfileService {

    private final BorrowerProfileRepository profileRepository;
    private final UserRepository userRepository;

    public BorrowerProfileService(
            BorrowerProfileRepository profileRepository,
            UserRepository userRepository) {

        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BorrowerProfileResponse saveProfile(
            String email,
            BorrowerProfileRequest request) {

        User user = userRepository.findLockedByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User account was not found"
                ));

        BorrowerProfile profile = profileRepository
                .findByUserEmailIgnoreCase(email)
                .orElseGet(() -> {
                    BorrowerProfile newProfile = new BorrowerProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setPhone(request.phone().trim());
        profile.setDateOfBirth(request.dateOfBirth());
        profile.setAddress(request.address().trim());
        profile.setCity(request.city().trim());
        profile.setState(request.state().trim());
        profile.setPostalCode(request.postalCode().trim());
        profile.setOccupation(request.occupation().trim());
        profile.setEmploymentType(request.employmentType());
        profile.setMonthlyIncome(request.monthlyIncome());

        return toResponse(profileRepository.saveAndFlush(profile));
    }

    @Transactional(readOnly = true)
    public BorrowerProfileResponse getProfile(String email) {
        BorrowerProfile profile = profileRepository
                .findByUserEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Borrower profile has not been created"
                ));

        return toResponse(profile);
    }

    private BorrowerProfileResponse toResponse(BorrowerProfile profile) {
        User user = profile.getUser();

        return new BorrowerProfileResponse(
                profile.getId(),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                profile.getPhone(),
                profile.getDateOfBirth(),
                profile.getAddress(),
                profile.getCity(),
                profile.getState(),
                profile.getPostalCode(),
                profile.getOccupation(),
                profile.getEmploymentType(),
                profile.getMonthlyIncome(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
