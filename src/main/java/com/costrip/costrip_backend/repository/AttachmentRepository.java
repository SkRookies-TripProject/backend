package com.costrip.costrip_backend.repository;

import com.costrip.costrip_backend.entity.Attachment;
import com.costrip.costrip_backend.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    //  여행 기준 첨부파일 목록
    List<Attachment> findByTripOrderByCreatedAtAsc(Trip trip);

    //  여행 기준 첨부파일 개수
    long countByTrip(Trip trip);
}
