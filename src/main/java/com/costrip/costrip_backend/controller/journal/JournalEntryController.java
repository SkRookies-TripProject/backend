package com.costrip.costrip_backend.controller.journal;

import com.costrip.costrip_backend.dto.common.ApiResponse;
import com.costrip.costrip_backend.dto.journal.JournalEntryRequestDto;
import com.costrip.costrip_backend.dto.journal.JournalEntryResponseDto;
import com.costrip.costrip_backend.service.journal.JournalEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    // 여행별 전체 목록 또는 특정 날짜 메모 목록을 조회한다.
    @GetMapping("/trips/{tripId}/journal-entries")
    public ResponseEntity<ApiResponse<List<JournalEntryResponseDto>>> getJournalEntries(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tripId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate recordDate
    ) {
        List<JournalEntryResponseDto> journalEntries = journalEntryService.getJournalEntries(
                userDetails.getUsername(),
                tripId,
                recordDate
        );

        return ResponseEntity.ok(ApiResponse.success("Journal entries retrieved.", journalEntries));
    }

    // 엔트리 단건 상세 조회다.
    @GetMapping("/journal-entries/{entryId}")
    public ResponseEntity<ApiResponse<JournalEntryResponseDto>> getJournalEntry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long entryId
    ) {
        JournalEntryResponseDto journalEntry = journalEntryService.getJournalEntry(
                userDetails.getUsername(),
                entryId
        );

        return ResponseEntity.ok(ApiResponse.success("Journal entry retrieved.", journalEntry));
    }

    // 여행 하위 리소스로 엔트리를 생성한다.
    @PostMapping("/trips/{tripId}/journal-entries")
    public ResponseEntity<ApiResponse<JournalEntryResponseDto>> createJournalEntry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tripId,
            @Valid @RequestBody JournalEntryRequestDto requestDto
    ) {
        JournalEntryResponseDto journalEntry = journalEntryService.createJournalEntry(
                userDetails.getUsername(),
                tripId,
                requestDto
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Journal entry created.", journalEntry));
    }

    // 메모와 기록 날짜를 함께 수정한다.
    @PutMapping("/journal-entries/{entryId}")
    public ResponseEntity<ApiResponse<JournalEntryResponseDto>> updateJournalEntry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long entryId,
            @Valid @RequestBody JournalEntryRequestDto requestDto
    ) {
        JournalEntryResponseDto journalEntry = journalEntryService.updateJournalEntry(
                userDetails.getUsername(),
                entryId,
                requestDto
        );

        return ResponseEntity.ok(ApiResponse.success("Journal entry updated.", journalEntry));
    }

    // 삭제는 hard delete 정책을 사용한다.
    @DeleteMapping("/journal-entries/{entryId}")
    public ResponseEntity<ApiResponse<Void>> deleteJournalEntry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long entryId
    ) {
        journalEntryService.deleteJournalEntry(userDetails.getUsername(), entryId);
        return ResponseEntity.ok(ApiResponse.success("Journal entry deleted.", null));
    }
}
