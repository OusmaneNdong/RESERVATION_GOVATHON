package com.gavathon.repository;

import com.gavathon.entity.Qrcode;
import com.gavathon.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QrcodeRepository extends JpaRepository<Qrcode, Integer> {

    Optional<Qrcode> findByQrcodenameAndUserId(String qrcodename, Long userId);

    Optional<Qrcode> findByUser(User user);
}
