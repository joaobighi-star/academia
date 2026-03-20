package com.academia.controller.api;

import com.academia.model.Usuario;
import com.academia.repository.UsuarioRepository;
import com.academia.repository.AlunoRepository;
import com.academia.repository.ProfessorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginRestController {

    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginRestController(UsuarioRepository usuarioRepository, 
                               AlunoRepository alunoRepository,
                               ProfessorRepository professorRepository,
                               PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String senha = credentials.get("senha");

        return usuarioRepository.findByEmail(email)
            .map(user -> {
                if (passwordEncoder.matches(senha, user.getSenha())) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("idUsuario", user.getId());
                    response.put("email", user.getEmail());
                    response.put("role", user.getRolePrincipal());

                    // Busca o ID específico da tabela Aluno ou Professor
                    if (user.getRolePrincipal().equals("ROLE_ALUNO")) {
                        alunoRepository.findByUsuarioEmail(user.getEmail())
                                       .ifPresent(a -> response.put("idVinculado", a.getId()));
                    } else if (user.getRolePrincipal().equals("ROLE_PROFESSOR")) {
                        professorRepository.findByUsuarioEmail(user.getEmail())
                                           .ifPresent(p -> response.put("idVinculado", p.getId()));
                    }

                    return ResponseEntity.ok(response);
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Senha incorreta");
            })
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não encontrado"));
    }
}