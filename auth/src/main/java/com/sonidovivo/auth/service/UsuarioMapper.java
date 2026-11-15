package com.sonidovivo.auth.service;

import java.util.List;

import com.sonidovivo.auth.dto.UsuarioResponseDTO;
import com.sonidovivo.auth.model.Usuario;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static UsuarioResponseDTO aResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getRun(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getEmail(),
                usuario.getFechaNacimiento(),
                usuario.getRegion(),
                usuario.getComuna(),
                usuario.getDireccion(),
                usuario.getRol().name(),
                usuario.getEstado().name(),
                List.copyOf(usuario.getAuditoria()),
                usuario.isRequiereCambioClave());
    }
}
