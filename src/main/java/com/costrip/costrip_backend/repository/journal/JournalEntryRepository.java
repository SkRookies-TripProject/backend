package com.costrip.costrip_backend.repository.journal;

import com.costrip.costrip_backend.entity.journal.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    @Query("""
            SELECT DISTINCT journalEntry
            FROM JournalEntry journalEntry
            LEFT JOIN FETCH journalEntry.attachments attachments
            WHERE journalEntry.trip.id = :tripId
            ORDER BY journalEntry.recordDate ASC, journalEntry.createdAt ASC
            """)
    List<JournalEntry> findByTripIdOrderByRecordDateAscCreatedAtAsc(@Param("tripId") Long tripId);

    @Query("""
            SELECT DISTINCT journalEntry
            FROM JournalEntry journalEntry
            LEFT JOIN FETCH journalEntry.attachments attachments
            WHERE journalEntry.trip.id = :tripId
              AND journalEntry.recordDate = :recordDate
            ORDER BY journalEntry.createdAt ASC
            """)
    List<JournalEntry> findByTripIdAndRecordDateOrderByCreatedAtAsc(
            @Param("tripId") Long tripId,
            @Param("recordDate") LocalDate recordDate
    );

    @Query("""
            SELECT DISTINCT journalEntry
            FROM JournalEntry journalEntry
            JOIN journalEntry.trip trip
            JOIN trip.user user
            LEFT JOIN FETCH journalEntry.attachments attachments
            WHERE journalEntry.id = :entryId
              AND user.email = :email
            """)
    Optional<JournalEntry> findByIdAndUserEmail(
            @Param("entryId") Long entryId,
            @Param("email") String email
    );
}
