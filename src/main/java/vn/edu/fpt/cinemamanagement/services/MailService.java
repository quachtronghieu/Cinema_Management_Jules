package vn.edu.fpt.cinemamanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendTest(String title,String deception,String to) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("addminn9999@gmail.com");
        msg.setTo(to);
        msg.setSubject(title);
        msg.setText(deception);

        mailSender.send(msg);
    }
}

