package com.webapp.comparar.service;


import com.webapp.comparar.model.Usuario;
import com.webapp.comparar.repository.UsuarioRepository;
import com.webapp.comparar.security.UserPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public JwtUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Verifica que la contraseña no sea nula
        if (usuario.getPassUsuario() == null) {
            throw new UsernameNotFoundException("Credenciales inválidas");
        }

        return new UserPrincipal(usuario);
    }
}