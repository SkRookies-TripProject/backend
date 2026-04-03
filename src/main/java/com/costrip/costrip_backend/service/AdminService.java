package com.costrip.costrip_backend.service;

import com.costrip.costrip_backend.dto.auth.AdminDashboardResponseDto;
import com.costrip.costrip_backend.repository.ExpenseRepository;
import com.costrip.costrip_backend.repository.TripRepository;
import com.costrip.costrip_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final ExpenseRepository expenseRepository;

    public AdminDashboardResponseDto getDashboard() {

        long totalUsers = userRepository.count();
        long totalTrips = tripRepository.count();
        BigDecimal totalExpenseAmount = expenseRepository.getTotalAmount();

        return new AdminDashboardResponseDto(
                totalUsers,
                totalTrips,
                totalExpenseAmount
        );
    }
}
