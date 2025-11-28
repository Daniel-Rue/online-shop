package ru.kon.onlineshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ru.kon.onlineshop.entity.Order;
import ru.kon.onlineshop.exceptions.email.EmailSendException;
import ru.kon.onlineshop.service.EmailService;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOrderConfirmation(Order order, String email) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Подтверждение заказа #" + order.getId());
            helper.setText(generateEmailContent(order), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendException("Не удалось отправить письмо", e);
        }
    }

    private String generateEmailContent(Order order) {
        Context context = new Context();
        context.setVariable("order", order);
        return templateEngine.process("order-email", context);
    }
}
