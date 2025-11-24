package com.gavathon.controller;

import com.gavathon.dto.QrcodeDto;
import com.gavathon.dto.UserDto;
import com.gavathon.entity.Params;
import com.gavathon.entity.Qrcode;
import com.gavathon.entity.User;
import com.gavathon.repository.UserRepository;
import com.gavathon.services.qrcode.QrcodeService;
import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/qrcode")
@RequiredArgsConstructor
@CrossOrigin
public class QrcodeController {

    private final QrcodeService qrcodeService;
    private final UserRepository userRepository;


    @GetMapping("/generate/{userId}")
    public ResponseEntity<String> generateQRCode(@PathVariable Long userId) {
        try {
            // Ici, le service génère un QR code en Base64 pour l’utilisateurgenerateBusinessCardFromHtml
            String qrCodeBase64 = qrcodeService.generateQRCodeWithLink(userId);
            // On renvoie du texte (Base64) que Angular peut afficher via <img [src]="'data:image/png;base64,' + qrCode">
            return ResponseEntity.ok(qrCodeBase64);
        } catch (WriterException | IOException e) {
            return ResponseEntity.status(500).body("❌ Erreur lors de la génération du QR Code");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    @GetMapping("/{filename}/{idUser}")
    public ResponseEntity<Resource> getQrcode(
            @PathVariable String filename,
            @PathVariable Long idUser) throws IOException {

        // Récupérer l'utilisateur depuis la base
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

        // Vérifier que le QR code existe pour cet utilisateur
        Qrcode qrcode = user.getQrcode();
        if (qrcode == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Utilisateur n'a pas de QR code");
        }

        // Vérifier que le filename correspond (ignorer casse et espaces)
        if (!qrcode.getQrcodename().trim().equalsIgnoreCase(filename.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "QR code non autorisé pour cet utilisateur");
        }

        // Construire le chemin vers le fichier
        Path file = Paths.get(Params.IMAGEQRCODEURL + qrcode.getQrcodename());
        if (!Files.exists(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier QR code introuvable");
        }

        // Retourner le fichier comme ressource
        Resource resource = new UrlResource(file.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }






}
