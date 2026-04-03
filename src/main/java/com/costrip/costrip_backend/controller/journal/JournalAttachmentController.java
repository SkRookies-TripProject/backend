package com.costrip.costrip_backend.controller.journal;

import com.costrip.costrip_backend.dto.common.ApiResponse;
import com.costrip.costrip_backend.dto.journal.JournalAttachmentResponseDto;
import com.costrip.costrip_backend.service.journal.JournalAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JournalAttachmentController {

    private final JournalAttachmentService journalAttachmentService;

    /**
     * 메모 한 건에 이미지 파일 여러 장을 업로드한다.
     */
    @PostMapping(
            value = "/journal-entries/{entryId}/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<List<JournalAttachmentResponseDto>>> uploadAttachments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long entryId,
            @RequestParam("files") List<MultipartFile> files
    ) {
        List<JournalAttachmentResponseDto> attachments = journalAttachmentService.uploadAttachments(
                userDetails.getUsername(),
                entryId,
                files
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("메모 이미지가 업로드되었습니다.", attachments));
    }

    /**
     * 메모 이미지 한 건을 삭제한다.
     */
    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long attachmentId
    ) {
        journalAttachmentService.deleteAttachment(userDetails.getUsername(), attachmentId);
        return ResponseEntity.ok(ApiResponse.success("메모 이미지가 삭제되었습니다.", null));
    }
}
