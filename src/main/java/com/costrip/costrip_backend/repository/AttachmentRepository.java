package com.costrip.costrip_backend.repository;

import com.costrip.costrip_backend.entity.Attachment;
import com.costrip.costrip_backend.entity.Trip;
import com.costrip.costrip_backend.entity.journal.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByTripOrderByCreatedAtAsc(Trip trip);

    long countByTrip(Trip trip);

    List<Attachment> findByJournalEntryOrderByCreatedAtAsc(JournalEntry journalEntry);

    @Query("""
            SELECT attachment
            FROM Attachment attachment
            JOIN attachment.journalEntry journalEntry
            JOIN journalEntry.trip trip
            JOIN trip.user user
            WHERE attachment.id = :attachmentId
              AND user.email = :email
            """)
    Optional<Attachment> findByIdAndUserEmail(
            @Param("attachmentId") Long attachmentId,
            @Param("email") String email
    );
}
