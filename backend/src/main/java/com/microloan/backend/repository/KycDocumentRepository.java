package com.microloan.backend.repository;

import com.microloan.backend.entity.DocumentType;
import com.microloan.backend.entity.KycDocument;
import com.microloan.backend.entity.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KycDocumentRepository
        extends JpaRepository<KycDocument, Long> {

    List<KycDocument> findByUserEmailIgnoreCaseOrderByUploadedAtDesc(
            String email
    );

    Optional<KycDocument> findByUserEmailIgnoreCaseAndDocumentType(
            String email,
            DocumentType documentType
    );

    List<KycDocument> findByStatusOrderByUploadedAtAsc(
            KycStatus status
    );
}