package com.bms.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bms.dto.BookingResponse;
import com.bms.entity.Booking;
import com.bms.entity.BookingSeat;
import com.bms.entity.Payment;
import com.bms.entity.User;
import com.bms.repository.BookingRepository;
import com.bms.repository.BookingSeatRepository;
import com.bms.repository.UserRepository;
import com.bms.service.BookingService;

@Controller
public class PageBookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final UserRepository userRepository;

    public PageBookingController(
            BookingService bookingService,
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            UserRepository userRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/my-bookings")
    public String myBookings(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings = bookingRepository.findByUserIdWithShowAndEvent(user.getId());
        model.addAttribute("bookings", bookings);
        return "my-bookings";
    }

    @GetMapping("/bookings/{id}")
    public String bookingDetail(@PathVariable Long id, Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingRepository.findByIdWithShowAndEvent(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Ensure user owns this booking (or is admin)
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!booking.getUser().getId().equals(user.getId()) && !isAdmin) {
            throw new RuntimeException("Access denied");
        }

        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(id);

        model.addAttribute("booking", booking);
        model.addAttribute("bookingSeats", bookingSeats);
        return "booking-detail";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cancel failed: " + e.getMessage());
        }

        return "redirect:/bookings/" + id;
    }

    @PostMapping("/bookings/{id}/pay")
    public String payBooking(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Payment payment = bookingService.processPayment(id);
            if (payment.getStatus().name().equals("SUCCESS")) {
                redirectAttributes.addFlashAttribute("success", "Payment successful! Booking confirmed.");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Payment failed. Booking has been cancelled. Please try booking again.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Payment error: " + e.getMessage());
        }

        return "redirect:/bookings/" + id;
    }
}
