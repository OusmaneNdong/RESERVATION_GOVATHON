package com.gavathon.services.send.otp;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OtpService {


    private final ConcurrentHashMap<String, Integer> otpStorage = new ConcurrentHashMap<>();

    // Génère un OTP pour un identifiant (email ou téléphone)
    public int generateOtp(String identifier) {
        int otp = 100000 + new Random().nextInt(900000);
        otpStorage.put(identifier, otp);
        return otp;
    }

    // Valide l'OTP pour un identifiant (email ou téléphone)
    public boolean validateOtp(String identifier, int otp) {
        Integer storedOtp = otpStorage.get(identifier);
        if (storedOtp != null && storedOtp == otp) {
            otpStorage.remove(identifier); // OTP validé → suppression
            return true;
        }
        return false;
    }
}
