package com.costrip.costrip_backend.repository;

import com.costrip.costrip_backend.entity.Attachment;
import com.costrip.costrip_backend.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    // 지출에 연결된 첨부파일 목록
    List<Attachment> findByExpenseOrderByCreatedAtAsc(Expense expense);

    // 지출에 연결된 첨부파일 수
    long countByExpense(Expense expense);
}
