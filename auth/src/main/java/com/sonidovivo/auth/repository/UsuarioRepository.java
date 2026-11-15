package com.sonidovivo.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sonidovivo.auth.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByRun(String run);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRun(String run);
}
