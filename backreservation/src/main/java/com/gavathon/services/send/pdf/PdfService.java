package com.gavathon.services.send.pdf;

import com.gavathon.entity.User;
import com.gavathon.repository.UserRepository;
import com.gavathon.services.qrcode.QrcodeService;
import com.gavathon.services.user.UserService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

@Service
@RequiredArgsConstructor


public class PdfService {

    private final TemplateEngine templateEngine;
    private final UserRepository userRepository;
    private final QrcodeService qrCodeService;

    /**
     * Génère le PDF d'une carte de visite avec QR code
     */
    public byte[] generateBusinessCardPdf(User user, String qrBase64) throws Exception {

        // Injection des variables dans le template HTML
        Context context = new Context();
        context.setVariable("prenom", user.getPrenom());
        context.setVariable("nom", user.getNom());
        context.setVariable("fonction", user.getFonction());
        context.setVariable("email", user.getEmail());
        context.setVariable("telephone", user.getTelephone());
        context.setVariable("qrcode", qrBase64);
        context.setVariable("ministerLogo", encodeImage("static/logos/logominister.png"));
        context.setVariable("mctnLogo", encodeImage("static/logos/mctn-removebg.png"));
        context.setVariable("s1", encodeImage("/images/COFINANCE UE.png"));
        context.setVariable("s2", encodeImage("/images/EXPERTISE FRANCE.png"));
        context.setVariable("s3", encodeImage("/images/LOGO GIZ.png"));
        context.setVariable("s4", encodeImage("/images/UE.png"));
        context.setVariable("s5", encodeImage("static/logos/mctn.png"));



        String htmlContent = templateEngine.process("badge", context);




        // Génération du PDF
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(os);
            builder.run();

            byte[] pdfBytes = os.toByteArray();

            // 💾 Enregistrement dans le dossier "Downloads"
            saveToDownloads(pdfBytes, user);

            return pdfBytes;

        } catch (Exception e) {
            throw new Exception("Erreur génération PDF : " + e.getMessage(), e);
        }
    }

    /**
     * Enregistre le PDF dans le répertoire "Downloads" de l'utilisateur
     */
    private void saveToDownloads(byte[] pdfBytes, User user) throws IOException {
        // Récupère le chemin du dossier "Downloads" de l’utilisateur
        String userHome = System.getProperty("user.home");
        Path downloadsPath = Paths.get(userHome, "Downloads");

        // Crée le dossier s’il n’existe pas
        if (!Files.exists(downloadsPath)) {
            Files.createDirectories(downloadsPath);
        }

        // Nom du fichier
        String filename = String.format("badge_%s_%s.pdf",
                user.getPrenom().replaceAll("\\s+", "_"),
                user.getNom().replaceAll("\\s+", "_"));

        Path filePath = downloadsPath.resolve(filename);

        // Écrit le fichier
        Files.write(filePath, pdfBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("✅ Badge PDF enregistré dans : " + filePath.toAbsolutePath());
    }

    // Convertit les images en Base64 pour le template
    private String encodeImage(String path) throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get("src/main/resources/" + path));
        return Base64.getEncoder().encodeToString(bytes);
    }



    public String generateAndSavePdf(User user, String qrBase64) throws Exception {
        byte[] pdfBytes = generateBusinessCardPdf(user, qrBase64);

        String filename = String.format("badge_%s_%s.pdf",
                user.getPrenom().replaceAll("\\s+", "_"),
                user.getNom().replaceAll("\\s+", "_"));

        String userHome = System.getProperty("user.home");
        Path downloadsPath = Paths.get(userHome, "Downloads");

        if (!Files.exists(downloadsPath)) {
            Files.createDirectories(downloadsPath);
        }

        Files.write(downloadsPath.resolve(filename), pdfBytes,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return filename; // ✅ Renvoie le nom du fichier
    }




    private String loadBackgroundImage() {
        try (InputStream is = getClass().getResourceAsStream("/images/finalgov.jpg")) {
            if (is == null) throw new RuntimeException("Image finalgov.jpg introuvable !");
            return Base64.getEncoder().encodeToString(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture image : " + e.getMessage());
        }
    }

}
