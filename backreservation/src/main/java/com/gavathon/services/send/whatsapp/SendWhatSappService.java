package com.gavathon.services.send.whatsapp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class SendWhatSappService {





    public static void sendNotificationOTP(String message, String number) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");

        RequestBody body = RequestBody.create(mediaType,
                "token=o5ev9jpddl8saakw" +
                        "&to=+" + number +
                        "&body=" + message +
                        "&priority=1" +
                        "&referenceId=");

        Request request = new Request.Builder()
                .url("https://api.ultramsg.com/instance40778/messages/chat")
                .post(body)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("✅ WhatsApp API response: " + response.body().string());
        } catch (IOException e) {
            System.err.println("❌ Erreur envoi WhatsApp à " + number + " : " + e.getMessage());
        }
    }

//    public static void sendQrCodeToWhatsApp(String telephone, String document) throws IOException {
//        OkHttpClient client = new OkHttpClient();
//
//
//        RequestBody body = new FormBody.Builder()
//                .add("token", "o5ev9jpddl8saakw")
//                .add("to", telephone)
//                .add("filename", "attestation_nafp.pdf")
//                .add("document", document)
//                .add("caption", "veuillez recevoir votre attestaton, vous pouvez toujours l'utiliser tant qu'elle est valable. ")
//
//
//                .build();
//
//        Request request = new Request.Builder()
//                .url("https://api.ultramsg.com/instance40778/messages/document")
//                .post(body)
//                .addHeader("content-type", "application/x-www-form-urlencoded")
//                .build();
//
//        Response response = client.newCall(request).execute();
//
//        System.out.println(response.body().string());
//
//
//    }
//




    public static void sendQrCodeToWhatsApp(String telephone, String imageBase64) throws IOException {

        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("token", "o5ev9jpddl8saakw")
                .add("to", telephone)
                .add("image", imageBase64)
                .add("caption", "🎟️ Voici votre badge d’accès GOVATHON 2025")
                .build();

        Request request = new Request.Builder()
                .url("https://api.ultramsg.com/instance40778/messages/image")
                .post(body)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build();

        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());
    }


}
