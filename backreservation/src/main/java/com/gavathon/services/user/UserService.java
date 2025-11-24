package com.gavathon.services.user;

import com.gavathon.dto.QrcodeDto;
import com.gavathon.dto.UserDto;
import com.gavathon.entity.User;
import com.gavathon.repository.UserRepository;
import com.gavathon.services.send.mail.MailBody;
import com.gavathon.services.send.mail.SendMaildingService;
import com.gavathon.services.send.otp.OtpService;
import com.gavathon.services.send.whatsapp.SendWhatSappService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SendMaildingService sendMaildingService;
    private final OtpService otpService;

    private UserDto toDto(User user) {
        return UserDto.builder()
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .telephone(user.getTelephone())
                .email(user.getEmail())
                .fonction(user.getFonction())
                .qrcode(user.getQrcode() != null ?
                        QrcodeDto.builder()
                                .id(user.getQrcode().getId())
                                .qrcodename(user.getQrcode().getQrcodename())
                                .build()
                        : null
                )
                .build();
    }

    private User toEntity(UserDto dto) {
        User user = new User();
        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setTelephone(dto.getTelephone());
        user.setEmail(dto.getEmail());
        user.setFonction(dto.getFonction());

        if (dto.getQrcode() != null) {
            var qr = new com.gavathon.entity.Qrcode();
            qr.setId(dto.getQrcode().getId());
            qr.setQrcodename(dto.getQrcode().getQrcodename());
            qr.setUser(user);
            user.setQrcode(qr);
        }

        return user;
    }



    private final UserRepository userRepository;

    public UserDto getUserWithQrcode(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Conversion User -> UserDto directement dans le service
        return toDto(user);
    }

    /**
     * recuperation de
     * tous les users
     * @return
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    /**
     * @param userDto
     * @return
     */
    public User registerUser(UserDto userDto) {
        // Vérification existence
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà !");
        }

        if (userRepository.existsByTelephone(userDto.getTelephone())) {
            throw new RuntimeException("Un utilisateur avec ce numéro existe déjà !");
        }

        // Création de l'utilisateur
        User user = User.builder()
                .nom(userDto.getNom())
                .prenom(userDto.getPrenom())
                .telephone(userDto.getTelephone())
                .email(userDto.getEmail())
                .fonction(userDto.getFonction())
                .isPresenct(false)
                .isComplet(false)
                .build();

        // ✅ Génération d'un seul OTP
        int otp = otpService.generateOtp(user.getEmail()); // ou basé sur téléphone, peu importe

        // ✅ Assignation du même OTP à l'utilisateur
        user.setCodeOpt(String.valueOf(otp)); // Assurez-vous que User a un champ codeOtp

        // ✅ Sauvegarde de l'utilisateur avec OTP
        userRepository.save(user);

        // ✅ Envoi du même OTP par mail
        sendMaildingService.sendSimpleMailForOtp(
                new MailBody(
                        user.getEmail(),
                        "Code de confirmation",
                        "Bonjour " + user.getPrenom() + ",\n\nVotre code OTP est : " + otp + "\n\nMerci."
                )
        );

        // ✅ Envoi du même OTP par WhatsApp
        String message = "Bonjour " + user.getPrenom() + ", votre code OTP est : " + otp;
        SendWhatSappService.sendNotificationOTP(message, user.getTelephone());

        return user;
    }

//    public User registerUser(UserDto userDto) {
//        // Vérification existence
//        if (userRepository.existsByEmail(userDto.getEmail())) {
//            throw new RuntimeException("Un utilisateur avec cet email existe déjà !");
//        }
//
//        if (userRepository.existsByTelephone(userDto.getTelephone())) {
//            throw new RuntimeException("Un utilisateur avec ce numéro existe déjà !");
//        }
//
//        // Création de l'utilisateur
//        User user = User.builder()
//                .nom(userDto.getNom())
//                .prenom(userDto.getPrenom())
//                .telephone(userDto.getTelephone())
//                .email(userDto.getEmail())
//                .fonction(userDto.getFonction())
//                .isPresenct(false)
//                .isComplet(false)
//                .build();
//
//        userRepository.save(user);
//
//        // ✅ Génération OTP unique pour les deux canaux
//        int otp = otpService.generateOtp(user.getEmail()); // vous pouvez générer basé sur email ou téléphone, mais une seule fois
//
//        // ✅ Envoi OTP par mail
//        sendMaildingService.sendSimpleMailForOtp(
//                new MailBody(
//                        user.getEmail(),
//                        "Code de confirmation",
//                        "Bonjour " + user.getPrenom() + ",\n\nVotre code OTP est : " + otp + "\n\nMerci."
//                )
//        );
//
//        // ✅ Envoi OTP par WhatsApp
//        String message = "Bonjour " + user.getPrenom() + ", votre code OTP est : " + otp;
//        SendWhatSappService.sendNotificationOTP(message, user.getTelephone());
//
//        return user;
//    }




    public User markAsPresentAndComplete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPresenct(true);
        user.setComplet(true);

        return userRepository.save(user);
    }



    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));

        if (updatedUser.getNom() != null) existingUser.setNom(updatedUser.getNom());
        if (updatedUser.getPrenom() != null) existingUser.setPrenom(updatedUser.getPrenom());
        if (updatedUser.getTelephone() != null) existingUser.setTelephone(updatedUser.getTelephone());
        if (updatedUser.getEmail() != null) existingUser.setEmail(updatedUser.getEmail());
        if (updatedUser.getFonction() != null) existingUser.setFonction(updatedUser.getFonction());
        if (updatedUser.getCodeOpt() != null) existingUser.setCodeOpt(updatedUser.getCodeOpt());

        // mise à jour des booléens SI vous voulez les gérer
        existingUser.setPresenct(updatedUser.isPresenct());
        existingUser.setComplet(updatedUser.isComplet());

        return userRepository.save(existingUser);
    }


    public User updateUserPresent(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));

        // On force à TRUE
        existingUser.setPresenct(true);
        existingUser.setComplet(true);

        return userRepository.save(existingUser);
    }



    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }



}
