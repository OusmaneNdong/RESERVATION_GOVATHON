package com.gavathon.services.send.mail;

import com.gavathon.entity.Params;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class SendMaildingService {

    private final JavaMailSender mailSender;


    public void sendSimpleMailForOtp(MailBody mailBody){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailBody.to());
        message.setFrom(Params.EMAILSENDER);
        message.setSubject(mailBody.subject());
        message.setText(mailBody.text());

        mailSender.send(message);
    }
    /**
     * Envoi d'un QR code par mail avec pièce jointe
     */
    public void sendQrCodeToUser(MailBody mailBody, String base64Image, String fileName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(mailBody.to());
            helper.setSubject(mailBody.subject());
            helper.setText(mailBody.text(), false);

            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            helper.addAttachment(fileName, new ByteArrayResource(imageBytes), "image/png");

            mailSender.send(message);
            System.out.println("✅ QR Code envoyé par e-mail à " + mailBody.to());

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi du mail avec QR code : " + e.getMessage(), e);
        }
    }


    public void sendPdfToUser(MailBody mailBody, String pdfPath, String fileName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(mailBody.to());
            helper.setSubject(mailBody.subject());
            helper.setText(mailBody.text(), false);

            FileSystemResource file = new FileSystemResource(new File(pdfPath));
            helper.addAttachment(fileName, file);

            mailSender.send(message);

            System.out.println("✅ PDF envoyé par e-mail à " + mailBody.to());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi du mail avec PDF : " + e.getMessage(), e);
        }
    }

}
