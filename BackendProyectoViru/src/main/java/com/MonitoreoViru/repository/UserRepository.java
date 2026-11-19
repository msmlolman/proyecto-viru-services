package com.MonitoreoViru.repository;

import com.MonitoreoViru.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByCorreo(String correo);
}