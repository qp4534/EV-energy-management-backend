package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.dashboard.AccountStatusTrendDto;
import com.ev_energy_management.backend.dto.dashboard.CarModelDistributionDto;
import com.ev_energy_management.backend.dto.dashboard.MemberFlowDto;
import com.ev_energy_management.backend.dto.dashboard.UserRoleDistributionDto;
import com.ev_energy_management.backend.service.CarService;
import com.ev_energy_management.backend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 관리자 메인 화면(AdministratorMain.jsx) 전용 조회 엔드포인트.
// 별도 Service 없이 이미 있는 UserService/CarService의 메서드를 그대로 가져다 씀
// (차량 모델 분포는 Car 도메인, 나머지는 User 도메인 데이터라서 각 서비스에 귀속시킴).
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final UserService userService;
    private final CarService carService;

    public DashboardController(UserService userService, CarService carService) {
        this.userService = userService;
        this.carService = carService;
    }

    @GetMapping("/car-model-distribution")
    public List<CarModelDistributionDto> getCarModelDistribution() {
        return carService.getModelDistribution();
    }

    @GetMapping("/user-role-distribution")
    public List<UserRoleDistributionDto> getUserRoleDistribution() {
        return userService.getRoleDistribution();
    }

    @GetMapping("/member-flow")
    public List<MemberFlowDto> getMemberFlow() {
        return userService.getMemberFlow();
    }

    @GetMapping("/account-status-trend")
    public List<AccountStatusTrendDto> getAccountStatusTrend() {
        return userService.getAccountStatusTrend();
    }
}