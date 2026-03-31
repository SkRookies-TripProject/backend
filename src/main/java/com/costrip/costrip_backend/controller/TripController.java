package com.costrip.costrip_backend.controller;

import com.costrip.costrip_backend.auth.userinfo.UserInfoUserDetails;
import com.costrip.costrip_backend.dto.common.ApiResponse;
import com.costrip.costrip_backend.dto.trip.TripRequestDto;
import com.costrip.costrip_backend.dto.trip.TripResponseDto;
import com.costrip.costrip_backend.entity.enums.TripStatus;
import com.costrip.costrip_backend.service.TripService;
import com.costrip.costrip_backend.service.impl.TripServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripServiceImpl tripService;

    /**
     * GET /api/trips
     * 여행 목록 조회 (상태별 필터: PLANNED / IN_PROGRESS / COMPLETED)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TripResponseDto>>> getTrips(
            @AuthenticationPrincipal UserInfoUserDetails userDetails,
            @RequestParam(required = false) TripStatus status) {

        List<TripResponseDto> trips = tripService.getTripsByUser(userDetails.getUsername(), status);
        return ResponseEntity
                .ok(ApiResponse.success("여행 목록 조회 성공", trips));
    }

    /**
     * POST /api/trips
     * 여행 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TripResponseDto>> createTrip(
            @AuthenticationPrincipal UserInfoUserDetails userDetails,
            @Valid @RequestBody TripRequestDto requestDto) {

        TripResponseDto responseDto = tripService.createTrip(userDetails.getUsername(), requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("여행이 등록되었습니다.", responseDto));
    }

    /**
     * GET /api/trips/{tripId}
     * 여행 상세 조회
     */
    @GetMapping("/{tripId}")
    public ResponseEntity<ApiResponse<TripResponseDto>> getTrip(
            @AuthenticationPrincipal UserInfoUserDetails userDetails,
            @PathVariable Long tripId) {

        TripResponseDto responseDto = tripService.getTripById(userDetails.getUsername(), tripId);
        return ResponseEntity
                .ok(ApiResponse.success("여행 상세 조회 성공", responseDto));
    }

    /**
     * PUT /api/trips/{tripId}
     * 여행 수정 (본인 여행만 가능)
     */
    @PutMapping("/{tripId}")
    public ResponseEntity<ApiResponse<TripResponseDto>> updateTrip(
            @AuthenticationPrincipal UserInfoUserDetails userDetails,
            @PathVariable Long tripId,
            @Valid @RequestBody TripRequestDto requestDto) {

        TripResponseDto responseDto = tripService.updateTrip(userDetails.getUsername(), tripId, requestDto);
        return ResponseEntity
                .ok(ApiResponse.success("여행이 수정되었습니다.", responseDto));
    }

    /**
     * DELETE /api/trips/{tripId}
     * 여행 삭제 (본인 여행만 가능)
     */
    @DeleteMapping("/{tripId}")
    public ResponseEntity<ApiResponse<Void>> deleteTrip(
            @AuthenticationPrincipal UserInfoUserDetails userDetails,
            @PathVariable Long tripId) {

        tripService.deleteTrip(userDetails.getUsername(), tripId);
        return ResponseEntity
                .ok(ApiResponse.success("여행이 삭제되었습니다.", null));
    }
}
