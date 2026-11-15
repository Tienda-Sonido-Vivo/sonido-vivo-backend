package com.sonidovivo.pagos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sonidovivo.pagos.model.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByCodigoPedido(String codigoPedido);

    List<Pago> findByCorreoClienteIgnoreCaseOrderByFechaPagoDesc(String correoCliente);

    List<Pago> findAllByOrderByFechaPagoDesc();
}
