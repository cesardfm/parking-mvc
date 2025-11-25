package com.g3.parking.service;

import com.g3.parking.request.InvoiceRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;

@Service
public class SendEmail {

    private final JavaMailSender mailSender;
    private final InvoiceService invoiceService;
    private final String mailFrom;

    public SendEmail(JavaMailSender mailSender, InvoiceService invoiceService,
                     @Value("${spring.mail.from:no-reply@demomailtrap.co}") String mailFrom) {
        this.mailSender = mailSender;
        this.invoiceService = invoiceService;
        this.mailFrom = mailFrom;
    }

    public void sendInvoiceEmail(String to, InvoiceRequest req) throws MessagingException, IOException {
        byte[] pdf = invoiceService.generateInvoicePdf(req);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // usar un from con dominio válido
        helper.setFrom(mailFrom);
        helper.setTo(to);
        helper.setSubject("Factura " + req.getInvoiceId());
        helper.setText("Adjunto su factura.", false);

        helper.addAttachment("invoice.pdf", new ByteArrayResource(pdf));

        mailSender.send(message);
    }
}
