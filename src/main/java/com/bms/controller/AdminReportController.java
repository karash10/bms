package com.bms.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bms.dto.EventBookingReport;
import com.bms.dto.PopularEventReport;
import com.bms.dto.RevenueReport;
import com.bms.dto.SeatOccupancyReport;
import com.bms.service.ReportService;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/bookings")
    public ResponseEntity<Map<String, Object>> bookingsReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("totalBookings", reportService.getTotalBookings());
        report.put("bookingsPerEvent", reportService.getBookingsPerEvent());
        return ResponseEntity.ok(report);
    }

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> revenueReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("totalRevenue", reportService.getTotalRevenue());
        report.put("revenuePerEvent", reportService.getRevenuePerEvent());
        return ResponseEntity.ok(report);
    }

    @GetMapping("/popular-events")
    public ResponseEntity<List<PopularEventReport>> popularEventsReport() {
        return ResponseEntity.ok(reportService.getPopularEvents());
    }

    @GetMapping("/seat-occupancy")
    public ResponseEntity<List<SeatOccupancyReport>> seatOccupancyReport() {
        return ResponseEntity.ok(reportService.getSeatOccupancy());
    }
}
