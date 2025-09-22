package vn.service.impl;

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import vn.dto.MailInfo;
import vn.service.SendMailService;

@Service
public class SendMailServiceImpl implements SendMailService {
    @Autowired
    JavaMailSender sender;

    List<MailInfo> list = new ArrayList<>();

    @Override
    public void send(MailInfo mail) throws MessagingException, IOException {
        // Tạo message
        MimeMessage message = sender.createMimeMessage();
        // Sử dụng Helper để thiết lập các thông tin cần thiết cho message
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        helper.setFrom(mail.getFrom());
        helper.setTo(mail.getTo());
        helper.setSubject(mail.getSubject());
        helper.setText(mail.getBody(), true);
        helper.setReplyTo(mail.getFrom());

        if (mail.getAttachments() != null) {
            FileSystemResource file = new FileSystemResource(new File(mail.getAttachments()));
            helper.addAttachment(mail.getAttachments(), file);
        }

        // Gửi message đến SMTP server
        sender.send(message);
    }

    @Override
    public void queue(MailInfo mail) {
        list.add(mail);
    }

    @Override
    public void queue(String to, String subject, String body) {
        System.out.println("=== QUEUEING EMAIL ===");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        queue(new MailInfo(to, subject, body));
    }

    @Override
    @Scheduled(fixedDelay = 5000)
    public void run() {
        System.out.println("=== SCHEDULER RUNNING ===");
        System.out.println("Queue size: " + list.size());
        while (!list.isEmpty()) {
            MailInfo mail = list.remove(0);
            try {
                System.out.println("=== SENDING EMAIL ===");
                System.out.println("To: " + mail.getTo());
                this.send(mail);
                System.out.println("=== EMAIL SENT SUCCESSFULLY ===");
            } catch (Exception e) {
                System.out.println("=== EMAIL SEND FAILED ===");
                e.printStackTrace();
            }
        }
    }
}
