package com.sonidovivo.carrito.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sonidovivo.carrito.model.Carrito;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<Carrito> findByCorreoClienteIgnoreCase(String correoCliente);
}
