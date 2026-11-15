package com.sonidovivo.ventas.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sonidovivo.ventas.dto.ResumenVentasDTO;
import com.sonidovivo.ventas.dto.VentaRegistroRequestDTO;
import com.sonidovivo.ventas.model.Venta;
import com.sonidovivo.ventas.repository.VentaRepository;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;

    public VentaService(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    @Transactional
    public void registrar(VentaRegistroRequestDTO request) {
        if (ventaRepository.findByCodigoPedido(request.codigoPedido()).isPresent()) {
            return;
        }

        Venta venta = new Venta();
        venta.setCodigoPedido(request.codigoPedido());
        venta.setFechaVenta(request.fecha());
        venta.setTotal(request.total());
        venta.setCantidadProductos(request.cantidadProductos());

        ventaRepository.save(venta);
    }

    @Transactional(readOnly = true)
    public ResumenVentasDTO resumenDiario(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();

        return construirResumen(inicio, fin);
    }

    @Transactional(readOnly = true)
    public ResumenVentasDTO resumenMensual(int anio, int mes) {
        LocalDate primerDia = LocalDate.of(anio, mes, 1);
        LocalDateTime inicio = primerDia.atStartOfDay();
        LocalDateTime fin = primerDia.plusMonths(1).atStartOfDay();

        return construirResumen(inicio, fin);
    }

    private ResumenVentasDTO construirResumen(LocalDateTime inicio, LocalDateTime fin) {
        return new ResumenVentasDTO(
                inicio,
                fin,
                ventaRepository.contarVentas(inicio, fin),
                ventaRepository.sumarUnidades(inicio, fin),
                ventaRepository.sumarVentas(inicio, fin));
    }
}
