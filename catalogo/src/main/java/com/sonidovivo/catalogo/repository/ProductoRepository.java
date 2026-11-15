package com.sonidovivo.catalogo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sonidovivo.catalogo.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Producto> findByCategoriaNombreIgnoreCase(String nombreCategoria);

    @Modifying
    @Query("""
            UPDATE Producto producto
            SET producto.stock = producto.stock - :cantidad, producto.version = producto.version + 1
            WHERE producto.codigo = :codigo
              AND producto.stock >= :cantidad
            """)
    int reservarStock(@Param("codigo") String codigo, @Param("cantidad") int cantidad);

    @Modifying
    @Query("""
            UPDATE Producto producto
            SET producto.stock = producto.stock + :cantidad, producto.version = producto.version + 1
            WHERE producto.codigo = :codigo
            """)
    int liberarStock(@Param("codigo") String codigo, @Param("cantidad") int cantidad);

    @Modifying
    @Query("""
            UPDATE Producto producto
            SET producto.stock = :stock, producto.version = producto.version + 1
            WHERE producto.codigo = :codigo
            """)
    int actualizarStock(@Param("codigo") String codigo, @Param("stock") int stock);
}
