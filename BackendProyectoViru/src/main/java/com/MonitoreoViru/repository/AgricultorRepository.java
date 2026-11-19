package com.MonitoreoViru.repository;

import com.MonitoreoViru.models.Agricultor;
import com.MonitoreoViru.models.JuntaRegante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgricultorRepository extends JpaRepository<Agricultor, Integer> {
}
