package vn.edu.fpt.cinemamanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendTest(String to) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("addminn9999@gmail.com");
        msg.setTo(to);
        msg.setSubject("HELLO");
        msg.setText("Test Test");

        mailSender.send(msg);
    }
}

