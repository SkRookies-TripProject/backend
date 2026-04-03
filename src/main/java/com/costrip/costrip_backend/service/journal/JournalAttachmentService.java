package com.costrip.costrip_backend.service.journal;

import com.costrip.costrip_backend.dto.journal.JournalAttachmentResponseDto;
import com.costrip.costrip_backend.entity.Attachment;
import com.costrip.costrip_backend.entity.journal.JournalEntry;
import com.costrip.costrip_backend.exception.ResourceNotFoundException;
import com.costrip.costrip_backend.repository.AttachmentRepository;
import com.costrip.costrip_backend.repository.journal.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JournalAttachmentService {

    private static final Path JOURNAL_UPLOAD_DIR = Paths.get("uploads", "journal");

    private final JournalEntryRepository journalEntryRepository;
    private final AttachmentRepository attachmentRepository;

    /**
     * 메모 한 건에 이미지 파일을 저장하고 첨부 메타데이터를 반환한다.
     */
    @Transactional
    public List<JournalAttachmentResponseDto> uploadAttachments(
            String email,
            Long entryId,
            List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드할 이미지 파일이 없습니다.");
        }

        JournalEntry journalEntry = findJournalEntryByIdAndUser(entryId, email);
        createUploadDirectoryIfNeeded();

        return files.stream()
                .filter(file -> !file.isEmpty())
                .map(file -> saveAttachment(journalEntry, file))
                .map(JournalAttachmentResponseDto::from)
                .toList();
    }

    /**
     * 메모 이미지 한 건을 삭제하고 저장된 파일도 함께 정리한다.
     */
    @Transactional
    public void deleteAttachment(String email, Long attachmentId) {
        Attachment attachment = attachmentRepository.findByIdAndUserEmail(attachmentId, email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "이미지를 찾을 수 없습니다: " + attachmentId,
                        HttpStatus.NOT_FOUND
                ));

        deletePhysicalFile(attachment.getFilePath());
        attachmentRepository.delete(attachment);
    }

    /**
     * 현재 로그인한 사용자가 접근 가능한 메모인지 확인하면서 메모를 조회한다.
     */
    private JournalEntry findJournalEntryByIdAndUser(Long entryId, String email) {
        return journalEntryRepository.findByIdAndUserEmail(entryId, email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "메모를 찾을 수 없습니다: " + entryId,
                        HttpStatus.NOT_FOUND
                ));
    }

    /**
     * 이미지 파일을 로컬에 저장하고 attachments 테이블에 메타데이터를 기록한다.
     */
    private Attachment saveAttachment(JournalEntry journalEntry, MultipartFile file) {
        validateImageFile(file);

        String originalFileName = file.getOriginalFilename();
        String storedFileName = createStoredFileName(originalFileName);
        Path targetPath = JOURNAL_UPLOAD_DIR.resolve(storedFileName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 저장 중 오류가 발생했습니다.", e);
        }

        Attachment attachment = Attachment.builder()
                .trip(journalEntry.getTrip())
                .journalEntry(journalEntry)
                .fileName(originalFileName)
                .filePath("/uploads/journal/" + storedFileName)
                .fileType(file.getContentType())
                .build();

        return attachmentRepository.save(attachment);
    }

    /**
     * 이미지 업로드 경로가 없으면 생성한다.
     */
    private void createUploadDirectoryIfNeeded() {
        try {
            Files.createDirectories(JOURNAL_UPLOAD_DIR);
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 경로를 생성할 수 없습니다.", e);
        }
    }

    /**
     * 이미지 파일만 업로드되도록 확장자와 MIME 타입을 확인한다.
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("비어 있는 파일은 업로드할 수 없습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }
    }

    /**
     * 중복 파일명을 피하기 위해 UUID 기반 저장 파일명을 만든다.
     */
    private String createStoredFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null) {
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex >= 0) {
                extension = originalFileName.substring(lastDotIndex);
            }
        }
        return UUID.randomUUID() + extension;
    }

    /**
     * 파일 삭제는 DB 정리와 별도로 best-effort로 시도한다.
     */
    private void deletePhysicalFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }

        String normalizedPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
        Path targetPath = Paths.get(normalizedPath);

        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 삭제 중 오류가 발생했습니다.", e);
        }
    }
}
