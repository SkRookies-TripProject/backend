package com.costrip.costrip_backend.service;

import com.costrip.costrip_backend.dto.trip.TripResponseDto;
import com.costrip.costrip_backend.entity.Attachment;
import com.costrip.costrip_backend.entity.Trip;
import com.costrip.costrip_backend.entity.User;
import com.costrip.costrip_backend.repository.AttachmentRepository;
import com.costrip.costrip_backend.repository.ExpenseBudgetRepository;
import com.costrip.costrip_backend.repository.ExpenseRepository;
import com.costrip.costrip_backend.repository.TripRepository;
import com.costrip.costrip_backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseBudgetRepository expenseBudgetRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private TripService tripService;

    @Test
    @DisplayName("여행 목록 응답에 대표 썸네일 경로가 포함된다")
    void getTripsByUser_includesThumbnailPath() {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .password("password")
                .name("tester")
                .role("USER")
                .build();

        Trip trip = Trip.builder()
                .id(10L)
                .user(user)
                .title("Tokyo")
                .country("Japan")
                .startDate(LocalDate.of(2026, 4, 4))
                .endDate(LocalDate.of(2026, 4, 6))
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tripRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(trip));
        when(expenseRepository.sumAmountByTrip(10L)).thenReturn(BigDecimal.valueOf(10000));
        when(expenseBudgetRepository.sumBudgetByTripId(10L)).thenReturn(BigDecimal.valueOf(50000));
        when(attachmentRepository.findThumbnailCandidatesByTripId(eq(10L), any(Pageable.class)))
                .thenReturn(List.of(Attachment.builder()
                        .filePath("/uploads/journal/thumbnail.jpg")
                        .build()));

        List<TripResponseDto> result = tripService.getTripsByUser("user@example.com");

        assertThat(result)
                .singleElement()
                .satisfies(response -> assertThat(response.getThumbnailPath())
                        .isEqualTo("/uploads/journal/thumbnail.jpg"));
    }

    @Test
    @DisplayName("대표 썸네일 후보가 없으면 null을 반환한다")
    void getTripById_returnsNullThumbnailWhenNoImagesExist() {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .password("password")
                .name("tester")
                .role("USER")
                .build();

        Trip trip = Trip.builder()
                .id(10L)
                .user(user)
                .title("Osaka")
                .country("Japan")
                .startDate(LocalDate.of(2026, 4, 4))
                .endDate(LocalDate.of(2026, 4, 6))
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tripRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(trip));
        when(expenseRepository.sumAmountByTrip(10L)).thenReturn(BigDecimal.ZERO);
        when(expenseBudgetRepository.sumBudgetByTripId(10L)).thenReturn(BigDecimal.ZERO);
        when(attachmentRepository.findThumbnailCandidatesByTripId(eq(10L), any(Pageable.class)))
                .thenReturn(List.of());

        TripResponseDto result = tripService.getTripById("user@example.com", 10L);

        assertThat(result.getThumbnailPath()).isNull();
    }
}
