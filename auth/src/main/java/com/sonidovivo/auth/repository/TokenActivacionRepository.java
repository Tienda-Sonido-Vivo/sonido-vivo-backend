package com.sonidovivo.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sonidovivo.auth.model.TokenActivacion;

public interface TokenActivacionRepository extends JpaRepository<TokenActivacion, Long> {

    Optional<TokenActivacion> findByTokenHash(String tokenHash);

    void deleteByUsuario_IdAndFechaUsoIsNull(Long usuarioId);
}
