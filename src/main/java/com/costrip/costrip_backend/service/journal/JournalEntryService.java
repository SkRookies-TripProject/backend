package com.costrip.costrip_backend.service.journal;

import com.costrip.costrip_backend.dto.journal.JournalEntryRequestDto;
import com.costrip.costrip_backend.dto.journal.JournalEntryResponseDto;
import com.costrip.costrip_backend.entity.Trip;
import com.costrip.costrip_backend.entity.User;
import com.costrip.costrip_backend.entity.journal.JournalEntry;
import com.costrip.costrip_backend.exception.ResourceNotFoundException;
import com.costrip.costrip_backend.repository.TripRepository;
import com.costrip.costrip_backend.repository.UserRepository;
import com.costrip.costrip_backend.repository.journal.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    /**
     * 여행별 메모 목록을 조회한다.
     * recordDate가 있으면 특정 날짜 메모만 조회한다.
     */
    public List<JournalEntryResponseDto> getJournalEntries(
            String email,
            Long tripId,
            LocalDate recordDate
    ) {
        User user = findUserByEmail(email);
        Trip trip = findTripByIdAndUserId(tripId, user.getId());

        // 날짜 필터가 있으면 여행 기간 내 날짜인지 먼저 검증한다.
        if (recordDate != null) {
            validateRecordDateInTripRange(recordDate, trip);
        }

        List<JournalEntry> journalEntries = (recordDate == null)
                ? journalEntryRepository.findByTripIdOrderByRecordDateAscCreatedAtAsc(tripId)
                : journalEntryRepository.findByTripIdAndRecordDateOrderByCreatedAtAsc(tripId, recordDate);

        return journalEntries.stream()
                .map(JournalEntryResponseDto::from)
                .toList();
    }

    /**
     * 메모 단건 상세를 조회한다.
     */
    public JournalEntryResponseDto getJournalEntry(String email, Long entryId) {
        JournalEntry journalEntry = findJournalEntryByIdAndUser(entryId, email);
        return JournalEntryResponseDto.from(journalEntry);
    }

    /**
     * 여행에 날짜별 메모를 생성한다.
     * 메모 날짜가 여행 기간 내에 있는지 검증한다.
     */
    @Transactional
    public JournalEntryResponseDto createJournalEntry(
            String email,
            Long tripId,
            JournalEntryRequestDto requestDto
    ) {
        User user = findUserByEmail(email);
        Trip trip = findTripByIdAndUserId(tripId, user.getId());

        validateJournalRequest(trip, requestDto);

        JournalEntry journalEntry = JournalEntry.builder()
                .trip(trip)
                .recordDate(requestDto.getRecordDate())
                .memo(requestDto.getMemo())
                .build();

        JournalEntry savedJournalEntry = journalEntryRepository.save(journalEntry);
        return JournalEntryResponseDto.from(savedJournalEntry);
    }

    /**
     * 기존 메모의 날짜와 내용을 수정한다.
     * 본인 여행의 메모인지 먼저 확인한다.
     */
    @Transactional
    public JournalEntryResponseDto updateJournalEntry(
            String email,
            Long entryId,
            JournalEntryRequestDto requestDto
    ) {
        JournalEntry journalEntry = findJournalEntryByIdAndUser(entryId, email);

        validateJournalRequest(journalEntry.getTrip(), requestDto);
        validateOwner(journalEntry, email);

        journalEntry.setRecordDate(requestDto.getRecordDate());
        journalEntry.setMemo(requestDto.getMemo());

        return JournalEntryResponseDto.from(journalEntry);
    }

    /**
     * 메모를 삭제한다.
     * 본인 여행의 메모만 삭제할 수 있다.
     */
    @Transactional
    public void deleteJournalEntry(String email, Long entryId) {
        JournalEntry journalEntry = findJournalEntryByIdAndUser(entryId, email);
        validateOwner(journalEntry, email);
        journalEntryRepository.delete(journalEntry);
    }

    /**
     * 이메일로 사용자 정보를 조회한다.
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "사용자를 찾을 수 없습니다: " + email,
                        HttpStatus.NOT_FOUND
                ));
    }

    /**
     * 현재 로그인한 사용자의 여행인지 확인하면서 여행을 조회한다.
     */
    private Trip findTripByIdAndUserId(Long tripId, Long userId) {
        return tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "여행을 찾을 수 없습니다: " + tripId,
                        HttpStatus.NOT_FOUND
                ));
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
     * 메모가 현재 로그인한 사용자의 여행에 속한 데이터인지 확인한다.
     */
    private void validateOwner(JournalEntry journalEntry, String email) {
        String ownerEmail = journalEntry.getTrip().getUser().getEmail();
        if (!ownerEmail.equals(email)) {
            throw new AccessDeniedException("이 메모에 접근할 권한이 없습니다.");
        }
    }

    /**
     * 메모 요청값에 대해 여행 기간 내 날짜인지 검증한다.
     */
    private void validateJournalRequest(Trip trip, JournalEntryRequestDto requestDto) {
        validateRecordDateInTripRange(requestDto.getRecordDate(), trip);
    }

    /**
     * 메모 날짜가 여행 시작일과 종료일 사이인지 확인한다.
     */
    private void validateRecordDateInTripRange(LocalDate recordDate, Trip trip) {
        if (trip.getStartDate() != null && recordDate.isBefore(trip.getStartDate())) {
            throw new IllegalArgumentException("기록 날짜는 여행 시작일 이후여야 합니다.");
        }

        if (trip.getEndDate() != null && recordDate.isAfter(trip.getEndDate())) {
            throw new IllegalArgumentException("기록 날짜는 여행 종료일 이전이어야 합니다.");
        }
    }
}
