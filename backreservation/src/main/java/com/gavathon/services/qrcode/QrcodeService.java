package com.gavathon.services.qrcode;

import com.gavathon.dto.QrcodeDto;
import com.gavathon.dto.UserDto;
import com.gavathon.entity.Params;
import com.gavathon.entity.Qrcode;
import com.gavathon.entity.User;
import com.gavathon.repository.QrcodeRepository;
import com.gavathon.repository.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QrcodeService {


   private final QrcodeRepository qrcodeRepository;
   private final UserRepository userRepository;


    // ✅ Récupérer un QR code avec son utilisateur
    public QrcodeDto getQrcodeById(int id) {
        Qrcode qrcode = qrcodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR code non trouvé"));

        return toDto(qrcode);
    }

    // 🔹 Conversion Qrcode -> QrcodeDto
    private QrcodeDto toDto(Qrcode qrcode) {
        return QrcodeDto.builder()
                .id(qrcode.getId())
                .qrcodename(qrcode.getQrcodename())
                .user(qrcode.getUser() != null ?
                        UserDto.builder()
                                .nom(qrcode.getUser().getNom())
                                .prenom(qrcode.getUser().getPrenom())
                                .telephone(qrcode.getUser().getTelephone())
                                .email(qrcode.getUser().getEmail())
                                .fonction(qrcode.getUser().getFonction())
                                .build()
                        : null
                )
                .build();
    }

    // 🔹 Conversion QrcodeDto -> Qrcode
    private Qrcode toEntity(QrcodeDto dto) {
        Qrcode qrcode = new Qrcode();
        qrcode.setId(dto.getId());
        qrcode.setQrcodename(dto.getQrcodename());

        if (dto.getUser() != null) {
            User user = new User();
            user.setNom(dto.getUser().getNom());
            user.setPrenom(dto.getUser().getPrenom());
            user.setTelephone(dto.getUser().getTelephone());
            user.setEmail(dto.getUser().getEmail());
            user.setFonction(dto.getUser().getFonction());

            qrcode.setUser(user);
        }

        return qrcode;
    }

//    public String generateQRCode(Long userId) throws WriterException, IOException {
//        // 🔹 Récupérer l’utilisateur
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + userId));
//
//        // ✅ Contenu du QR code
//        String qrContent = String.format(
//                "👤 Informations de l'utilisateur :\n\n" +
//                        "Nom : %s %s\n" +
//                        "Email : %s\n" +
//                        "Téléphone : %s\n" +
//                        "Fonction : %s\n" +
//                        "Code Opt : %s\n" +
//                        "Présent : %s\n" +
//                        "Complet : %s",
//                user.getNom(),
//                user.getPrenom(),
//                user.getEmail(),
//                user.getTelephone(),
//                user.getFonction(),
//                user.getCodeOpt(),
//                user.isPresenct() ? "Oui" : "Non",
//                user.isComplet() ? "Oui" : "Non"
//        );
//
//        // 🔹 Paramètres du QR code
//        QRCodeWriter qrCodeWriter = new QRCodeWriter();
//        Map<EncodeHintType, Object> qrParams = new HashMap<>();
//        qrParams.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
//        qrParams.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.toString());
//        qrParams.put(EncodeHintType.MARGIN, 2);
//
//        int qrSize = 600;
//        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, qrSize, qrSize, qrParams);
//        BufferedImage qrImage = new BufferedImage(qrSize, qrSize, BufferedImage.TYPE_INT_RGB);
//
//        for (int x = 0; x < qrSize; x++) {
//            for (int y = 0; y < qrSize; y++) {
//                qrImage.setRGB(x, y, bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF);
//            }
//        }
//
//        // 🔹 Ajouter un logo
//        try {
//            ClassPathResource resource = new ClassPathResource("images/logominister.png");
//            BufferedImage logo = ImageIO.read(resource.getInputStream());
//            int logoSize = qrSize / 4;
//            Image scaledLogo = logo.getScaledInstance(logoSize, logoSize, Image.SCALE_SMOOTH);
//
//            Graphics2D g = qrImage.createGraphics();
//            int x = (qrSize - logoSize) / 2;
//            int y = (qrSize - logoSize) / 2;
//            g.drawImage(scaledLogo, x, y, null);
//            g.dispose();
//        } catch (Exception e) {
//            System.err.println("Logo non trouvé, génération du QR sans logo.");
//        }
//
//        // 📁 Créer le dossier si inexistant
//        File directory = new File(Params.DIRECTORYRESOURCE);
//        if (!directory.exists() && !directory.mkdirs()) {
//            throw new IOException("Impossible de créer le dossier : " + Params.DIRECTORYRESOURCE);
//        }
//
//        // ✅ Nom du fichier unique : NOM-PRENOM-TELEPHONE-timestamp.png
//        String safeNom = cleanFileName(user.getNom());
//        String safePrenom = cleanFileName(user.getPrenom());
//        String safeTel = cleanFileName(user.getTelephone());
//        String timestamp = String.valueOf(System.currentTimeMillis()); // identifiant unique
//        String fileName = safeNom + "-" + safePrenom + "-" + safeTel + "-" + timestamp + ".png";
//
//        File qrFile = new File(directory, fileName);
//        ImageIO.write(qrImage, "PNG", qrFile);
//
//        // 🔹 Conversion Base64
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        ImageIO.write(qrImage, "PNG", outputStream);
//        String base64Qr = Base64.getEncoder().encodeToString(outputStream.toByteArray());
//
//        // 🔹 Enregistrer le QR en base
//        Qrcode qrcode = new Qrcode();
//        qrcode.setQrcodename(fileName); // nom unique
//        qrcode.setUser(user);
//        qrcodeRepository.save(qrcode);
//
//        return base64Qr;
//    }


    private String cleanFileName(String input) {
        if (input == null) return "inconnu";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", ""); // enlève les accents
        return normalized.replaceAll("[^a-zA-Z0-9-_]", ""); // enlève les caractères spéciaux
    }

    public QrcodeDto getQrcodeByNameAndUserId(String qrcodename, Long userId) {
        Qrcode qrcode = qrcodeRepository
                .findByQrcodenameAndUserId(qrcodename, userId)
                .orElseThrow(() -> new RuntimeException("QR code not found"));

        // Conversion en DTO
        return QrcodeDto.builder()
                .id(qrcode.getId())
                .qrcodename(qrcode.getQrcodename())
                .user(null) // tu peux mapper user en UserDto si nécessaire
                .build();
    }



//    public String generateQRCodeWithLink(Long userId) throws WriterException, IOException {
//        // 🔹 Vérifier si l'utilisateur existe
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + userId));
//
//        // 🔗 Construire le lien final compatible Angular
//        String qrContent = Params.LIENPRESENT + "/" + userId; // <-- mise à jour ici
//
//        // 🔹 Paramètres du QR code
//        QRCodeWriter qrCodeWriter = new QRCodeWriter();
//        Map<EncodeHintType, Object> qrParams = new HashMap<>();
//        qrParams.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
//        qrParams.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.toString());
//        qrParams.put(EncodeHintType.MARGIN, 2);
//
//        int qrSize = 600;
//        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, qrSize, qrSize, qrParams);
//        BufferedImage qrImage = new BufferedImage(qrSize, qrSize, BufferedImage.TYPE_INT_RGB);
//
//        for (int x = 0; x < qrSize; x++) {
//            for (int y = 0; y < qrSize; y++) {
//                qrImage.setRGB(x, y, bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF);
//            }
//        }
//
//        // 🔹 Ajouter un logo au centre (optionnel)
//        try {
//            ClassPathResource resource = new ClassPathResource("images/logominister.png");
//            BufferedImage logo = ImageIO.read(resource.getInputStream());
//            int logoSize = qrSize / 4;
//            Image scaledLogo = logo.getScaledInstance(logoSize, logoSize, Image.SCALE_SMOOTH);
//
//            Graphics2D g = qrImage.createGraphics();
//            int x = (qrSize - logoSize) / 2;
//            int y = (qrSize - logoSize) / 2;
//            g.drawImage(scaledLogo, x, y, null);
//            g.dispose();
//        } catch (Exception e) {
//            System.err.println("Logo non trouvé, génération du QR sans logo.");
//        }
//
//        // 📁 Enregistrer le QR code
//        File directory = new File(Params.DIRECTORYRESOURCE);
//        if (!directory.exists()) directory.mkdirs();
//
//        String fileName = "QR-" + user.getNom() + "-" + user.getPrenom() + "-" + System.currentTimeMillis() + ".png";
//        File qrFile = new File(directory, fileName);
//        ImageIO.write(qrImage, "PNG", qrFile);
//
//        // 🔹 Conversion en Base64
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        ImageIO.write(qrImage, "PNG", outputStream);
//        String base64Qr = Base64.getEncoder().encodeToString(outputStream.toByteArray());
//
//        // 🔹 Enregistrer le QR en base
//        Qrcode qrcode = new Qrcode();
//        qrcode.setQrcodename(fileName);
//        qrcode.setUser(user);
//        qrcodeRepository.save(qrcode);
//
//        return base64Qr;
//    }

    public String generateQRCodeWithLink(Long userId) throws WriterException, IOException {
        // 🔹 Vérifier si l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + userId));

        String qrContent;

        // 🔹 Déterminer le contenu du QR selon isPresent
        if (user.isPresenct()) {
            qrContent = "Monsieur/Madame " + user.getPrenom() + " " + user.getNom() +
                    " et le téléphone " + user.getTelephone() + " est déjà présent dans la salle.";
        } else {
            qrContent = Params.LIENPRESENT + "/" + userId;
        }

        // 🔹 Paramètres du QR code
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> qrParams = new HashMap<>();
        qrParams.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        qrParams.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.toString());
        qrParams.put(EncodeHintType.MARGIN, 2);

        int qrSize = 600;
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, qrSize, qrSize, qrParams);
        BufferedImage qrImage = new BufferedImage(qrSize, qrSize, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < qrSize; x++) {
            for (int y = 0; y < qrSize; y++) {
                qrImage.setRGB(x, y, bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }

        // 🔹 Ajouter un logo au centre (optionnel)
        try {
            ClassPathResource resource = new ClassPathResource("images/logominister.png");
            BufferedImage logo = ImageIO.read(resource.getInputStream());
            int logoSize = qrSize / 4;
            Image scaledLogo = logo.getScaledInstance(logoSize, logoSize, Image.SCALE_SMOOTH);

            Graphics2D g = qrImage.createGraphics();
            int x = (qrSize - logoSize) / 2;
            int y = (qrSize - logoSize) / 2;
            g.drawImage(scaledLogo, x, y, null);
            g.dispose();
        } catch (Exception e) {
            System.err.println("Logo non trouvé, génération du QR sans logo.");
        }

        // 📁 Enregistrer le QR code
        File directory = new File(Params.DIRECTORYRESOURCE);
        if (!directory.exists()) directory.mkdirs();

        String fileName = "QR-" + user.getNom() + "-" + user.getPrenom() + "-" + System.currentTimeMillis() + ".png";
        File qrFile = new File(directory, fileName);
        ImageIO.write(qrImage, "PNG", qrFile);

        // 🔹 Conversion en Base64
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "PNG", outputStream);
        String base64Qr = Base64.getEncoder().encodeToString(outputStream.toByteArray());

        // 🔹 Enregistrer le QR en base
        Qrcode qrcode = new Qrcode();
        qrcode.setQrcodename(fileName);
        qrcode.setUser(user);
        qrcodeRepository.save(qrcode);

        return base64Qr;
    }

}
