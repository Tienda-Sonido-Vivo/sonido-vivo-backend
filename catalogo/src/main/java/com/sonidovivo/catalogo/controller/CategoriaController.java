package com.sonidovivo.catalogo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sonidovivo.catalogo.dto.CategoriaResponseDTO;
import com.sonidovivo.catalogo.service.CategoriaService;

@RestController
@Tag(name = "Categorías", description = "Consulta de las categorías disponibles en el catálogo.")
@RequestMapping("/api/catalogo/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }
    @Operation(summary = "Listar categorías",
            description = "Obtiene todas las categorías registradas en el catálogo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de categorías obtenido correctamente.")
    })

    @GetMapping
    public List<CategoriaResponseDTO> listar() {
        return categoriaService.listar();
    }
}
