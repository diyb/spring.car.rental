package com.example.demo.controller;

import com.example.demo.dto.BookingForm;
import com.example.demo.service.BookingService;
import com.example.demo.service.CarService;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final CarService carService;
    private final UserService userService;

    public BookingController(BookingService bookingService,
                             CarService carService,
                             UserService userService) {
        this.bookingService = bookingService;
        this.carService = carService;
        this.userService = userService;
    }

    /** Administraator näeb kõiki broneeringuid, klient ainult enda omi. */
    @GetMapping
    public String list(Authentication auth, Model model) {
        if (isAdmin(auth)) {
            model.addAttribute("bookings", bookingService.findAll());
        } else {
            model.addAttribute("bookings", bookingService.findByUser(userService.idFor(auth.getName())));
        }
        return "bookings/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        BookingForm form = new BookingForm();
        form.setStartDate(LocalDate.now());
        form.setEndDate(LocalDate.now().plusDays(1));
        model.addAttribute("bookingForm", form);
        model.addAttribute("cars", carService.findAvailable());
        return "bookings/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("bookingForm") BookingForm form,
                       BindingResult result,
                       Authentication auth,
                       Model model,
                       RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                var booking = bookingService.create(form, userService.idFor(auth.getName()));
                redirect.addFlashAttribute("success", "Broneering loodud. Koguhind: " + booking.getTotalPrice() + " €");
                return "redirect:/bookings";
            } catch (IllegalArgumentException | NoSuchElementException e) {
                result.reject("booking.invalid", e.getMessage());
            }
        }
        model.addAttribute("cars", carService.findAvailable());
        return "bookings/form";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, Authentication auth, RedirectAttributes redirect) {
        try {
            Long userId = isAdmin(auth) ? null : userService.idFor(auth.getName());
            bookingService.cancel(id, userId);
            redirect.addFlashAttribute("success", "Broneering tühistatud.");
        } catch (NoSuchElementException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bookings";
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
