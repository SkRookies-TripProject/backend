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

    public JournalEntryResponseDto getJournalEntry(String email, Long entryId) {
        JournalEntry journalEntry = findJournalEntryByIdAndUser(entryId, email);
        return JournalEntryResponseDto.from(journalEntry);
    }

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

    @Transactional
    public void deleteJournalEntry(String email, Long entryId) {
        JournalEntry journalEntry = findJournalEntryByIdAndUser(entryId, email);
        validateOwner(journalEntry, email);
        journalEntryRepository.delete(journalEntry);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email,
                        HttpStatus.NOT_FOUND
                ));
    }

    private Trip findTripByIdAndUserId(Long tripId, Long userId) {
        return tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trip not found: " + tripId,
                        HttpStatus.NOT_FOUND
                ));
    }

    private JournalEntry findJournalEntryByIdAndUser(Long entryId, String email) {
        return journalEntryRepository.findByIdAndUserEmail(entryId, email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Journal entry not found: " + entryId,
                        HttpStatus.NOT_FOUND
                ));
    }

    private void validateOwner(JournalEntry journalEntry, String email) {
        String ownerEmail = journalEntry.getTrip().getUser().getEmail();
        if (!ownerEmail.equals(email)) {
            throw new AccessDeniedException("You do not have access to this journal entry.");
        }
    }

    private void validateJournalRequest(Trip trip, JournalEntryRequestDto requestDto) {
        validateRecordDateInTripRange(requestDto.getRecordDate(), trip);
    }

    private void validateRecordDateInTripRange(LocalDate recordDate, Trip trip) {
        if (trip.getStartDate() != null && recordDate.isBefore(trip.getStartDate())) {
            throw new IllegalArgumentException("recordDate must be on or after the trip start date.");
        }

        if (trip.getEndDate() != null && recordDate.isAfter(trip.getEndDate())) {
            throw new IllegalArgumentException("recordDate must be on or before the trip end date.");
        }
    }
}
