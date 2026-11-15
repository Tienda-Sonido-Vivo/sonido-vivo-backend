package com.sonidovivo.mensajeria.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sonidovivo.mensajeria.dto.MensajeRequestDTO;
import com.sonidovivo.mensajeria.dto.MensajeResponseDTO;
import com.sonidovivo.mensajeria.model.Mensaje;
import com.sonidovivo.mensajeria.model.TipoRemitente;
import com.sonidovivo.mensajeria.repository.MensajeRepository;

@Service
public class MensajeriaService {

    private final MensajeRepository mensajeRepository;

    public MensajeriaService(MensajeRepository mensajeRepository) {
        this.mensajeRepository = mensajeRepository;
    }

    @Transactional
    public MensajeResponseDTO crear(MensajeRequestDTO request, Authentication authentication) {
        Mensaje mensaje = new Mensaje();
        mensaje.setNombreCompleto(request.nombreCompleto().trim());
        mensaje.setCorreo(request.correo().trim().toLowerCase());
        mensaje.setMensaje(request.mensaje().trim());
        mensaje.setTipoMensaje(request.tipoMensaje());
        mensaje.setTipoRemitente(obtenerTipoRemitente(authentication));
        mensaje.setRevisado(false);

        return aResponse(mensajeRepository.save(mensaje));
    }

    @Transactional(readOnly = true)
    public List<MensajeResponseDTO> listar() {
        return mensajeRepository.findAllByOrderByFechaCreacionDesc()
                .stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional
    public MensajeResponseDTO cambiarRevision(Long id, boolean revisado) {
        Mensaje mensaje = mensajeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el mensaje solicitado."));

        mensaje.setRevisado(revisado);

        return aResponse(mensajeRepository.save(mensaje));
    }

    private TipoRemitente obtenerTipoRemitente(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))) {
            return TipoRemitente.CLIENTE;
        }

        for (var authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                return TipoRemitente.ADMIN;
            }

            if (authority.getAuthority().equals("ROLE_VENDEDOR")) {
                return TipoRemitente.VENDEDOR;
            }

            if (authority.getAuthority().equals("ROLE_CLIENTE")) {
                return TipoRemitente.CLIENTE;
            }
        }

        throw new AccessDeniedException("El rol autenticado no puede enviar mensajes.");
    }

    private MensajeResponseDTO aResponse(Mensaje mensaje) {
        return new MensajeResponseDTO(
                mensaje.getId(),
                mensaje.getNombreCompleto(),
                mensaje.getCorreo(),
                mensaje.getMensaje(),
                mensaje.getTipoMensaje().name(),
                mensaje.getTipoRemitente().name(),
                mensaje.isRevisado(),
                mensaje.getFechaCreacion());
    }
}
