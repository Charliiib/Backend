package com.webapp.comparar.controller;



import com.webapp.comparar.dto.SucursalFavoritaDTO;
import com.webapp.comparar.service.SucursalFavoritaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sucursales-favoritas")
@CrossOrigin(origins = "http://localhost:3000")
public class SucursalFavoritaController {

    @Autowired
    private SucursalFavoritaService sucursalFavoritaService;

    // Obtener sucursales favoritas por usuario
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<SucursalFavoritaDTO>> obtenerSucursalesFavoritas(@PathVariable Long idUsuario) {
        try {
            List<SucursalFavoritaDTO> favoritas = sucursalFavoritaService.obtenerSucursalesFavoritasPorUsuario(idUsuario);
            return ResponseEntity.ok(favoritas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Agregar sucursal a favoritos
    @PostMapping("/agregar")
    public ResponseEntity<?> agregarSucursalFavorita(@RequestBody SucursalFavoritaDTO sucursalFavoritaDTO) {
        try {
            SucursalFavoritaDTO resultado = sucursalFavoritaService.agregarSucursalFavorita(sucursalFavoritaDTO);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Eliminar sucursal de favoritos
    @DeleteMapping("/eliminar")
    public ResponseEntity<?> eliminarSucursalFavorita(@RequestBody Map<String, Long> request) {
        try {
            Long idUsuario = request.get("idUsuario");
            Long idComercio = request.get("idComercio");
            Long idBandera = request.get("idBandera");
            Long idSucursal = request.get("idSucursal");

            boolean eliminado = sucursalFavoritaService.eliminarSucursalFavorita(
                    idUsuario, idComercio, idBandera, idSucursal);

            if (eliminado) {
                Map<String, String> response = new HashMap<>();
                response.put("mensaje", "Sucursal eliminada de favoritos");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Verificar si una sucursal es favorita
    @GetMapping("/verificar")
    public ResponseEntity<Boolean> verificarSucursalFavorita(
            @RequestParam Long idUsuario,
            @RequestParam Long idComercio,
            @RequestParam Long idBandera,
            @RequestParam Long idSucursal) {
        try {
            boolean esFavorita = sucursalFavoritaService.esSucursalFavorita(
                    idUsuario, idComercio, idBandera, idSucursal);
            return ResponseEntity.ok(esFavorita);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}