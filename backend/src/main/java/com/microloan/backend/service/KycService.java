package com.microloan.backend.service;

import com.microloan.backend.dto.KycDocumentResponse;
import com.microloan.backend.entity.DocumentType;
import com.microloan.backend.entity.KycDocument;
import com.microloan.backend.entity.KycStatus;
import com.microloan.backend.entity.User;
import com.microloan.backend.repository.KycDocumentRepository;
import com.microloan.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class KycService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    private final KycDocumentRepository kycDocumentRepository;
    private final UserRepository userRepository;
    private final Path uploadRoot;

    public KycService(
            KycDocumentRepository kycDocumentRepository,
            UserRepository userRepository,
            @Value("${app.upload.dir:uploads/kyc}") String uploadDirectory
    ) {
        this.kycDocumentRepository = kycDocumentRepository;
        this.userRepository = userRepository;
        this.uploadRoot = Path.of(uploadDirectory)
                .toAbsolutePath()
                .normalize();
    }

    @Transactional
    public KycDocumentResponse uploadDocument(
            String email,
            DocumentType documentType,
            String documentNumber,
            MultipartFile file
    ) {
        validateInput(documentType, documentNumber, file);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        KycDocument document = kycDocumentRepository
                .findByUserEmailIgnoreCaseAndDocumentType(email, documentType)
                .orElseGet(KycDocument::new);

        if (document.getId() != null
                && document.getStatus() == KycStatus.VERIFIED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A verified document cannot be replaced"
            );
        }

        String oldStoredFileName = document.getStoredFileName();
        String contentType = file.getContentType().toLowerCase(Locale.ROOT);
        String extension = getExtension(contentType);
        String storedFileName = UUID.randomUUID() + extension;
        Path newFilePath = safeFilePath(storedFileName);

        try {
            Files.createDirectories(uploadRoot);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(
                        inputStream,
                        newFilePath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            document.setUser(user);
            document.setDocumentType(documentType);
            document.setDocumentNumber(documentNumber.trim().toUpperCase());
            document.setOriginalFileName(getOriginalFileName(file));
            document.setStoredFileName(storedFileName);
            document.setContentType(contentType);
            document.setFileSize(file.getSize());
            document.setStatus(KycStatus.PENDING);
            document.setRejectionReason(null);
            document.setVerifiedAt(null);

            KycDocument savedDocument =
                    kycDocumentRepository.save(document);

            deleteOldFile(oldStoredFileName, storedFileName);

            return toResponse(savedDocument);

        } catch (IOException exception) {
            deleteFileQuietly(newFilePath);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to store the KYC document"
            );
        } catch (RuntimeException exception) {
            deleteFileQuietly(newFilePath);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<KycDocumentResponse> getMyDocuments(String email) {
        return kycDocumentRepository
                .findByUserEmailIgnoreCaseOrderByUploadedAtDesc(email)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateInput(
            DocumentType documentType,
            String documentNumber,
            MultipartFile file
    ) {
        if (documentType == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document type is required"
            );
        }

        if (documentNumber == null
                || !documentNumber.trim()
                .matches("[A-Za-z0-9 -]{4,30}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Enter a valid document number"
            );
        }

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a document file"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(
                        contentType.toLowerCase(Locale.ROOT)
                )) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Only PDF, JPG and PNG files are allowed"
            );
        }
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "application/pdf" -> ".pdf";
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            default -> throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Unsupported file type"
            );
        };
    }

    private String getOriginalFileName(MultipartFile file) {
        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            return "document";
        }

        return Path.of(originalName).getFileName().toString();
    }

    private Path safeFilePath(String storedFileName) {
        Path filePath = uploadRoot.resolve(storedFileName).normalize();

        if (!filePath.startsWith(uploadRoot)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file name"
            );
        }

        return filePath;
    }

    private void deleteOldFile(
            String oldStoredFileName,
            String newStoredFileName
    ) {
        if (oldStoredFileName == null
                || oldStoredFileName.equals(newStoredFileName)) {
            return;
        }

        deleteFileQuietly(safeFilePath(oldStoredFileName));
    }

    private void deleteFileQuietly(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }

    private KycDocumentResponse toResponse(KycDocument document) {
        return new KycDocumentResponse(
                document.getId(),
                document.getDocumentType(),
                maskDocumentNumber(document.getDocumentNumber()),
                document.getOriginalFileName(),
                document.getContentType(),
                document.getFileSize(),
                document.getStatus(),
                document.getRejectionReason(),
                document.getUploadedAt(),
                document.getVerifiedAt()
        );
    }

    private String maskDocumentNumber(String documentNumber) {
        if (documentNumber == null || documentNumber.isBlank()) {
            return "";
        }

        if (documentNumber.length() <= 4) {
            return "*".repeat(documentNumber.length());
        }

        return "*".repeat(documentNumber.length() - 4)
                + documentNumber.substring(documentNumber.length() - 4);
    }
}