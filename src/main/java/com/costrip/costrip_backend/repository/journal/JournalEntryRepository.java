package com.costrip.costrip_backend.repository.journal;

import com.costrip.costrip_backend.entity.journal.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    // 여행 상세 화면에서 날짜순 목록을 바로 내려주기 위한 조회다.
    List<JournalEntry> findByTripIdOrderByRecordDateAscCreatedAtAsc(Long tripId);

    // 특정 날짜의 기록만 필터링할 때 사용한다.
    List<JournalEntry> findByTripIdAndRecordDateOrderByCreatedAtAsc(Long tripId, LocalDate recordDate);

    // 상세 조회에서 소유권을 trip.user 기준으로 같이 확인한다.
    @Query("""
            SELECT journalEntry
            FROM JournalEntry journalEntry
            JOIN journalEntry.trip trip
            JOIN trip.user user
            WHERE journalEntry.id = :entryId
              AND user.email = :email
            """)
    Optional<JournalEntry> findByIdAndUserEmail(
            @Param("entryId") Long entryId,
            @Param("email") String email
    );
}
