package com.costrip.costrip_backend.service;

import com.costrip.costrip_backend.dto.expense.ExpenseBudgetRequestDto;
import com.costrip.costrip_backend.dto.expense.ExpenseRequestDto;
import com.costrip.costrip_backend.dto.trip.TripRequestDto;
import com.costrip.costrip_backend.dto.trip.TripResponseDto;
import com.costrip.costrip_backend.entity.Expense;
import com.costrip.costrip_backend.entity.ExpenseBudget;
import com.costrip.costrip_backend.entity.Trip;
import com.costrip.costrip_backend.entity.User;
import com.costrip.costrip_backend.entity.enums.TripStatus;
import com.costrip.costrip_backend.repository.ExpenseBudgetRepository;
import com.costrip.costrip_backend.repository.ExpenseRepository;
import com.costrip.costrip_backend.repository.TripRepository;
import com.costrip.costrip_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseBudgetRepository expenseBudgetRepository;

    /**
     * 여행 목록 조회
     */
    public List<TripResponseDto> getTripsByUser(String email) {

        User user = findUserByEmail(email);

        List<Trip> trips =
                tripRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return trips.stream()
                .map(trip -> {

                    BigDecimal totalSpent =
                            expenseRepository.sumAmountByTrip(trip.getId());

                    BigDecimal totalBudget =
                            expenseBudgetRepository.sumBudgetByTripId(trip.getId());

                    return TripResponseDto.from(trip, totalBudget, totalSpent);
                })
                .toList();
    }

    /**
     * 여행 상세 조회
     */
    public TripResponseDto getTripById(String email, Long tripId) {

        User user = findUserByEmail(email);
        Trip trip = findTripByIdAndUserId(tripId, user.getId());

        BigDecimal totalSpent =
                expenseRepository.sumAmountByTrip(trip.getId());

        BigDecimal totalBudget =
                expenseBudgetRepository.sumBudgetByTripId(trip.getId());

        return TripResponseDto.from(trip, totalBudget, totalSpent);
    }

    /**
     * 여행 등록
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

        //
        if (dto.getBudgets() != null) {
            List<ExpenseBudget> budgets = dto.getBudgets().stream()
                    .map(b -> new ExpenseBudget(trip, b.getCategory(), b.getAmount()))
                    .toList();

            expenseBudgetRepository.saveAll(budgets);
        }

        return TripResponseDto.from(trip);
    }

    /**
     * 여행 수정
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

        return TripResponseDto.from(trip);
    }

    /**
     * 여행 삭제
     */
    @Transactional
    public void deleteTrip(String email, Long tripId) {

        User user = findUserByEmail(email);
        Trip trip = findTripByIdAndUserId(tripId, user.getId());

        tripRepository.delete(trip);
    }

    // ── 헬퍼 ─────────────────────────

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
