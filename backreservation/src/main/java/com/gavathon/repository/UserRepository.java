package com.gavathon.repository;

import com.gavathon.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);

    Optional<User> findByEmail(String email);

    Optional<User> findByTelephone(String telephone);

}
