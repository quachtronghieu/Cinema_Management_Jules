package vn.edu.fpt.cinemamanagement.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cinemamanagement.entities.Movie;
import vn.edu.fpt.cinemamanagement.entities.Voucher;
import vn.edu.fpt.cinemamanagement.services.MovieService;
import vn.edu.fpt.cinemamanagement.services.VoucherService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/homepage")
public class HomepageController {
    @Autowired
    private MovieService movieService;
    @Autowired
    private VoucherService voucherService;

    //Huynh Anh add
    @GetMapping({"", "/"})
    public String homepage(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("username", username);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String role = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", "));
            model.addAttribute("role", role);
        }
        return "homepage/homepage";
    }

    @GetMapping("/vouchers")
    public String vouchers(Model model, @RequestParam(name = "page", defaultValue = "1", required = false) int page) {
        int size = 9;

        // Spring Data bắt đầu từ 0
        int pageIndex = page - 1;
        Pageable pageable = PageRequest.of(pageIndex, size);
        Page<Voucher> voucherPage = voucherService.findAllVoucher(pageable);

        model.addAttribute("vouchersList", voucherPage.getContent());

        int totalPages = voucherPage.getTotalPages();
        int currentPage = page; // vẫn giữ 1-based cho Thymeleaf

        int visiblePages = 5;
        int startPage, endPage;

        if (totalPages <= visiblePages) {
            startPage = 1; // 1-based
            endPage = totalPages;
        } else {
            startPage = ((currentPage - 1) / visiblePages) * visiblePages + 1;
            endPage = Math.min(startPage + visiblePages - 1, totalPages);
        }

        model.addAttribute("voucherPage", voucherPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", totalPages);
        return "vouchers/voucher_list_customer";
    }

    @GetMapping("/vouchers/detail/{id}")
    public String voucherDetails(@PathVariable("id") String id, Model model) {
        model.addAttribute("voucher", voucherService.findVoucherById(id));
        return "vouchers/voucher_detail_customer";
    }

    // 1. Add thêm movies now showing - Huynh Anh
    @GetMapping("/movies_nowshowing")
    public String moviesNowShowing(
            @CookieValue(value = "user_name", required = false) String username,
            @CookieValue(value = "user_role", required = false) String role,
            Model model) {
        // Lấy danh sách phim đang chiếu (có logic ẩn phim > 30 ngày )
        List<Movie> nowShowing = movieService.getNowShowingMovies();

        // Đưa danh sách và thông tin người dùng ra view
        model.addAttribute("nowShowing", nowShowing);
        model.addAttribute("username", username);
        model.addAttribute("role", role);

        // Render sang view mới
        return "movies/movies_nowshowing"; // trỏ tới file templates/movies/movies_nowshowing.html
    }

}
