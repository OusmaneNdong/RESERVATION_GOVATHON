package com.gavathon.controller;

import com.gavathon.entity.User;
import com.gavathon.repository.UserRepository;
import com.gavathon.services.qrcode.QrcodeService;
import com.gavathon.services.send.pdf.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
@CrossOrigin
public class PdfController {

    private final UserRepository userRepository;
    private final QrcodeService qrcodeService;
    private final com.gavathon.services.send.pdf.PdfService pdfService;

    @GetMapping("/business-card/{userId}")
    public ResponseEntity<byte[]> generateBusinessCardPdf(@PathVariable Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Générer le QR code en Base64
        String qrBase64 = qrcodeService.generateQRCodeWithLink(userId);

        // Générer le PDF
        byte[] pdfBytes = pdfService.generateBusinessCardPdf(user, qrBase64);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"carte_visite.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
