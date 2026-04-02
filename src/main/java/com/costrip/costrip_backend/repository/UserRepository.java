package com.costrip.costrip_backend.repository;

import com.costrip.costrip_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 조회 (로그인, 중복 검사)
    Optional<User> findByEmail(String email);

    // 이메일 중복 확인
    boolean existsByEmail(String email);

    /* 관리자 기능용 */
    // 이름 검색
    List<User> findByNameContaining(String name);

    // 이메일 검색
    List<User> findByEmailContaining(String email);

    // 이름 OR 이메일 검색 (핵심)
    List<User> findByNameContainingOrEmailContaining(String name, String email);
}
