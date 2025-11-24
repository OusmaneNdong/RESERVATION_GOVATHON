package com.gavathon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto{

    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String fonction;
    private String codeOpt;
    private boolean isPresenct;
    private boolean isComplet;
    private QrcodeDto qrcode;
}