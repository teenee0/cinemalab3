package com.example.cinemalab3.Controllers;

import com.example.cinemalab3.Models.Films;
import com.example.cinemalab3.Repository.FilmsRepository;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class MainController {
    private final FilmsRepository filmsRepository;

    public MainController(FilmsRepository filmsRepository) {
        this.filmsRepository = filmsRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Главная Страница");
        return "greeting";
    }
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "Страница входа");
        return "login";
    }
    @GetMapping("/support")
    public String support(Model model) {
        model.addAttribute("title", "Поддержка");
        return "support";
    }
    @GetMapping("/films")
    public String films(Model model) {
        Iterable<Films> film = filmsRepository.findAll();
        model.addAttribute("title","Страница сеансов");
        model.addAttribute("films", film);
        return "films";
    }
    @GetMapping("/addfilm")
    public String addFilm(Model model) {
        model.addAttribute("title","Страница добавления фильма");
        return "addfilm";
    }

    @GetMapping("/films/{id}")
    public String updateFilm(@PathVariable long id, Model model) {
        if (!filmsRepository.existsById(id)){
            return "redirect:/films";
        }
        Optional<Films> film = filmsRepository.findById(id);
        ArrayList<Films> res = new ArrayList<>();
        film.ifPresent(res::add);
        model.addAttribute("films", res);
        model.addAttribute("title", "Страница редактирования");
        return "filmDetails";

    }

    @GetMapping("/films/filter")
    public String searchFilms(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String studio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime sessionDateTime,
            @RequestParam(required = false) Integer ticketCount,
            @RequestParam(required = false, defaultValue = "asc") String sort,
            Model model) {

        Sort.Direction sortDirection = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortBy = Sort.by(sortDirection, "sessionDateTime");

        List<Films> films;

        if (title != null || studio != null || sessionDateTime != null || ticketCount != null) {
            films = filmsRepository.findByParams(title, studio, sessionDateTime, ticketCount, sortBy);
        } else {
            films = filmsRepository.findAll(sortBy);
        }

        model.addAttribute("films", films);
        return "films";
    }

    @GetMapping("/films/stats")
    public String stats(Model model) {
        List<Object[]> stats = filmsRepository.findFilmIssueStats();

        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        for (Object[] row : stats) {
            dates.add(row[0].toString());
            counts.add((Long) row[1]);
        }

        model.addAttribute("dates", dates);
        model.addAttribute("counts", counts);
        return "film_stats";
    }



    @PreAuthorize("isAuthenticated()")
    @PostMapping("/addfilm")
    public String addFilm(@RequestParam String title,
                          @RequestParam String studio,
                          @RequestParam LocalDateTime sessionDateTime,
                          @RequestParam int ticketCount,
                          Model model) {
        Films film = new Films(title, studio, sessionDateTime, ticketCount);
        filmsRepository.save(film);
        return "redirect:/films";

    }

    @PostMapping("/films/save")
    public String saveFils(
            @RequestParam("id") long id,
            @RequestParam String title,
            @RequestParam String studio,
            @RequestParam LocalDateTime sessionDateTime,
            @RequestParam int ticketCount,
            Model model) {
        Films film = filmsRepository.findById(id).orElseThrow();
        film.setTitle(title);
        film.setStudio(studio);
        film.setSessionDateTime(sessionDateTime);
        film.setTicketCount(ticketCount);
        filmsRepository.save(film);
        return "redirect:/films";

    }

    @PostMapping("/films/{id}/remove")
    public String removeFilm(@PathVariable long id, Model model) {
        Films film = filmsRepository.findById(id).orElseThrow();
        filmsRepository.delete(film);
        return "redirect:/films";
    }






}
