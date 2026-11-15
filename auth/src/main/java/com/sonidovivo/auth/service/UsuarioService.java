package com.sonidovivo.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sonidovivo.auth.dto.ActivacionResponseDTO;
import com.sonidovivo.auth.dto.UsuarioAdminRequestDTO;
import com.sonidovivo.auth.dto.UsuarioActualizarRequestDTO;
import com.sonidovivo.auth.dto.UsuarioResponseDTO;
import com.sonidovivo.auth.model.EstadoUsuario;
import com.sonidovivo.auth.model.RolUsuario;
import com.sonidovivo.auth.model.Usuario;
import com.sonidovivo.auth.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ActivacionService activacionService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            ActivacionService activacionService) {

        this.usuarioRepository = usuarioRepository;
        this.activacionService = activacionService;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioMapper::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorRun(String run) {
        return UsuarioMapper.aResponse(obtenerUsuario(run));
    }

    @Transactional
    public UsuarioResponseDTO crear(UsuarioAdminRequestDTO request) {
        String run = RutChileno.normalizar(request.run());
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (!RutChileno.esValido(request.run())) {
            throw new IllegalArgumentException("El RUN ingresado no es válido.");
        }

        if (usuarioRepository.existsByRun(run)) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese RUN.");
        }

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo.");
        }

        Usuario usuario = new Usuario();
        usuario.setRun(run);
        usuario.setNombre(request.nombre().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setEmail(email);
        usuario.setFechaNacimiento(request.fechaNacimiento());
        usuario.setRegion(request.region().trim());
        usuario.setComuna(request.comuna().trim());
        usuario.setDireccion(request.direccion().trim());
        usuario.setRol(request.rol());
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setRequiereCambioClave(true);
        usuario.agregarAuditoria("Cuenta creada por administración " + LocalDateTime.now());

        return UsuarioMapper.aResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO actualizar(
            String run,
            UsuarioActualizarRequestDTO request,
            String correoAdministrador) {

        Usuario usuario = obtenerUsuario(run);
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        usuarioRepository.findByEmailIgnoreCase(email)
                .filter(otro -> !otro.getId().equals(usuario.getId()))
                .ifPresent(otro -> {
                    throw new IllegalArgumentException("El correo ya pertenece a otro usuario.");
                });

        if (usuario.getEmail().equalsIgnoreCase(correoAdministrador)
                && (request.estado() == EstadoUsuario.INACTIVO || request.rol() != RolUsuario.ADMIN)) {
            throw new AccessDeniedException("No puedes desactivar ni quitar el rol de tu propia cuenta.");
        }

        usuario.setNombre(request.nombre().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setEmail(email);
        usuario.setFechaNacimiento(request.fechaNacimiento());
        usuario.setRegion(request.region().trim());
        usuario.setComuna(request.comuna().trim());
        usuario.setDireccion(request.direccion().trim());
        usuario.setRol(request.rol());
        usuario.setEstado(request.estado());
        usuario.agregarAuditoria("Datos actualizados " + LocalDateTime.now());

        return UsuarioMapper.aResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO cambiarEstado(String run, EstadoUsuario estado, String correoAdministrador) {
        Usuario usuario = obtenerUsuario(run);

        if (usuario.getEmail().equalsIgnoreCase(correoAdministrador)
                && estado == EstadoUsuario.INACTIVO) {
            throw new AccessDeniedException("No puedes desactivar tu propia cuenta.");
        }

        usuario.setEstado(estado);
        usuario.agregarAuditoria("Estado cambiado a " + estado + " " + LocalDateTime.now());

        return UsuarioMapper.aResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public ActivacionResponseDTO generarActivacion(String run) {
        Usuario usuario = obtenerUsuario(run);

        if (!usuario.isRequiereCambioClave()) {
            throw new IllegalArgumentException("La cuenta ya fue activada.");
        }

        return activacionService.generarToken(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(correo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario autenticado."));

        return UsuarioMapper.aResponse(usuario);
    }

    private Usuario obtenerUsuario(String run) {
        return usuarioRepository.findByRun(normalizarRun(run))
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario solicitado."));
    }

    private String normalizarRun(String run) {
        return RutChileno.normalizar(run);
    }
}
