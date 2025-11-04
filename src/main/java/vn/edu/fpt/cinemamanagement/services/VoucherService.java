package vn.edu.fpt.cinemamanagement.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import vn.edu.fpt.cinemamanagement.entities.Voucher;
import vn.edu.fpt.cinemamanagement.repositories.VoucherRepository;

import java.util.List;

@Service
public class VoucherService {

    private VoucherRepository voucherRepo;

    public VoucherService(VoucherRepository voucherRepo) {
        this.voucherRepo = voucherRepo;
    }

    public Page<Voucher> findAllVoucher(Pageable pageable) {
        return voucherRepo.findAll(pageable);
    }

    public Voucher findVoucherById(String ID) {
        return voucherRepo.findById(ID).orElse(null);
    }

    public Voucher save(Voucher voucher) {
        return voucherRepo.save(voucher);
    }

    private int extractNextNumber(String maxID) {
        if (maxID == null || maxID.length() < 4) {
            return 1; // bắt đầu từ 00001 nếu chưa có mã
        }
        String numberPart = maxID.substring(3); // Bỏ VC1 / VC2
        return Integer.parseInt(numberPart) + 1;
    }

    public String generateAmountVoucherID() {
        String maxID = voucherRepo.getMaxAmountVoucherID();
        int nextNumber = extractNextNumber(maxID);

        return String.format("VC1%05d", nextNumber);
    }

    public String generatePercentageVoucherID() {
        String maxID = voucherRepo.getMaxPercentageVoucherID();
        int nextNumber = extractNextNumber(maxID);

        return String.format("VC2%05d", nextNumber);
    }

    public boolean validateVoucher(Model model, Voucher voucher) {
        boolean isValid = true;
        List<Voucher> voucherList = voucherRepo.findAll();

        //Validate Name
        if (voucher.getVoucherName().isEmpty()) {
            model.addAttribute("errorName", "Voucher name must be not empty!");
            isValid = false;
        } else if (voucher.getVoucherName().length() < 3) {
            model.addAttribute("errorName", "Voucher name too short, must be greater than 3 characters");
            isValid = false;
        } else if (voucher.getVoucherName().length() > 100) {
            model.addAttribute("errorName", "Voucher name too long");
            isValid = false;
        } else if (!voucher.getVoucherName().matches("^[a-zA-Z0-9\\s]+$")) {
            model.addAttribute("errorName", "Voucher name must be not include special characters");
            isValid = false;
        }

        //Validate code
        if (voucher.getCode().isEmpty()) {
            model.addAttribute("errorCode", "Voucher code must be not empty!");
            isValid = false;
        } else if (voucher.getCode().length() < 3) {
            model.addAttribute("errorCode", "Voucher code too short, must be greater than 3 characters");
            isValid = false;
        } else if (!voucher.getCode().matches("^[a-zA-Z0-9\\s]+$")) {
            model.addAttribute("errorCode", "Voucher code must be not include special characters");
            isValid = false;
        } else if (voucher.getCode().length() > 10) {
            model.addAttribute("errorCode", "Voucher name too long, must be less than or equal to 10 characters");
            isValid = false;
        } else if (voucher.getCode().split(" ").length > 1) {
            model.addAttribute("errorCode", "Voucher must be not have space");
            isValid = false;
        }
        for (Voucher v : voucherList) {
            if (voucher.getCode().equalsIgnoreCase(v.getCode()) && !voucher.getVoucherId().equalsIgnoreCase(v.getVoucherId())) {
                model.addAttribute("errorCode", "Voucher code is available");
                isValid = false;
            }
        }

        //Validate Usage Limit
        try {
            if (voucher.getUsageLimit() <= 0) {
                model.addAttribute("errorUsage", "Usage Limit number must be greater than 0");
                isValid = false;
            } else if (voucher.getUsageLimit() > 100000) {
                model.addAttribute("errorUsage", "Usage Limit number is large, must be less than 100.000");
                isValid = false;
            }
        } catch (Exception e) {
            model.addAttribute("errorUsage",
                    "Usage Limit is large");
            isValid = false;
        }

        //Validate DiscountValue
        try {
            if (voucher.getDiscountType().equalsIgnoreCase("percentage")) {
                if (voucher.getDiscountValue() <= 0 || voucher.getDiscountValue() >= 100) {
                    model.addAttribute("errorValue",
                            "Discount value must be greater than 0 and less than 100");
                    isValid = false;
                }
            } else {
                if (voucher.getDiscountValue() <= 1000) {
                    model.addAttribute("errorValue",
                            "Discount value must be greater than 1.000VNĐ");
                    isValid = false;
                } else if (voucher.getDiscountValue() > 10000000) {
                    model.addAttribute("errorValue",
                            "Discount value is large");
                    isValid = false;
                }
            }
        } catch (Exception e) {
            model.addAttribute("errorValue",
                    "Discount value is large");
            isValid = false;
        }
        return isValid;
    }

    public void delete(String id) {
        voucherRepo.deleteById(id);
    }
}
