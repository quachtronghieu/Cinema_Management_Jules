package vn.edu.fpt.cinemamanagement.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cinemamanagement.entities.Movie;
import vn.edu.fpt.cinemamanagement.repositories.MovieRatingRepository;
import vn.edu.fpt.cinemamanagement.repositories.MovieRepository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MovieService {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private MovieRatingRepository movieRatingRepository;

    // ============================================
// REGEX PATTERNS - Movie Validation
// ============================================

    // Title: cho phép chữ, số, khoảng trắng, và một số dấu câu cơ bản
//  - ^ và $: bắt đầu & kết thúc chuỗi
//  - [A-Za-z0-9\\s.,:;!?'"()-] : chỉ cho phép ký tự chữ, số, khoảng trắng và các dấu cơ bản
//  - {2,100}: độ dài từ 2 đến 100 ký tự
    private static final Pattern TITLE_PATTERN = Pattern.compile(
            "^[A-Za-z0-9\\s.,:;!?'\"()\\-]{2,100}$"
    );

    private void validateTitle(String title, Map<String, String> errors) {
        // 1️⃣ Kiểm tra null / rỗng
        if (title == null || title.trim().isEmpty()) {
            errors.put("title", "The title is required.");
            return;
        }

        // 2️⃣ Kiểm tra độ dài
        if (title.length() < 2 || title.length() > 100) {
            errors.put("title", "The title must be between 2 and 100 characters.");
            return;
        }

        if (!TITLE_PATTERN.matcher(title).matches()) {
            errors.put("title", "The title can only contain letters, numbers, spaces, and basic punctuation marks.");
        }
    }

    private void validateDuplicateTitle(String title, Map<String, String> errors) {

        boolean duplicate = movieRepository.existsByTitleIgnoreCase(title);

        // ✅ Case: create new
        if (duplicate) {
            errors.put("title", "A movie with this title already exists.");
        }
    }


    // Summary: cho phép chữ, số, dấu câu, xuống dòng (\n), tối đa 1000 ký tự
//  - Dùng DOTALL để . match cả newline
    private static final Pattern SUMMARY_PATTERN = Pattern.compile(
            "^[A-Za-z0-9\\s.,:;!?'\"()\\-\\n]{10,1000}$",
            Pattern.DOTALL
    );

    private void validateSummary(String summary, Map<String, String> errors) {
        if (summary == null || summary.trim().isEmpty()) {
            errors.put("summary", "The summary is required.");
            return;
        }

        if (!SUMMARY_PATTERN.matcher(summary).matches()) {
            errors.put("summary", "The summary must be 10–1000 characters long and can only contain letters, numbers, spaces, basic punctuation, and line breaks.");
        }
    }

    // Trailer: link YouTube hợp lệ
//  - hỗ trợ cả youtube.com và youtu.be
//  - có thể có tham số ?v= hoặc phần sau dấu /
    private static final Pattern YOUTUBE_LINK_PATTERN = Pattern.compile(
            "^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)[A-Za-z0-9_-]{11}(&\\S*)?$"
    );

    private void validateTrailer(String trailer, Map<String, String> errors) {
        if (trailer == null || trailer.trim().isEmpty()) {
            errors.put("trailer", "The trailer link is required.");
            return;
        }

        if (!YOUTUBE_LINK_PATTERN.matcher(trailer).matches()) {
            errors.put("trailer", "Please enter a valid YouTube link (e.g. https://youtu.be/XXXXXXXXXXX).");
        }
    }

    private void validateDuration(Integer duration, Map<String, String> errors) {
        if (duration == null) {
            errors.put("duration", "The duration is required.");
            return;
        }

        if (duration <= 60 || duration > 180) {
            errors.put("duration", "Duration must be between 60 and 180 minutes.");
        }
    }

    private void validateReleaseDate(LocalDate releaseDate, Map<String, String> errors, boolean isUpdate, String movieId) {
        if (releaseDate == null) {
            errors.put("releaseDate", "The release date is required.");
            return;
        }

        LocalDate today = LocalDate.now();

        int year = releaseDate.getYear();
        if (year < 1000 || year > 9999) {
            errors.put("releaseDate", "Year must be 4 digits.");
            return;
        }

        // === Cấu hình giới hạn thời gian cho tạo mới ===
        LocalDate minAllowed = today.plusWeeks(2); // ít nhất 2 tuần sau
        LocalDate maxAllowed = today.plusMonths(5);

        // Nếu sau khi cộng 5 tháng mà ngày vượt quá số ngày trong tháng mới,
        // LocalDate đã tự động cắt về cuối tháng — nên không cần xử lý thủ công


        if (!isUpdate) {
            // Khi tạo mới
            if (releaseDate.isBefore(minAllowed)) {
                errors.put("releaseDate", "The release date must be at least 2 weeks after today (" + minAllowed + ").");
            } else if (releaseDate.isAfter(maxAllowed)) {
                errors.put("releaseDate", "The release date cannot be more than 5 months from today (" + maxAllowed + ").");
            }
        } else {
            // Khi update
            Movie existing = movieRepository.findById(movieId).orElse(null);
            if (existing == null) {
                errors.put("movieId", "Movie not found for update.");
                return;
            }

            LocalDate oldReleaseDate = existing.getReleaseDate();

            // Nếu phim đã chiếu rồi → không cho đổi ngày
            if (!today.isBefore(oldReleaseDate)) {
                if (!releaseDate.equals(oldReleaseDate)) {
                    errors.put("releaseDate", "Cannot modify release date after the movie has already been released.");
                }
            } else {
                // Nếu chưa chiếu → cho đổi nhưng vẫn tuân quy tắc >= hôm nay và <= 5 tháng
                if (!releaseDate.isAfter(today)) {
                    errors.put("releaseDate", "The updated release date must be after today.");
                } else if (releaseDate.isAfter(maxAllowed)) {
                    errors.put("releaseDate", "The updated release date cannot be more than 5 months from today (" + maxAllowed + ").");
                }
            }
        }
    }

private void validateAgeRating(String ageRating, Map<String, String> errors) {
    if (ageRating == null || ageRating.trim().isEmpty()) {
        errors.put("ageRating", "The age rating is required.");
    }
}

    private void validateImage(String img, Map<String, String> errors) {
        if (img == null || img.trim().isEmpty()) {
            errors.put("img", "The movie poster image is required.");
        }
    }

    private void validateGenre(String genre, Map<String, String> errors) {
        if (genre == null || genre.trim().isEmpty()) {
            errors.put("genre", "The movie genre is required.");
        }
    }

    public List<String> getGenres() {
        return List.of(
                "Action",
                "Adventure",
                "Animation",
                "Comedy",
                "Crime",
                "Drama",
                "Fantasy",
                "Historical",
                "Horror",
                "Mystery",
                "Romance",
                "Sci-Fi",
                "Thriller",
                "War",
                "Western"
        );
    }

    public List<String> getAgeRatings() {
        return List.of(
                "P",
                "T13",
                "T16",
                "T18"
        );
    }

    public String getAgeRatingDescription(String code) {
        switch (code) {
            case "P": return "P - Suitable for all ages.";
            case "T13": return "T13 - Restricted to viewers aged 13 and above.";
            case "T16": return "T16 - Restricted to viewers aged 16 and above.";
            case "T18": return "T18 - Restricted to viewers aged 18 and above.";
            default: return "Unknown rating.";
        }
    }



    @Transactional
    public Map<String, String> createMovie(Movie movie) {
        Map<String, String> errors = new HashMap<>();

        // 1️⃣ VALIDATE FORMAT
        validateTitle(movie.getTitle(), errors);
        validateGenre(movie.getGenre(), errors);
        validateDuration(movie.getDuration(), errors);
        validateReleaseDate(movie.getReleaseDate(), errors, false, movie.getMovieID());
        validateAgeRating(movie.getAgeRating(), errors);
        validateSummary(movie.getSummary(), errors);
        validateImage(movie.getImg(), errors);
        validateTrailer(movie.getTrailer(), errors);
        validateDuplicateTitle(movie.getTitle(), errors);

        // Nếu có lỗi format thì return luôn
        if (!errors.isEmpty()) {
            return errors;
        }

        System.out.println("Checking duplicate for title: " + movie.getTitle());
        System.out.println("existsByTitleIgnoreCase result: " + movieRepository.existsByTitleIgnoreCase(movie.getTitle().toUpperCase()));

        // Nếu có lỗi business rule thì return luôn
        if (!errors.isEmpty()) {
            return errors;
        }
        movie.setTitle(movie.getTitle().toUpperCase());

        // 3️⃣ SAVE
        movieRepository.save(movie);
        return errors; // Trống = thành công
    }

    @Transactional
    public Map<String, String> updateMovie(Movie movie) {
        Map<String, String> errors = new HashMap<>();

        // 1️⃣ VALIDATE FORMAT
        validateTitle(movie.getTitle(), errors);
        validateGenre(movie.getGenre(), errors);
        validateDuration(movie.getDuration(), errors);
        validateReleaseDate(movie.getReleaseDate(), errors, true, movie.getMovieID());
        validateAgeRating(movie.getAgeRating(), errors);
        validateSummary(movie.getSummary(), errors);
        validateImage(movie.getImg(), errors);
        validateTrailer(movie.getTrailer(), errors);

        // Nếu có lỗi format thì return luôn
        if (!errors.isEmpty()) {
            return errors;
        }

        System.out.println("Checking duplicate for title: " + movie.getTitle());
        System.out.println("existsByTitleIgnoreCase result: " + movieRepository.existsByTitleIgnoreCase(movie.getTitle().toUpperCase()));

        Movie existing = movieRepository.findById(movie.getMovieID()).orElse(null);
        if (existing == null) {
            errors.put("movieID", "Movie not found for update.");
        } else {
            // Nếu title mới khác title cũ, kiểm tra trùng
            if (!movie.getTitle().equalsIgnoreCase(existing.getTitle())) {
                boolean duplicateTitle = movieRepository.existsByTitleIgnoreCase(movie.getTitle());
                if (duplicateTitle) {
                    errors.put("title", "A movie with this title already exists.");
                }
            }
        }

        // Nếu có lỗi business rule thì return luôn
        if (!errors.isEmpty()) {
            return errors;
        }
        movie.setTitle(movie.getTitle().toUpperCase());

        // 3️⃣ SAVE
        movieRepository.save(movie);
        return errors; // Trống = thành công
    }


    @Transactional
    public Page<Movie> getAllMovies(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }

    @Transactional
    public boolean existsByMovieID(String id) {
        return movieRepository.existsByMovieID(id);
    }

    @Transactional
    public Movie findById(String id) {
        return movieRepository.findByMovieID(id);
    }

    @Transactional
    public void save(Movie movie) {
        movieRepository.save(movie);
    }

    @Transactional
    public void delete(Movie movie) {
        movieRepository.delete(movie);
    }

    //Huynh Anh add- sửa movies now showing
    @Transactional
    public List<Movie> getNowShowingMovies() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        // Lấy tất cả phim trong DB
        List<Movie> allMovies = movieRepository.findAll();

        // Lọc ra:
        //  Phim đã hoặc đang chiếu: release_date <= hôm nay
        //  Phim chưa quá 30 ngày kể từ ngày chiếu: release_date >= hôm nay - 30
        List<Movie> filteredMovies = new ArrayList<>();
        for (Movie m : allMovies) {
            LocalDate release = m.getReleaseDate();
            if ((release.isBefore(today) || release.isEqual(today)) && !release.isBefore(thirtyDaysAgo)) {
                filteredMovies.add(m);
            }
        }

        return filteredMovies;
    }

    //
    @Transactional
    public void deleteById(String id) {
        if (movieRepository.existsById(id)) {
            movieRepository.deleteById(id);
        }
    }

    @Transactional
    public String generateMovieID() {
        String lastMovieID = movieRepository.findLastMovieId();
        if (lastMovieID == null || lastMovieID.isEmpty()) {
            return "MV000001";
        }
        int number = Integer.parseInt(lastMovieID.substring(2)) + 1;
        return String.format("MV%06d", number);
    }

    @Transactional
    public List<String> getImgPaths(String currentImg) {
        List<String> imagePaths = new ArrayList<>();
        try {
            // 1️⃣ Lấy danh sách file ảnh trong thư mục
            File folder = new ClassPathResource("static/assets/img/movies").getFile();
            File[] files = folder.listFiles();

            if (files == null) return imagePaths;

            // 2️⃣ Lấy danh sách đường dẫn ảnh (hoặc tên file) đã được dùng trong DB
            List<String> usedImgs = movieRepository.findAll()
                    .stream()
                    .map(Movie::getImg)
                    .filter(Objects::nonNull)
                    .map(path -> {
                        // chuẩn hóa: chỉ lấy tên file nếu đường dẫn có chứa "/"
                        int lastSlash = path.lastIndexOf("/");
                        return lastSlash != -1 ? path.substring(lastSlash + 1) : path;
                    })
                    .collect(Collectors.toList());

            // 3️⃣ Lọc ra những file chưa được dùng
            for (File file : files) {
                if (file.isFile() && !usedImgs.contains(file.getName())) {
                    imagePaths.add("/assets/img/movies/" + file.getName());
                }
            }
            if (currentImg != null && !imagePaths.contains(currentImg)) {
                imagePaths.add(currentImg);
            }

        } catch (IOException e) {
            throw new RuntimeException("Cannot read movies folder!", e);
        }

        return imagePaths;
    }


    public Page<Movie> findComingSoonMovies(Pageable pageable) {
        return movieRepository.findByReleaseDateGreaterThan(LocalDate.now(), pageable);
    }

    public boolean isMovieNowShowing(Movie movie, List<Movie> nowShowingMovies) {
        if (movie == null || nowShowingMovies == null) return false;
        return nowShowingMovies.stream()
                .anyMatch(m -> m.getMovieID().equals(movie.getMovieID()));
    }



    // Lấy Top 5 movies theo rating cao nhất
    @Transactional
    public List<Movie> getTop5Movies() {
        List<Object[]> result = movieRatingRepository.findTop5RatedMovies();
        List<Movie> topMovies = new ArrayList<>();

        for (Object[] row : result) {
            String movieId = (String) row[0];
            movieRepository.findById(movieId).ifPresent(topMovies::add);
            if (topMovies.size() >= 5) break; // chỉ lấy 5 phim
        }
        return topMovies;
    }
}

