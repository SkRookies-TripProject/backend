package com.costrip.costrip_backend.repository;

import com.costrip.costrip_backend.entity.Attachment;
import com.costrip.costrip_backend.entity.Trip;
import com.costrip.costrip_backend.entity.User;
import com.costrip.costrip_backend.entity.journal.JournalEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AttachmentRepositoryTest {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("대표 썸네일은 가장 먼저 생성된 메모의 첫 이미지다")
    void findThumbnailCandidatesByTripId_returnsFirstImageOfEarliestJournalEntry() {
        Trip trip = persistTrip("first-journal");

        JournalEntry firstEntry = persistJournalEntry(trip, "first");
        JournalEntry secondEntry = persistJournalEntry(trip, "second");

        setJournalEntryCreatedAt(firstEntry.getId(), LocalDateTime.of(2026, 4, 1, 10, 0));
        setJournalEntryCreatedAt(secondEntry.getId(), LocalDateTime.of(2026, 4, 1, 11, 0));

        Attachment firstEntryFirstImage = persistAttachment(trip, firstEntry, "/uploads/journal/first-entry-first.jpg");
        Attachment firstEntrySecondImage = persistAttachment(trip, firstEntry, "/uploads/journal/first-entry-second.jpg");
        Attachment secondEntryFirstImage = persistAttachment(trip, secondEntry, "/uploads/journal/second-entry-first.jpg");

        setAttachmentCreatedAt(firstEntryFirstImage.getId(), LocalDateTime.of(2026, 4, 1, 12, 0));
        setAttachmentCreatedAt(firstEntrySecondImage.getId(), LocalDateTime.of(2026, 4, 1, 12, 30));
        setAttachmentCreatedAt(secondEntryFirstImage.getId(), LocalDateTime.of(2026, 4, 1, 11, 5));

        entityManager.clear();

        List<Attachment> thumbnails = attachmentRepository.findThumbnailCandidatesByTripId(
                trip.getId(),
                PageRequest.of(0, 1)
        );

        assertThat(thumbnails)
                .singleElement()
                .extracting(Attachment::getFilePath)
                .isEqualTo("/uploads/journal/first-entry-first.jpg");
    }

    @Test
    @DisplayName("대표 이미지가 삭제되면 다음 등록순 이미지가 대표 썸네일이 된다")
    void findThumbnailCandidatesByTripId_skipsDeletedRepresentativeAttachment() {
        Trip trip = persistTrip("deleted-thumbnail");

        JournalEntry firstEntry = persistJournalEntry(trip, "first");
        JournalEntry secondEntry = persistJournalEntry(trip, "second");

        setJournalEntryCreatedAt(firstEntry.getId(), LocalDateTime.of(2026, 4, 2, 9, 0));
        setJournalEntryCreatedAt(secondEntry.getId(), LocalDateTime.of(2026, 4, 2, 10, 0));

        Attachment firstImage = persistAttachment(trip, firstEntry, "/uploads/journal/first-image.jpg");
        Attachment secondImage = persistAttachment(trip, firstEntry, "/uploads/journal/second-image.jpg");
        Attachment thirdImage = persistAttachment(trip, secondEntry, "/uploads/journal/third-image.jpg");

        setAttachmentCreatedAt(firstImage.getId(), LocalDateTime.of(2026, 4, 2, 9, 10));
        setAttachmentCreatedAt(secondImage.getId(), LocalDateTime.of(2026, 4, 2, 9, 20));
        setAttachmentCreatedAt(thirdImage.getId(), LocalDateTime.of(2026, 4, 2, 10, 10));

        attachmentRepository.deleteById(firstImage.getId());
        entityManager.flush();
        entityManager.clear();

        List<Attachment> thumbnails = attachmentRepository.findThumbnailCandidatesByTripId(
                trip.getId(),
                PageRequest.of(0, 1)
        );

        assertThat(thumbnails)
                .singleElement()
                .extracting(Attachment::getFilePath)
                .isEqualTo("/uploads/journal/second-image.jpg");
    }

    @Test
    @DisplayName("여행 전체에 이미지가 없으면 대표 썸네일은 없다")
    void findThumbnailCandidatesByTripId_returnsEmptyWhenTripHasNoImages() {
        Trip trip = persistTrip("no-thumbnail");
        JournalEntry entry = persistJournalEntry(trip, "memo only");
        setJournalEntryCreatedAt(entry.getId(), LocalDateTime.of(2026, 4, 3, 8, 0));
        entityManager.clear();

        List<Attachment> thumbnails = attachmentRepository.findThumbnailCandidatesByTripId(
                trip.getId(),
                PageRequest.of(0, 1)
        );

        assertThat(thumbnails).isEmpty();
    }

    private Trip persistTrip(String title) {
        User user = entityManager.persistAndFlush(User.builder()
                .email(title + "@example.com")
                .password("password")
                .name("tester")
                .role("USER")
                .build());

        Trip trip = entityManager.persistAndFlush(Trip.builder()
                .user(user)
                .title(title)
                .country("Japan")
                .startDate(LocalDate.of(2026, 4, 1))
                .endDate(LocalDate.of(2026, 4, 5))
                .build());

        entityManager.clear();
        return trip;
    }

    private JournalEntry persistJournalEntry(Trip trip, String memo) {
        return entityManager.persistAndFlush(JournalEntry.builder()
                .trip(entityManager.getEntityManager().getReference(Trip.class, trip.getId()))
                .recordDate(LocalDate.of(2026, 4, 1))
                .memo(memo)
                .build());
    }

    private Attachment persistAttachment(Trip trip, JournalEntry entry, String filePath) {
        return entityManager.persistAndFlush(Attachment.builder()
                .trip(entityManager.getEntityManager().getReference(Trip.class, trip.getId()))
                .journalEntry(entityManager.getEntityManager().getReference(JournalEntry.class, entry.getId()))
                .fileName(filePath.substring(filePath.lastIndexOf('/') + 1))
                .filePath(filePath)
                .fileType("image/jpeg")
                .build());
    }

    private void setJournalEntryCreatedAt(Long entryId, LocalDateTime createdAt) {
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE journal_entries SET created_at = ?, updated_at = ? WHERE id = ?")
                .setParameter(1, Timestamp.valueOf(createdAt))
                .setParameter(2, Timestamp.valueOf(createdAt))
                .setParameter(3, entryId)
                .executeUpdate();
        entityManager.flush();
    }

    private void setAttachmentCreatedAt(Long attachmentId, LocalDateTime createdAt) {
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE attachments SET created_at = ? WHERE id = ?")
                .setParameter(1, Timestamp.valueOf(createdAt))
                .setParameter(2, attachmentId)
                .executeUpdate();
        entityManager.flush();
    }
}
