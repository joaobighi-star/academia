package com.academia.controller.api;

import com.academia.enums.Perfil;
import com.academia.model.Professor;
import com.academia.model.Usuario;
import com.academia.repository.ProfessorRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/professores")
public class ProfessorRestController {

    private final ProfessorRepository professorRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfessorRestController(ProfessorRepository professorRepository,
                                   PasswordEncoder passwordEncoder) {
        this.professorRepository = professorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================
    // ✅ CREATE
    // =========================
    @PostMapping
    public ResponseEntity<Professor> criar(@RequestBody Professor professor) {

        // Criando usuário automaticamente
        Usuario usuario = new Usuario();
        usuario.setEmail(professor.getUsuario().getEmail());
        usuario.setSenha(passwordEncoder.encode("prof123"));
        usuario.setPerfis(Set.of(Perfil.ROLE_PROFESSOR));
        usuario.setSenhaAlteradaPeloAdmin(false);

        professor.setUsuario(usuario);

        Professor salvo = professorRepository.save(professor);

        return ResponseEntity
                .created(URI.create("/api/professores/" + salvo.getId()))
                .body(salvo);
    }

    // =========================
    // ✅ READ ALL
    // =========================
    @GetMapping
    public ResponseEntity<List<Professor>> listar() {
        List<Professor> lista = professorRepository.findAll();
        return ResponseEntity.ok(lista);
    }

    // =========================
    // ✅ READ BY ID
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<Professor> buscarPorId(@PathVariable Long id) {

        Optional<Professor> professor = professorRepository.findById(id);

        if (professor.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(professor.get());
    }

    // =========================
    // ✅ UPDATE
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<Professor> atualizar(@PathVariable Long id,
                                               @RequestBody Professor dadosAtualizados) {

        Optional<Professor> professorOptional = professorRepository.findById(id);

        if (professorOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Professor professor = professorOptional.get();

        professor.setNome(dadosAtualizados.getNome());
        professor.setEspecialidade(dadosAtualizados.getEspecialidade());
        professor.setDisponibilidadeAulasParticulares(dadosAtualizados.getDisponibilidadeAulasParticulares());

        Professor atualizado = professorRepository.save(professor);

        return ResponseEntity.ok(atualizado);
    }

    // =========================
    // ✅ DELETE
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {

        if (!professorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        professorRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}