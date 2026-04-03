package com.costrip.costrip_backend.dto.journal;

import com.costrip.costrip_backend.entity.Attachment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class JournalAttachmentResponseDto {

    private Long attachmentId;
    private String fileName;
    private String filePath;
    private String fileType;
    private LocalDateTime createdAt;

    public static JournalAttachmentResponseDto from(Attachment attachment) {
        return JournalAttachmentResponseDto.builder()
                .attachmentId(attachment.getId())
                .fileName(attachment.getFileName())
                .filePath(attachment.getFilePath())
                .fileType(attachment.getFileType())
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
