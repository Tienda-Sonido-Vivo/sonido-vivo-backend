package com.sonidovivo.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sonidovivo.auth.model.EstadoUsuario;
import com.sonidovivo.auth.model.RolUsuario;
import com.sonidovivo.auth.model.Usuario;
import com.sonidovivo.auth.repository.UsuarioRepository;

@Component
public class BootstrapAdminRunner implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String correoAdministrador;
    private final String claveAdministrador;

    public BootstrapAdminRunner(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin-email:admin@demo.cl}") String correoAdministrador,
            @Value("${app.bootstrap.admin-password:}") String claveAdministrador) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.correoAdministrador = correoAdministrador;
        this.claveAdministrador = claveAdministrador;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Usuario administrador = usuarioRepository.findByEmailIgnoreCase(correoAdministrador)
                .orElseGet(() -> crearAdministrador());

        if (administrador.getRol() != RolUsuario.ADMIN) {
            throw new IllegalStateException("La cuenta de inicio debe tener el rol ADMIN.");
        }

        if (administrador.getPasswordHash() == null) {
            if (claveAdministrador.isBlank()) {
                throw new IllegalStateException(
                        "Define ADMIN_BOOTSTRAP_PASSWORD para activar la cuenta administradora inicial.");
            }
            if (claveAdministrador.length() < 4 || claveAdministrador.length() > 10) {
                throw new IllegalStateException(
                        "ADMIN_BOOTSTRAP_PASSWORD debe tener entre 4 y 10 caracteres.");
            }

            administrador.setPasswordHash(passwordEncoder.encode(claveAdministrador));
            administrador.setEstado(EstadoUsuario.ACTIVO);
            administrador.setRequiereCambioClave(false);
            administrador.agregarAuditoria("Clave inicial configurada por variables de entorno");
            usuarioRepository.save(administrador);
        }

        configurarUsuariosDePrueba();
    }

    private void configurarUsuariosDePrueba() {
        configurarUsuarioDePrueba(
                "vendedor1@demo.cl",
                "987654321",
                "María",
                "González Soto",
                "Valparaíso",
                "Viña del Mar",
                "Avenida Demo 456",
                RolUsuario.VENDEDOR);

        configurarUsuarioDePrueba(
                "vendedor2@demo.cl",
                "112233445",
                "Pedro",
                "Ramírez López",
                "Biobío",
                "Concepción",
                "Calle Principal 789",
                RolUsuario.VENDEDOR);

        configurarUsuarioDePrueba(
                "usuario@demo.cl",
                "556677889",
                "Ana",
                "Martínez Díaz",
                "Metropolitana de Santiago",
                "Ñuñoa",
                "Avenida Siempre Viva 123",
                RolUsuario.CLIENTE);
    }

    private void configurarUsuarioDePrueba(
            String correo,
            String run,
            String nombre,
            String apellidos,
            String region,
            String comuna,
            String direccion,
            RolUsuario rol) {

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(correo)
                .orElseGet(() -> crearUsuarioDePrueba(
                        correo, run, nombre, apellidos, region, comuna, direccion, rol));

        if (usuario.getPasswordHash() == null
                || !passwordEncoder.matches("1234", usuario.getPasswordHash())) {
            usuario.setPasswordHash(passwordEncoder.encode("1234"));
            usuario.setEstado(EstadoUsuario.ACTIVO);
            usuario.setRequiereCambioClave(false);
            usuario.agregarAuditoria("Clave de prueba 1234 configurada automáticamente");
            usuarioRepository.save(usuario);
        }
    }

    private Usuario crearUsuarioDePrueba(
            String correo,
            String run,
            String nombre,
            String apellidos,
            String region,
            String comuna,
            String direccion,
            RolUsuario rol) {

        Usuario usuario = new Usuario();
        usuario.setRun(run);
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setEmail(correo);
        usuario.setRegion(region);
        usuario.setComuna(comuna);
        usuario.setDireccion(direccion);
        usuario.setRol(rol);
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setRequiereCambioClave(false);
        usuario.setPasswordHash(passwordEncoder.encode("1234"));
        usuario.agregarAuditoria("Usuario de prueba creado automáticamente");
        return usuarioRepository.save(usuario);
    }

    private Usuario crearAdministrador() {
        Usuario usuario = new Usuario();
        usuario.setRun("000000000");
        usuario.setNombre("Administrador");
        usuario.setApellidos("Sonido Vivo");
        usuario.setEmail(correoAdministrador);
        usuario.setRegion("Metropolitana de Santiago");
        usuario.setComuna("Santiago");
        usuario.setDireccion("Cuenta de administración");
        usuario.setRol(RolUsuario.ADMIN);
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setRequiereCambioClave(true);
        return usuarioRepository.save(usuario);
    }
}
