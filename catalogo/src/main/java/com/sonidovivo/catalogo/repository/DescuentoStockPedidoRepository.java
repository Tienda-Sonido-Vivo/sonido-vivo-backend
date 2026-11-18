package com.sonidovivo.catalogo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sonidovivo.catalogo.model.DescuentoStockPedido;

public interface DescuentoStockPedidoRepository extends JpaRepository<DescuentoStockPedido, Long> {

    Optional<DescuentoStockPedido> findByCodigoPedido(String codigoPedido);
}
