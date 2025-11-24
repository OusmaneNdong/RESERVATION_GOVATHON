package com.gavathon.services.send.mail;

public record MailBody(
        String to,
        String subject,
        String text
) {}
