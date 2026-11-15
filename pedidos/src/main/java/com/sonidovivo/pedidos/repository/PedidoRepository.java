package com.sonidovivo.pedidos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sonidovivo.pedidos.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = "productos")
    List<Pedido> findAllByOrderByFechaRegistroDesc();

    @EntityGraph(attributePaths = "productos")
    List<Pedido> findByCorreoClienteOrderByFechaRegistroDesc(String correoCliente);

    @EntityGraph(attributePaths = "productos")
    Optional<Pedido> findByCodigo(String codigo);

    @EntityGraph(attributePaths = "productos")
    List<Pedido> findByPagadoTrueAndVentaRegistradaFalse();

    @EntityGraph(attributePaths = "productos")
    List<Pedido> findByPagoRegistradoFalse();
}
