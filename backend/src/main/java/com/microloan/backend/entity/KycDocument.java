package com.microloan.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "kyc_documents",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_kyc_user_document_type",
                columnNames = {"user_id", "document_type"}
        )
)
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;

    @Column(nullable = false, length = 30)
    private String documentNumber;

    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Column(nullable = false, unique = true, length = 255)
    private String storedFileName;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KycStatus status = KycStatus.PENDING;

    @Column(length = 300)
    private String rejectionReason;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    private LocalDateTime verifiedAt;

    public KycDocument() {
    }

    @PrePersist
    public void beforeInsert() {
        uploadedAt = LocalDateTime.now();

        if (status == null) {
            status = KycStatus.PENDING;
        }
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public KycStatus getStatus() {
        return status;
    }

    public void setStatus(KycStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
