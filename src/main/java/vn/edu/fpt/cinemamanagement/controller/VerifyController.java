package vn.edu.fpt.cinemamanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cinemamanagement.entities.Customer;
import vn.edu.fpt.cinemamanagement.services.CustomerService;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Controller
@RequestMapping("/verify")
public class VerifyController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/done/{id}/{code}")
    public String veryPage(@PathVariable(value="id") String id, @PathVariable(value="code") String code, Model model){

        String input = "wait"+ id;
        String fi;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            fi= sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Customer customer = customerService.findCustomerById(id);
        if (!"resetPassword".equals(customer.getVerify())) {
            model.addAttribute("error", "This reset link is no longer valid!");
            return "error/error500";
        }


            LocalDateTime requestTime = customer.getResetRequestedAt();
            LocalDateTime now = LocalDateTime.now();

            if(requestTime != null && now.isAfter(requestTime.plusMinutes(10))) {
                customer.setVerify("active");
                customer.setResetRequestedAt(null);
                customerService.save(customer);
                model.addAttribute("error", "Link expired, please request again!");
                return "error/error500";
            }

            if(fi.equalsIgnoreCase(code)){
                model.addAttribute("customer", customerService.findCustomerById(id));
                return "auth/reset_password";
            }


        model.addAttribute("error", "The URL is not valid");
        return "error/error500";
    }

    @PostMapping("/resetPassword")
    public String changePassword(@RequestParam("id") String id,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Model model) {
        Map<String, String> errors = customerService.resetPassword(id, newPassword, confirmPassword);
        Customer customer = customerService.findCustomerById(id);
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("customer", customer);
            return "auth/reset_password";
        }

        customer.setVerify("active");
        customer.setResetRequestedAt(null);
        customerService.save(customer);
        model.addAttribute("success", "Password changed successfully!");

        return "auth/login";

    }

}
