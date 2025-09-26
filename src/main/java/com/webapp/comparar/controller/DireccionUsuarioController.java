package com.webapp.comparar.controller;

import com.webapp.comparar.model.DireccionUsuario;
import com.webapp.comparar.dto.DireccionUsuarioDTO;
import com.webapp.comparar.repository.DireccionUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/direcciones")
public class DireccionUsuarioController {

    @Autowired
    private DireccionUsuarioRepository direccionUsuarioRepository;

    // Obtener todas las direcciones de un usuario
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<DireccionUsuario>> getDireccionesByUsuario(@PathVariable Long idUsuario) {
        try {
            List<DireccionUsuario> direcciones = direccionUsuarioRepository.findByIdUsuarioOrderByIdDireccionUsuarioDesc(idUsuario);
            return ResponseEntity.ok(direcciones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Obtener la última dirección usada por un usuario
    @GetMapping("/usuario/{idUsuario}/ultima")
    public ResponseEntity<DireccionUsuario> getUltimaDireccion(@PathVariable Long idUsuario) {
        try {
            List<DireccionUsuario> direcciones = direccionUsuarioRepository.findUltimaDireccionByUsuario(idUsuario);
            if (direcciones.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(direcciones.get(0));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Guardar nueva dirección
    @PostMapping
    public ResponseEntity<DireccionUsuario> saveDireccion(@RequestBody DireccionUsuarioDTO direccionDTO) {
        try {
            // Validaciones básicas
            if (direccionDTO.getIdUsuario() == null || direccionDTO.getNombreDireccion() == null) {
                return ResponseEntity.badRequest().build();
            }

            System.out.println("Guardando dirección: " + direccionDTO.getNombreDireccion() +
                    " para usuario: " + direccionDTO.getIdUsuario());

            // Verificar si ya existe una dirección con el mismo nombre para este usuario
            Optional<DireccionUsuario> existing = direccionUsuarioRepository
                    .findByIdUsuarioAndNombreDireccion(direccionDTO.getIdUsuario(), direccionDTO.getNombreDireccion());

            DireccionUsuario saved;
            if (existing.isPresent()) {
                // Actualizar la dirección existente
                DireccionUsuario direccionExistente = existing.get();
                direccionExistente.setLatitud(direccionDTO.getLatitud());
                direccionExistente.setLongitud(direccionDTO.getLongitud());
                saved = direccionUsuarioRepository.save(direccionExistente);
                System.out.println("Dirección actualizada: " + saved.getIdDireccionUsuario());
            } else {
                // Crear nueva dirección
                DireccionUsuario nuevaDireccion = new DireccionUsuario();
                nuevaDireccion.setIdUsuario(direccionDTO.getIdUsuario());
                nuevaDireccion.setNombreDireccion(direccionDTO.getNombreDireccion());
                nuevaDireccion.setLatitud(direccionDTO.getLatitud());
                nuevaDireccion.setLongitud(direccionDTO.getLongitud());

                saved = direccionUsuarioRepository.save(nuevaDireccion);
                System.out.println("Nueva dirección guardada: " + saved.getIdDireccionUsuario());
            }

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            System.err.println("Error guardando dirección: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    // Actualizar dirección existente
    @PutMapping("/{id}")
    public ResponseEntity<DireccionUsuario> updateDireccion(@PathVariable Long id, @RequestBody DireccionUsuarioDTO direccionDTO) {
        try {
            Optional<DireccionUsuario> existing = direccionUsuarioRepository.findById(id);
            if (existing.isPresent()) {
                DireccionUsuario direccion = existing.get();
                direccion.setNombreDireccion(direccionDTO.getNombreDireccion());
                direccion.setLatitud(direccionDTO.getLatitud());
                direccion.setLongitud(direccionDTO.getLongitud());

                DireccionUsuario updated = direccionUsuarioRepository.save(direccion);
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Eliminar dirección
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDireccion(@PathVariable Long id) {
        try {
            if (direccionUsuarioRepository.existsById(id)) {
                direccionUsuarioRepository.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Endpoint simple para probar la conexión
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Direcciones API funcionando correctamente");
    }
}