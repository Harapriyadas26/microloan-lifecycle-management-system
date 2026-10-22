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
    @Value("${spring.servlet.multipart.max-file-size:5MB}")
    private org.springframework.util.unit.DataSize maxFileSize;

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

        User user = userRepository.findLockedByEmail(email)
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
        if(file.getSize()>maxFileSize.toBytes()) throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE,"Document exceeds configured size limit");
        validateSignature(file);
        String contentType = file.getContentType().toLowerCase(Locale.ROOT);
        String extension = getExtension(contentType);
        String storedFileName = UUID.randomUUID() + extension;
        Path newFilePath = safeFilePath(storedFileName);

        boolean newFileCreated = false;
        try {
            Files.createDirectories(uploadRoot);

            try (InputStream inputStream = file.getInputStream();
                 var output = Files.newOutputStream(newFilePath, java.nio.file.StandardOpenOption.CREATE_NEW)) {
                newFileCreated = true;
                inputStream.transferTo(output);
            }

            document.setUser(user);
            document.setDocumentType(documentType);
            document.setDocumentNumber(documentNumber.trim().toUpperCase(Locale.ROOT));
            document.setOriginalFileName(getOriginalFileName(file));
            document.setStoredFileName(storedFileName);
            document.setContentType(contentType);
            document.setFileSize(file.getSize());
            document.setStatus(KycStatus.PENDING);
            document.setRejectionReason(null);
            document.setVerifiedAt(null);
            document.setUploadedAt(java.time.LocalDateTime.now());

            KycDocument savedDocument =
                    kycDocumentRepository.saveAndFlush(document);

            // Filesystem changes cannot participate in the SQL transaction.
            // Retain the old file until commit; remove only the new file on rollback.
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override public void afterCompletion(int status) {
                        if(status==STATUS_COMMITTED) deleteOldFile(oldStoredFileName,storedFileName);
                        else deleteFileQuietly(newFilePath);
                    }
                });

            return toResponse(savedDocument);

        } catch (IOException exception) {
            if (newFileCreated) deleteFileQuietly(newFilePath);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to store the KYC document"
            );
        } catch (RuntimeException exception) {
            if (newFileCreated) deleteFileQuietly(newFilePath);
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
                .matches("(?=.*[A-Za-z0-9])[A-Za-z0-9 -]{4,30}")) {
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

        String safe=originalName.replace('\\','/');
        safe=safe.substring(safe.lastIndexOf('/')+1).replaceAll("[^A-Za-z0-9._ -]","_");
        if(safe.isBlank()) return "document";
        return safe.substring(0, Math.min(safe.length(),150));
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
                document.getUser().getId(),
                document.getUser().getFullName(),
                document.getUser().getEmail(),
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
    private void validateSignature(MultipartFile file) {
        try(InputStream in=file.getInputStream()) {
            byte[] b=in.readNBytes(8);
            boolean pdf=b.length>=5 && b[0]==37 && b[1]==80 && b[2]==68 && b[3]==70 && b[4]==45;
            boolean jpg=b.length>=3 && (b[0]&255)==255 && (b[1]&255)==216 && (b[2]&255)==255;
            boolean png=java.util.Arrays.equals(b,new byte[]{(byte)137,80,78,71,13,10,26,10});
            String type=file.getContentType().toLowerCase(Locale.ROOT);
            if(!(type.equals("application/pdf") && pdf || type.equals("image/jpeg") && jpg || type.equals("image/png") && png))
                throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,"File contents do not match PDF, JPEG or PNG type");
        } catch(IOException e) {throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unable to read document");}
    }

    @Transactional(readOnly=true)
    public List<KycDocumentResponse> listForOfficer(KycStatus status) {
        return (status==null?kycDocumentRepository.findAllByOrderByUploadedAtDesc():
            kycDocumentRepository.findByStatusOrderByUploadedAtAsc(status)).stream().map(this::toResponse).toList();
    }
    @Transactional(readOnly=true)
    public KycDocumentResponse detail(Long id,String email) {return toResponse(findDocument(id,email));}

    @Transactional(readOnly=true)
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> file(Long id,String email,boolean download) {
        KycDocument d=findDocument(id,email);
        Path path=safeFilePath(d.getStoredFileName());
        if(!Files.isRegularFile(path) || Files.isSymbolicLink(path))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Document file unavailable");
        try {
            if(!path.toRealPath().startsWith(uploadRoot.toRealPath()))
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Document file unavailable");
        } catch(IOException e) {throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Document file unavailable");}
        var disposition=(download?org.springframework.http.ContentDisposition.attachment():org.springframework.http.ContentDisposition.inline())
            .filename(d.getOriginalFileName(),java.nio.charset.StandardCharsets.UTF_8).build();
        return org.springframework.http.ResponseEntity.ok()
            .contentType(org.springframework.http.MediaType.parseMediaType(d.getContentType()))
            .header("Content-Disposition",disposition.toString())
            .header("Cache-Control","no-store")
            .header("X-Content-Type-Options","nosniff")
            .header("Content-Security-Policy","sandbox; default-src 'none'")
            .body(new org.springframework.core.io.FileSystemResource(path));
    }
    @Transactional
    public KycDocumentResponse decide(Long id,boolean verify,String reason) {
        KycDocument found=findDocument(id,null);
        userRepository.findLockedByEmail(found.getUser().getEmail()).orElseThrow();
        // Refresh after waiting for a concurrent replacement/decision on this user.
        entityManager.refresh(found);
        if(found.getStatus()!=KycStatus.PENDING)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Only pending documents may be verified or rejected");
        if(!verify && (reason==null || reason.trim().length()<5 || reason.trim().length()>300))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Provide a rejection reason of 5 to 300 characters");
        found.setStatus(verify?KycStatus.VERIFIED:KycStatus.REJECTED);
        found.setRejectionReason(verify?null:reason.trim());
        found.setVerifiedAt(java.time.LocalDateTime.now());
        return toResponse(kycDocumentRepository.saveAndFlush(found));
    }
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private KycDocument findDocument(Long id,String email) {
        return (email==null?kycDocumentRepository.findById(id):kycDocumentRepository.findByIdAndUserEmailIgnoreCase(id,email))
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"KYC document not found"));
    }

}
