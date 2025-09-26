package com.webapp.comparar.service;

import com.webapp.comparar.dto.SucursalFavoritaDTO;
import com.webapp.comparar.model.SucursalFavorita;
import com.webapp.comparar.repository.SucursalFavoritaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SucursalFavoritaService {

    @Autowired
    private SucursalFavoritaRepository sucursalFavoritaRepository;

    // Obtener todas las sucursales favoritas de un usuario
    public List<SucursalFavoritaDTO> obtenerSucursalesFavoritasPorUsuario(Long idUsuario) {
        List<SucursalFavorita> favoritas = sucursalFavoritaRepository.findByIdUsuario(idUsuario);
        return favoritas.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Agregar sucursal a favoritos
    public SucursalFavoritaDTO agregarSucursalFavorita(SucursalFavoritaDTO sucursalFavoritaDTO) {
        // Verificar si ya existe
        boolean existe = sucursalFavoritaRepository.findByUsuarioAndSucursal(
                sucursalFavoritaDTO.getIdUsuario(),
                sucursalFavoritaDTO.getIdComercio(),
                sucursalFavoritaDTO.getIdBandera(),
                sucursalFavoritaDTO.getIdSucursal()
        ).isPresent();

        if (existe) {
            throw new RuntimeException("La sucursal ya está en favoritos");
        }

        SucursalFavorita sucursalFavorita = convertToEntity(sucursalFavoritaDTO);
        SucursalFavorita guardada = sucursalFavoritaRepository.save(sucursalFavorita);
        return convertToDTO(guardada);
    }

    // Eliminar sucursal de favoritos
    public boolean eliminarSucursalFavorita(Long idUsuario, Long idComercio, Long idBandera, Long idSucursal) {
        int eliminados = sucursalFavoritaRepository.deleteByUsuarioAndSucursal(idUsuario, idComercio, idBandera, idSucursal);
        return eliminados > 0;
    }

    // Verificar si una sucursal es favorita
    public boolean esSucursalFavorita(Long idUsuario, Long idComercio, Long idBandera, Long idSucursal) {
        return sucursalFavoritaRepository.findByUsuarioAndSucursal(idUsuario, idComercio, idBandera, idSucursal).isPresent();
    }

    // Métodos de conversión
    private SucursalFavoritaDTO convertToDTO(SucursalFavorita sucursalFavorita) {
        SucursalFavoritaDTO dto = new SucursalFavoritaDTO();
        dto.setIdFavorita(sucursalFavorita.getIdFavorita());
        dto.setIdUsuario(sucursalFavorita.getIdUsuario());
        dto.setIdComercio(sucursalFavorita.getIdComercio());
        dto.setIdBandera(sucursalFavorita.getIdBandera());
        dto.setIdSucursal(sucursalFavorita.getIdSucursal());
        dto.setSucursalNombre(sucursalFavorita.getSucursalNombre());
        dto.setComercioNombre(sucursalFavorita.getComercioNombre());
        dto.setBarrioNombre(sucursalFavorita.getBarrioNombre());
        dto.setLatitud(sucursalFavorita.getLatitud());
        dto.setLongitud(sucursalFavorita.getLongitud());
        dto.setFechaAgregado(sucursalFavorita.getFechaAgregado());
        return dto;
    }

    private SucursalFavorita convertToEntity(SucursalFavoritaDTO dto) {
        SucursalFavorita entity = new SucursalFavorita();
        entity.setIdUsuario(dto.getIdUsuario());
        entity.setIdComercio(dto.getIdComercio());
        entity.setIdBandera(dto.getIdBandera());
        entity.setIdSucursal(dto.getIdSucursal());
        entity.setSucursalNombre(dto.getSucursalNombre());
        entity.setComercioNombre(dto.getComercioNombre());
        entity.setBarrioNombre(dto.getBarrioNombre());
        entity.setLatitud(dto.getLatitud());
        entity.setLongitud(dto.getLongitud());
        return entity;
    }
}