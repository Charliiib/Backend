package com.webapp.comparar.controller;

import com.webapp.comparar.model.Sucursal;
import com.webapp.comparar.repository.SucursalRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sucursales")
public class SucursalController {

    @Autowired
    private SucursalRepository sucursalRepository;

    // Obtener todas las sucursales
    @GetMapping
    public List<Sucursal> getAllSucursales() {
        return sucursalRepository.findAll();
    }

    // Crear una nueva sucursal
    @PostMapping
    public Sucursal createSucursal(@RequestBody Sucursal sucursal) {
        return sucursalRepository.save(sucursal);
    }

    // Obtener una sucursal por ID compuesto
    @GetMapping("/{idComercio}/{idBandera}/{idSucursal}")
    public ResponseEntity<Sucursal> getSucursalById(
            @PathVariable Long idComercio,
            @PathVariable Long idBandera,
            @PathVariable Long idSucursal) {

        Sucursal.SucursalId id = new Sucursal.SucursalId(idComercio, idBandera, idSucursal);
        return sucursalRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Actualizar una sucursal
    @PutMapping("/{idComercio}/{idBandera}/{idSucursal}")
    public ResponseEntity<Sucursal> updateSucursal(
            @PathVariable Long idComercio,
            @PathVariable Long idBandera,
            @PathVariable Long idSucursal,
            @RequestBody Sucursal sucursalDetails) {

        Sucursal.SucursalId id = new Sucursal.SucursalId(idComercio, idBandera, idSucursal);
        return sucursalRepository.findById(id)
                .map(sucursal -> {
                    sucursal.setBarrio(sucursalDetails.getBarrio());
                    sucursal.setSucursalesNombre(sucursalDetails.getSucursalesNombre());
                    Sucursal updatedSucursal = sucursalRepository.save(sucursal);
                    return ResponseEntity.ok(updatedSucursal);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Eliminar una sucursal
    @DeleteMapping("/{idComercio}/{idBandera}/{idSucursal}")
    public ResponseEntity<Void> deleteSucursal(
            @PathVariable Long idComercio,
            @PathVariable Long idBandera,
            @PathVariable Long idSucursal) {

        Sucursal.SucursalId id = new Sucursal.SucursalId(idComercio, idBandera, idSucursal);
        if (!sucursalRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        sucursalRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Método adicional para buscar sucursales por barrio
    @GetMapping("/por-barrio/{idBarrio}")
    public List<Sucursal> getSucursalesByBarrio(@PathVariable Long idBarrio) {
        return sucursalRepository.findByBarrioIdBarrios(idBarrio);
    }

    @GetMapping("/cercanas")
    public ResponseEntity<List<Sucursal>> getSucursalesCercanas(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false, defaultValue = "10") int limit) {

        // 1. Obtener todas las sucursales
        List<Sucursal> todasSucursales = sucursalRepository.findAll();

        // 2. Calcular distancia para cada sucursal y filtrar las más cercanas
        List<Sucursal> sucursalesCercanas = todasSucursales.stream()
                .filter(s -> s.getSucursalesLatitud() != null && s.getSucursalesLongitud() != null)
                .map(s -> {
                    double distancia = calcularDistancia(
                            lat, lng,
                            s.getSucursalesLatitud(),
                            s.getSucursalesLongitud()
                    );
                    // Crear una copia para no modificar la entidad original
                    Sucursal sucursalConDistancia = new Sucursal();
                    BeanUtils.copyProperties(s, sucursalConDistancia);
                    sucursalConDistancia.setDistancia(distancia);
                    return sucursalConDistancia;
                })
                .sorted(Comparator.comparingDouble(Sucursal::getDistancia))
                .limit(limit)
                .collect(Collectors.toList());

        return ResponseEntity.ok(sucursalesCercanas);
    }

    // Método para calcular distancia (Haversine formula)
    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }


}