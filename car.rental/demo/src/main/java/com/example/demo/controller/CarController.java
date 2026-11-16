package com.example.demo.controller;

import com.example.demo.entity.Car;
import com.example.demo.service.CarService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cars")
public class CarController {

    private static final List<String> STATUSES = List.of(Car.STATUS_AVAILABLE, Car.STATUS_UNAVAILABLE);

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("cars", carService.findAll());
        return "cars/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("car", new Car());
        model.addAttribute("statuses", STATUSES);
        return "cars/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("car", carService.findById(id));
        model.addAttribute("statuses", STATUSES);
        return "cars/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("car") Car car,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", STATUSES);
            return "cars/form";
        }
        carService.save(car);
        redirect.addFlashAttribute("success", "Auto salvestatud.");
        return "redirect:/cars";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            carService.delete(id);
            redirect.addFlashAttribute("success", "Auto kustutatud.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cars";
    }
}
