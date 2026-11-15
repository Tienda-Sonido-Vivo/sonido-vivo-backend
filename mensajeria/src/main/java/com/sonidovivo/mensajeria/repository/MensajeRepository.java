package com.sonidovivo.mensajeria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sonidovivo.mensajeria.model.Mensaje;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findAllByOrderByFechaCreacionDesc();
}
