package vn.edu.fpt.cinemamanagement.services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendForgetPasswordMail(String title,String deception,String to) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("addminn9999@gmail.com");
            helper.setTo(to);
            helper.setSubject(title);

            // Cho phép nội dung HTML (true = HTML)
            helper.setText(deception, true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error when send mail: " + e.getMessage());
        }
    }
}

