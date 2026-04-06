package com.costrip.costrip_backend.service;

import com.costrip.costrip_backend.dto.trip.TripRequestDto;
import com.costrip.costrip_backend.dto.trip.TripResponseDto;
import com.costrip.costrip_backend.entity.Attachment;
import com.costrip.costrip_backend.entity.ExpenseBudget;
import com.costrip.costrip_backend.entity.Trip;
import com.costrip.costrip_backend.entity.User;
import com.costrip.costrip_backend.repository.AttachmentRepository;
import com.costrip.costrip_backend.repository.ExpenseBudgetRepository;
import com.costrip.costrip_backend.repository.ExpenseRepository;
import com.costrip.costrip_backend.repository.TripRepository;
import com.costrip.costrip_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseBudgetRepository expenseBudgetRepository;
    private final AttachmentRepository attachmentRepository;

    /**
     * 로그인한 사용자의 여행 목록을 조회하고, 각 여행의 첫 메모 이미지를 썸네일로 내려준다.
     */
    public List<TripResponseDto> getTripsByUser(String email) {
        User user = findUserByEmail(email);
        List<Trip> trips = tripRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return trips.stream()
                .map(trip -> {
                    BigDecimal totalSpent = expenseRepository.sumAmountByTrip(trip.getId());
                    BigDecimal totalBudget = expenseBudgetRepository.sumBudgetByTripId(trip.getId());
                    String thumbnailPath = findThumbnailPath(trip.getId());

                    return TripResponseDto.from(trip, totalBudget, totalSpent, thumbnailPath);
                })
                .toList();
    }

    /**
     * 여행 상세 조회 시에도 같은 썸네일 규칙을 적용한다.
     */
    public TripResponseDto getTripById(String email, Long tripId) {
        User user = findUserByEmail(email);
        Trip trip = findTripByIdAndUserId(tripId, user.getId());

        BigDecimal totalSpent = expenseRepository.sumAmountByTrip(trip.getId());
        BigDecimal totalBudget = expenseBudgetRepository.sumBudgetByTripId(trip.getId());
        String thumbnailPath = findThumbnailPath(trip.getId());

        return TripResponseDto.from(trip, totalBudget, totalSpent, thumbnailPath);
    }

    /**
     * 여행을 생성한다.
     */
    @Transactional
    public TripResponseDto createTrip(String email, TripRequestDto dto) {
        User user = findUserByEmail(email);
        validateDateRange(dto.getStartDate(), dto.getEndDate());

        Trip trip = Trip.builder()
                .user(user)
                .title(dto.getTitle())
                .country(dto.getCountry())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();

        tripRepository.save(trip);

        if (dto.getBudgets() != null) {
            List<ExpenseBudget> budgets = dto.getBudgets().stream()
                    .map(b -> new ExpenseBudget(trip, b.getCategory(), b.getAmount()))
                    .toList();

            expenseBudgetRepository.saveAll(budgets);
        }

        return TripResponseDto.from(trip, null);
    }

    /**
     * 여행 기본 정보를 수정한다.
     */
    @Transactional
    public TripResponseDto updateTrip(String email, Long tripId, TripRequestDto dto) {
        User user = findUserByEmail(email);
        Trip trip = findTripByIdAndUserId(tripId, user.getId());

        validateDateRange(dto.getStartDate(), dto.getEndDate());

        trip.setTitle(dto.getTitle());
        trip.setCountry(dto.getCountry());
        trip.setStartDate(dto.getStartDate());
        trip.setEndDate(dto.getEndDate());

        if (dto.getBudgets() != null) {
            expenseBudgetRepository.deleteByTrip(trip);  // 기존 예산 삭제

            List<ExpenseBudget> budgets = dto.getBudgets().stream()
                    .map(b -> new ExpenseBudget(trip, b.getCategory(), b.getAmount()))
                    .toList();
            expenseBudgetRepository.saveAll(budgets);    // 새 예산 저장
        }

        String thumbnailPath = findThumbnailPath(trip.getId());
        return TripResponseDto.from(trip, thumbnailPath);
    }

    /**
     * 여행을 삭제한다.
     */
    @Transactional
    public void deleteTrip(String email, Long tripId) {
        User user = findUserByEmail(email);
        Trip trip = findTripByIdAndUserId(tripId, user.getId());
        tripRepository.delete(trip);
    }

    /**
     * 첫 메모 이미지 한 장을 여행 썸네일로 사용한다.
     */
    private String findThumbnailPath(Long tripId) {
        return attachmentRepository.findThumbnailCandidatesByTripId(tripId, PageRequest.of(0, 1)).stream()
                .findFirst()
                .map(Attachment::getFilePath)
                .orElse(null);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
    }

    private Trip findTripByIdAndUserId(Long tripId, Long userId) {
        return tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없거나 접근 권한이 없습니다."));
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");
        }
    }
}
