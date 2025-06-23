package com.webapp.comparar.controller;

import com.webapp.comparar.dto.JwtAuthenticationResponse;
import com.webapp.comparar.dto.LoginRequest;
import com.webapp.comparar.dto.RegisterRequest;
import com.webapp.comparar.dto.UserResponse;
import com.webapp.comparar.model.Usuario;
import com.webapp.comparar.security.JwtTokenProvider;
import com.webapp.comparar.security.UserPrincipal;
import com.webapp.comparar.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;


    // Inyección de dependencias a través del constructor
    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          UsuarioService usuarioService,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Validated @RequestBody RegisterRequest registerRequest) {
        try {
            if (usuarioService.existeEmail(registerRequest.getEmail())) {
                return ResponseEntity.badRequest().body("El email ya está registrado");
            }

            Usuario usuario = new Usuario();
            usuario.setNombreUsuario(registerRequest.getNombre());
            usuario.setApellidoUsuario(registerRequest.getApellido());
            usuario.setEmailUsuario(registerRequest.getEmail());
            usuario.setPassUsuario(passwordEncoder.encode(registerRequest.getPassword()));

            Usuario usuarioGuardado = usuarioService.save(usuario);

            // Verifica que el usuario se guardó correctamente
            if (usuarioGuardado.getIdUsuario() == null) {
                throw new RuntimeException("Error al guardar el usuario");
            }

            return ResponseEntity.ok(new UserResponse(
                    usuarioGuardado.getIdUsuario(),
                    usuarioGuardado.getNombreUsuario(),
                    usuarioGuardado.getApellidoUsuario(),
                    usuarioGuardado.getEmailUsuario()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al registrar el usuario: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            // 1. Verificar que el usuario existe
            Usuario usuario = usuarioService.buscarPorEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));

            // 2. Autenticar
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // 3. Generar token
            String jwt = jwtTokenProvider.generateToken(authentication);

            // 4. Retornar respuesta
            return ResponseEntity.ok(new JwtAuthenticationResponse(
                    jwt,
                    new UserResponse(
                            usuario.getIdUsuario(),
                            usuario.getNombreUsuario(),
                            usuario.getApellidoUsuario(),
                            usuario.getEmailUsuario()
                    )
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error durante la autenticación: " + e.getMessage());
        }
    }
}