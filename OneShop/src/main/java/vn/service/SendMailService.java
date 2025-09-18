package vn.service;

import java.io.IOException;
import jakarta.mail.MessagingException;
import vn.dto.MailInfo;

public interface SendMailService {
    void run();
    void queue(String to, String subject, String body);
    void queue(MailInfo mail);
    void send(MailInfo mail) throws MessagingException, IOException;
}
