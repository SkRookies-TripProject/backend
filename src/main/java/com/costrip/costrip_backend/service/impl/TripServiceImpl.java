package com.costrip.costrip_backend.service.impl;

import com.costrip.costrip_backend.dto.trip.TripRequestDto;
import com.costrip.costrip_backend.dto.trip.TripResponseDto;
import com.costrip.costrip_backend.entity.Trip;
import com.costrip.costrip_backend.entity.User;
import com.costrip.costrip_backend.entity.enums.TripStatus;
import com.costrip.costrip_backend.exception.ResourceNotFoundException;
import com.costrip.costrip_backend.repository.TripRepository;
import com.costrip.costrip_backend.repository.UserRepository;
import com.costrip.costrip_backend.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Override
    public TripResponseDto createTrip(String username, TripRequestDto requestDto) {
        User user = getUser(username);

        Trip trip = Trip.builder()
                .title(requestDto.getTitle())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .budget(requestDto.getBudget())
                .status(TripStatus.PLANNED)
                .user(user)
                .build();

        Trip saved = tripRepository.save(trip);
        return toDto(saved);
    }

    @Override
    public TripResponseDto getTripById(String username, Long tripId) {
        Trip trip = getOwnedTrip(username, tripId);
        return toDto(trip);
    }

    @Override
    public List<TripResponseDto> getTripsByUser(String username, TripStatus status) {
        User user = getUser(username);

        List<Trip> trips = (status == null)
                ? tripRepository.findByUser(user)
                : tripRepository.findByUserAndStatus(user, status);

        return trips.stream().map(this::toDto).toList();
    }

    @Override
    public TripResponseDto updateTrip(String username, Long tripId, TripRequestDto requestDto) {
        Trip trip = getOwnedTrip(username, tripId);

        trip.setTitle(requestDto.getTitle());
        trip.setStartDate(requestDto.getStartDate());
        trip.setEndDate(requestDto.getEndDate());
        trip.setBudget(requestDto.getBudget());

        return toDto(tripRepository.save(trip));
    }

    @Override
    public void deleteTrip(String username, Long tripId) {
        Trip trip = getOwnedTrip(username, tripId);
        tripRepository.delete(trip);
    }

    // =====================
    // 내부 메서드
    // =====================

    private User getUser(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Trip getOwnedTrip(String username, Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (!trip.getUser().getEmail().equals(username)) {
            throw new RuntimeException("접근 권한 없음");
        }
        return trip;
    }

    private TripResponseDto toDto(Trip trip) {
        return TripResponseDto.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .budget(trip.getBudget())
                .status(trip.getStatus())
                .build();
    }
}