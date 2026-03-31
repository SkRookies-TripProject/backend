import com.costrip.costrip_backend.dto.trip.TripRequestDto;
import com.costrip.costrip_backend.dto.trip.TripResponseDto;
import com.costrip.costrip_backend.entity.enums.TripStatus;

import java.util.List;

public interface TripService {

    // 여행 생성
    TripResponseDto createTrip(String username, TripRequestDto requestDto);

    // 여행 단건 조회
    TripResponseDto getTripById(String username, Long tripId);

    // 여행 목록 조회 (상태 필터)
    List<TripResponseDto> getTripsByUser(String username, TripStatus status);

    // 여행 수정
    TripResponseDto updateTrip(String username, Long tripId, TripRequestDto requestDto);

    // 여행 삭제
    void deleteTrip(String username, Long tripId);
}