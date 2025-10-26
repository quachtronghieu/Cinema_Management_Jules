package vn.edu.fpt.cinemamanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.fpt.cinemamanagement.services.MailService;

@RestController
public class MailController {

    @Autowired
    MailService mailService;

    @GetMapping("/sendmail")
    public String testSend(@RequestParam(defaultValue = "vyltt.ce190528@gmail.com") String to) {
        try {
            mailService.sendTest(to);
            return "Mail was send";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}