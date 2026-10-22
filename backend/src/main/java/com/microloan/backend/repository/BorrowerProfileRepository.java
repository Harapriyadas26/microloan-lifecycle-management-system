package com.microloan.backend.repository;

import com.microloan.backend.entity.BorrowerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BorrowerProfileRepository
        extends JpaRepository<BorrowerProfile, Long> {

    Optional<BorrowerProfile> findByUserEmailIgnoreCase(String email);
}