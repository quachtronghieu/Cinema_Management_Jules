package vn.edu.fpt.cinemamanagement.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.fpt.cinemamanagement.services.BookingService;

import java.security.MessageDigest;

@Controller
@RequestMapping(value = "/payments")
public class PaymentController {

    @Autowired
    private QRController qrController;
    @Autowired
    BookingService bookingService;



    @GetMapping("/ebanking/{id}/{staff}")
    public String ebanking(@PathVariable String id, @PathVariable String staff, Model model) {

        return "";
    }


    @GetMapping("/{id}/{code}")
    public String paymentPage(@PathVariable(value = "id")String id, @PathVariable(value = "code") String code, Model model) {


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

       return "redirect:/qrpayment/qrpayment?idpay="+" https://unquivering-latrice-semisentimental.ngrok-free.dev/payments/paymentsuccess";


    }

    @GetMapping("/paymentsuccess")
    public String paymentSuccessPage( Model model) {

        return "payment/payment";
    }
}
