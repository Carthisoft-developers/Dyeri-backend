package com.dyeri.notification.handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.UUID;
@Slf4j @Component @RequiredArgsConstructor
public class MailHandler {
    private final JavaMailSender mailSender;
    public Mono<Void> sendEmail(UUID userId, String subject, String text) {
        return Mono.fromRunnable(() -> {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom("noreply@dyeri.tn");
                msg.setSubject(subject);
                msg.setText(text);
                mailSender.send(msg);
            } catch (Exception e) { log.warn("Mail error: {}", e.getMessage()); }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}