package vn.edu.fpt.cinemamanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cinemamanagement.entities.Movie;
import vn.edu.fpt.cinemamanagement.services.MovieService;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping(value = "movies")
public class MovieController {
    @Autowired
    private MovieService movieService;

    @RequestMapping
    public String getAllMovies(Model model,  @RequestParam(name = "page", defaultValue = "1", required = false) int page) {
        int size = 10;
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Movie> movies = movieService.getAllMovies(pageable);
        model.addAttribute("movies", movies);
        int totalPages = movies.getTotalPages();

        int visiblePages = 5;
        int startPage, endPage;
        if (totalPages <= visiblePages) {
            startPage = 1; // 1-based
            endPage = totalPages;
        } else {
            startPage = ((page - 1) / visiblePages) * visiblePages + 1;
            endPage = Math.min(startPage + visiblePages - 1, totalPages);
        }
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        if (movies.isEmpty()) {
            model.addAttribute("error", "Movie list is empty");
        }
        return "movies/movie_list";
    }
    @RequestMapping(value = "/detail/{id}")
    public String getMovieDetails(@PathVariable("id") String id, Model model) {
        Movie movie = movieService.findById(id);
        model.addAttribute("movie", movie);
        if (!movieService.existsByMovieID(id)) {
            model.addAttribute("error", String.format("Movie with ID %s does not exist", id));
        }
        return "movies/movie_detail";
    }

    @RequestMapping(value = "createMovie")
    public String createMovie(Model model) {
        Movie movie = new Movie();
        movie.setMovieID(movieService.generateMovieID());
        model.addAttribute("movie", movie);

        List<String> imgPaths = movieService.getImgPaths();
        model.addAttribute("imgPaths", imgPaths);
        model.addAttribute("errors", new HashMap<String, String>());
        model.addAttribute("genres", movieService.getGenres());
        return "movies/movie_create";
    }
    @RequestMapping(value = "/update/{id}")
    public String updateMovie(@PathVariable("id") String id, Model model) {
         model.addAttribute("movie", movieService.findById(id));
        if (!movieService.existsByMovieID(id)) {
            model.addAttribute("error", String.format("Movie with ID %s does not exist", id));
        }
        List<String> imgPaths = movieService.getImgPaths();
        model.addAttribute("imgPaths", imgPaths);
        model.addAttribute("errors", new HashMap<String, String>());
        model.addAttribute("genres", movieService.getGenres());
        return "movies/movie_update";
    }

@PostMapping(value = "create")
public String create(@ModelAttribute("movie") Movie movie, Model model) {
    var errors = movieService.createMovie(movie);

    if (!errors.isEmpty()) {
        model.addAttribute("movie", movie); // ⚠️ cần có dòng này
        model.addAttribute("errors", errors);
        model.addAttribute("imgPaths", movieService.getImgPaths());
        model.addAttribute("genres", movieService.getGenres());

        return  "movies/movie_create";
    }

    return "redirect:/movies";
}

    @PostMapping(value = "update")
    public String update(@ModelAttribute("movie") Movie movie, Model model) {
        var errors = movieService.updateMovie(movie);

        if (!errors.isEmpty()) {
            model.addAttribute("movie", movie); // ⚠️ cần có dòng này
            model.addAttribute("errors", errors);
            model.addAttribute("imgPaths", movieService.getImgPaths());
            model.addAttribute("genres", movieService.getGenres());

            return "movies/movie_update";
        }

        return "redirect:/movies";
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable("id") String id) {
        movieService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
