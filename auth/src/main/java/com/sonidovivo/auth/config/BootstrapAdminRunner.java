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

            administrador.setPasswordHash(passwordEncoder.encode(claveAdministrador));
            administrador.setEstado(EstadoUsuario.ACTIVO);
            administrador.setRequiereCambioClave(false);
            administrador.agregarAuditoria("Clave inicial configurada por variables de entorno");
            usuarioRepository.save(administrador);
        }
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
