package com.sonidovivo.pedidos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class DatosClientePedido {

    @Column(name = "cliente_nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "cliente_apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "cliente_telefono", nullable = false, length = 25)
    private String telefono;

    @Column(name = "cliente_correo", nullable = false, length = 150)
    private String correo;

    @Column(name = "cliente_region", nullable = false, length = 100)
    private String region;

    @Column(name = "cliente_comuna", nullable = false, length = 100)
    private String comuna;

    @Column(name = "cliente_direccion", nullable = false, length = 180)
    private String direccion;

    @Column(name = "cliente_numero_vivienda", length = 80)
    private String numeroVivienda;

    @Column(name = "cliente_codigo_postal", length = 20)
    private String codigoPostal;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getNumeroVivienda() {
        return numeroVivienda;
    }

    public void setNumeroVivienda(String numeroVivienda) {
        this.numeroVivienda = numeroVivienda;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }
}
