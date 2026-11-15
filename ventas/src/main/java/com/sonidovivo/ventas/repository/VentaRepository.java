package com.sonidovivo.ventas.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sonidovivo.ventas.model.Venta;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    Optional<Venta> findByCodigoPedido(String codigoPedido);

    @Query("""
            SELECT COALESCE(SUM(venta.total), 0)
            FROM Venta venta
            WHERE venta.fechaVenta >= :inicio
              AND venta.fechaVenta < :fin
            """)
    BigDecimal sumarVentas(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("""
            SELECT COUNT(venta)
            FROM Venta venta
            WHERE venta.fechaVenta >= :inicio
              AND venta.fechaVenta < :fin
            """)
    long contarVentas(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("""
            SELECT COALESCE(SUM(venta.cantidadProductos), 0)
            FROM Venta venta
            WHERE venta.fechaVenta >= :inicio
              AND venta.fechaVenta < :fin
            """)
    long sumarUnidades(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}
