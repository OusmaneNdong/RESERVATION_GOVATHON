package com.gavathon.dto;


import com.gavathon.entity.User;
import jakarta.persistence.Entity;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class QrcodeDto {

    private int id;
    private String qrcodename;
    private UserDto user;
}
