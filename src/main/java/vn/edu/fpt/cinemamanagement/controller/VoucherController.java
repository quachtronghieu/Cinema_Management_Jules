package vn.edu.fpt.cinemamanagement.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import vn.edu.fpt.cinemamanagement.entities.Voucher;
import vn.edu.fpt.cinemamanagement.services.VoucherService;

@Controller
@RequestMapping("/vouchers")
public class VoucherController {

    private VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }


    @GetMapping("")
    public String vouchersList(Model model, @RequestParam(name = "page", defaultValue = "1", required = false) int page) {
        int size = 10;

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

        if (voucherPage.isEmpty()){
            model.addAttribute("voucherEmpty", "Voucher list is empty" );
        }
        model.addAttribute("voucherPage", voucherPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", totalPages);

        return "vouchers/voucher_list";
    }
    @GetMapping("/create")
    public String newVoucher(Model model) {
        model.addAttribute("voucher", new Voucher());
        return "vouchers/voucher_create";
    }

    @PostMapping("/create")
    public String createVoucher(Voucher voucher, Model model) {
        if (voucher.getDiscountType().equalsIgnoreCase("amount")) {
            voucher.setVoucherId(voucherService.generateAmountVoucherID());
        } else {
            voucher.setVoucherId(voucherService.generatePercentageVoucherID());
        }

        boolean isValid = voucherService.validateVoucher(model, voucher);
        if (!isValid) {
            model.addAttribute("voucher", voucher);
            return "vouchers/voucher_create";
        }

        voucher.setVoucherName(voucher.getVoucherName().toUpperCase());
        voucher.setCode(voucher.getCode().toUpperCase());
        voucher.setUsedCount(0);
        voucher.setStatus(true);
        voucherService.save(voucher);
        return "redirect:/vouchers";
    }

    @GetMapping("/update/{id}")
    public String editVoucher(Model model, @PathVariable("id") String id) {
        model.addAttribute("voucher", voucherService.findVoucherById(id));
        return "vouchers/voucher_update";
    }

    @PostMapping("/update")
    public String updateVoucher(Voucher voucher, Model model) {
        boolean isValid = voucherService.validateVoucher(model, voucher);
        if (!isValid) {
            model.addAttribute("voucher", voucher);
            return "vouchers/voucher_update";
        }

        voucher.setVoucherName(voucher.getVoucherName().toUpperCase());
        voucher.setCode(voucher.getCode().toUpperCase());
        voucherService.save(voucher);
        return "redirect:/vouchers";
    }



    @GetMapping("/detail/{id}")
    public String detailVoucher(Model model, @PathVariable("id") String id) {
        model.addAttribute("voucher", voucherService.findVoucherById(id));
        return "vouchers/voucher_detail";
    }


    @PostMapping("/delete")
    public String deleteVoucher(@RequestParam("voucherId") String id) {
        voucherService.delete(id);
        return "redirect:/vouchers";
    }
}

