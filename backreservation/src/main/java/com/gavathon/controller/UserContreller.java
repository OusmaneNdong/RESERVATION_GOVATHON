package com.gavathon.controller;

import com.gavathon.dto.UserDto;
import com.gavathon.entity.Qrcode;
import com.gavathon.entity.User;
import com.gavathon.repository.QrcodeRepository;
import com.gavathon.repository.UserRepository;
import com.gavathon.services.qrcode.QrcodeService;
import com.gavathon.services.send.mail.MailBody;
import com.gavathon.services.send.mail.SendMaildingService;
import com.gavathon.services.send.otp.OtpService;
import com.gavathon.services.send.pdf.PdfService;
import com.gavathon.services.send.whatsapp.SendWhatSappService;
import com.gavathon.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserContreller {


    private final UserService userService;
    private final UserRepository userRepository;
    private final OtpService optService;
    private final QrcodeService qrcodeService;
    private final SendMaildingService sendEmailappService;
    private final SendWhatSappService sendWhatsappService;
    private final QrcodeRepository qrcodeRepository;
    private final PdfService pdfService;



    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public UserDto getUserWithQrcode(@PathVariable Long id) {
        return userService.getUserWithQrcode(id);
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserDto userDto) {
        User user = userService.registerUser(userDto);
        return ResponseEntity.ok(user);
    }

//
//    @PostMapping("/verify-otp-and-send-qrcode")
//    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, Object> payload) {
//        String email = (String) payload.get("email");
//        String telephone = (String) payload.get("telephone");
//        Integer otp = (Integer) payload.get("otp");
//
//        if ((email == null && telephone == null) || otp == null) {
//            return ResponseEntity.badRequest().body("Veuillez fournir un email ou un téléphone et l'OTP.");
//        }
//        boolean isValid = false;
//        User user = null;
//
//        // Validation OTP par email
//        if (email != null) {
//            isValid = optService.validateOtp(email, otp);
//            if (isValid) {
//                user = userRepository.findByEmail(email)
//                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec cet email"));
//            }
//        }
//
//        // Validation OTP par téléphone
//        if (!isValid && telephone != null) {
//            isValid = optService.validateOtp(telephone, otp);
//            if (isValid) {
//                user = userRepository.findByTelephone(telephone)
//                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec ce numéro"));
//            }
//        }
//
//        if (!isValid || user == null) {
//            return ResponseEntity.badRequest().body("❌ OTP invalide ou expiré !");
//        }
//
//        try {
//            // 1️⃣ Génération du QR code
//            String qrCodeBase64 = qrcodeService.generateQRCode(user.getId());
//
////            // 2️⃣ Envoi par mail
////            sendWhatSappService.sendQrCodeToUser(
////                    new MailBody(
////                            user.getEmail(),
////                            "🎟️ Votre QR Code d'accès",
////                            "Bonjour " + user.getPrenom() + ",\n\nVoici votre QR Code d'accès en pièce jointe.\n\nMerci."
////                    ),
////                    qrCodeBase64,
////                    user.getNom() + "-" + user.getPrenom() + ".png"
////            );
////
////            // 3️⃣ Envoi par WhatsApp
////            SendWhatSappService.sendQrCodeToWhatsApp(user.getTelephone(), qrCodeBase64);
//
//            // 4️⃣ Marquer l'utilisateur comme complet
//            user.setComplet(true);
//            userRepository.save(user);
//
//            return ResponseEntity.ok("✅ OTP validé avec succès ! QR Code envoyé par mail et WhatsApp.");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(500).body("❌ Erreur lors de la génération ou de l'envoi du QR code : " + e.getMessage());
//        }
//    }




    @PostMapping("/send-card")
    public ResponseEntity<?> sendCard(@RequestBody Map<String, Object> payload) throws IOException {

        String base64 = (String) payload.get("imageBase64");
        String email = (String) payload.get("email");
        String telephone = (String) payload.get("telephone");

        if (base64 == null) {
            return ResponseEntity.badRequest().body("Image manquante");
        }

        // ✅ Envoi mail
        sendEmailappService.sendQrCodeToUser(
                new MailBody(
                        email,
                        "🎟️ Votre badge GOVATHON 2025",
                        "Voici votre badge officiel au format image."
                ),
                base64,
                "badge-govathon.png"
        );

        // ✅ Envoi WhatsApp
//        SendWhatSappService.sendQrCodeToWhatsApp(
//                telephone,
//                base64
//        );

        return ResponseEntity.ok("✅ Badge envoyé par WhatsApp et email");
    }



    @PostMapping("/verify-otp-and-send-qrcode")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, Object> payload) {

        String email = (String) payload.get("email");
        String telephone = (String) payload.get("telephone");
        Integer otp = (Integer) payload.get("otp");

        if ((email == null && telephone == null) || otp == null) {
            return ResponseEntity.badRequest().body("Veuillez fournir un email ou un téléphone et l'OTP.");
        }

        boolean isValid = false;
        User user = null;

        if (email != null) {
            isValid = optService.validateOtp(email, otp);
            if (isValid) {
                user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            }
        }

        if (!isValid && telephone != null) {
            isValid = optService.validateOtp(telephone, otp);
            if (isValid) {
                user = userRepository.findByTelephone(telephone)
                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            }
        }

        if (!isValid || user == null) {
            return ResponseEntity.badRequest().body("❌ OTP invalide ou expiré !");
        }

        try {
            // ✅ 1 — QR Code base64
            String qrBase64 = qrcodeService.generateQRCodeWithLink(user.getId());

            // ✅ 2 — Générer & sauvegarder le PDF → retourne le NOM du fichier
            String pdfFilename = pdfService.generateAndSavePdf(user, qrBase64);

            // ✅ 3 — Stocker le nom du fichier PDF dans Qrcode
            Qrcode qrcode = qrcodeRepository.findByUser(user).orElse(new Qrcode());
            qrcode.setUser(user);
            qrcode.setQrcodename(pdfFilename);
            qrcodeRepository.save(qrcode);

            String pdfName = pdfService.generateAndSavePdf(user, qrBase64);

            String userHome = System.getProperty("user.home");
            String fullPdfPath = userHome + "/Downloads/" + pdfName;

            sendEmailappService.sendPdfToUser(
                    new MailBody(
                            user.getEmail(),
                            "Votre Badge Govathon 2025",
                            "Bonjour " + user.getPrenom() + ",\nVoici votre badge d’accès."
                    ),
                    fullPdfPath,
                    pdfName
            );


            // ✅ 5 — Envoi WhatsApp (Twilio → nécessite une URL ou un fichier local)
//            SendWhatSappService.sendQrCodeToWhatsApp(user.getTelephone(), pdfFilename);

            // ✅ 6 — Marquer comme complet
            user.setComplet(true);
            userRepository.save(user);

            return ResponseEntity.ok("✅ OTP validé ! Le badge PDF a été envoyé et enregistré.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("❌ Erreur lors de la génération du badge : " + e.getMessage());
        }
    }


    @PatchMapping("/presence/{id}")
    public ResponseEntity<User> markPresence(@PathVariable Long id) {
        User updated = userService.markAsPresentAndComplete(id);
        return ResponseEntity.ok(updated);
    }




    @PutMapping("/present/{id}")
    public User setUserPresent(@PathVariable Long id) {
        return userService.updateUserPresent(id);
    }



}
