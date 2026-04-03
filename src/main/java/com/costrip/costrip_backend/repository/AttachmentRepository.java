package com.costrip.costrip_backend.repository;

import com.costrip.costrip_backend.entity.Attachment;
import com.costrip.costrip_backend.entity.Trip;
import com.costrip.costrip_backend.entity.journal.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByTripOrderByCreatedAtAsc(Trip trip);

    long countByTrip(Trip trip);

    List<Attachment> findByJournalEntryOrderByCreatedAtAsc(JournalEntry journalEntry);
}
