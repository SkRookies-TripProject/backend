package com.costrip.costrip_backend.service;

import com.costrip.costrip_backend.dto.trip.TripRequestDto;
import com.costrip.costrip_backend.dto.trip.TripResponseDto;
import com.costrip.costrip_backend.entity.Trip;
import com.costrip.costrip_backend.entity.User;
import com.costrip.costrip_backend.entity.enums.TripStatus;
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

    /**
     * 여행 목록 조회
     * - status 파라미터가 있으면 상태별 필터링
     */
    public List<TripResponseDto> getTripsByUser(String email, TripStatus status) {
        User user = findUserByEmail(email);

        List<Trip> trips = (status != null)
                ? tripRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status)
                : tripRepository.findByUserOrderByCreatedAtDesc(user);

        return trips.stream()
                .map(trip -> {
                    BigDecimal totalSpent = tripRepository.sumExpenseAmountByTripId(trip.getId());
                    return TripResponseDto.from(trip, totalSpent);
                })
                .collect(Collectors.toList());
    }

    /**
     * 여행 상세 조회
     * - 본인 여행인지 확인 포함
     */
    public TripResponseDto getTripById(String email, Long tripId) {
        User user = findUserByEmail(email);
        Trip trip = findTripByIdAndUser(tripId, user);
        BigDecimal totalSpent = tripRepository.sumExpenseAmountByTripId(trip.getId());
        return TripResponseDto.from(trip, totalSpent);
    }

    /**
     * 여행 등록
     * - 종료일이 시작일 이전이면 예외
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
                .budget(dto.getBudget())
                .status(dto.getStatus() != null ? dto.getStatus() : TripStatus.PLANNED)
                .build();

        tripRepository.save(trip);
        return TripResponseDto.from(trip);
    }

    /**
     * 여행 수정
     * - 본인 여행만 수정 가능
     */
    @Transactional
    public TripResponseDto updateTrip(String email, Long tripId, TripRequestDto dto) {
        User user = findUserByEmail(email);
        Trip trip = findTripByIdAndUser(tripId, user);
        validateDateRange(dto.getStartDate(), dto.getEndDate());

        trip.setTitle(dto.getTitle());
        trip.setCountry(dto.getCountry());
        trip.setStartDate(dto.getStartDate());
        trip.setEndDate(dto.getEndDate());
        trip.setBudget(dto.getBudget());
        if (dto.getStatus() != null) {
            trip.setStatus(dto.getStatus());
        }

        return TripResponseDto.from(trip);
    }

    /**
     * 여행 삭제
     * - 본인 여행만 삭제 가능 (CascadeType.ALL로 지출도 함께 삭제됨)
     */
    @Transactional
    public void deleteTrip(String email, Long tripId) {
        User user = findUserByEmail(email);
        Trip trip = findTripByIdAndUser(tripId, user);
        tripRepository.delete(trip);
    }

    // ── 내부 헬퍼 ──────────────────────────────────────────────────────────────

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
    }

    private Trip findTripByIdAndUser(Long tripId, User user) {
        return tripRepository.findByIdAndUser(tripId, user)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없거나 접근 권한이 없습니다."));
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");
        }
    }
}
