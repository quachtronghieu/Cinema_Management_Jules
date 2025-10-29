package vn.edu.fpt.cinemamanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cinemamanagement.entities.Customer;
import vn.edu.fpt.cinemamanagement.services.CustomerService;

import java.security.MessageDigest;

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


        if(customerService.findCustomerById(id).getVerify().equals("resetPassword")){
            if(fi.equalsIgnoreCase(code)){
                return "auth/reset_password";
            }
        }
        return "error/error500";
    }

    @PostMapping
    public String changePassword(){



        return "da doi thanh cong";
    }


}
