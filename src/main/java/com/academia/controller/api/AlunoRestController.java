package com.academia.controller.api;

import com.academia.model.Aluno;
import com.academia.repository.AlunoRepository;
import com.academia.enums.Perfil;
import com.academia.enums.StatusMensalidade;
import com.academia.model.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/alunos")
public class AlunoRestController {

    private final AlunoRepository alunoRepository;
    private final PasswordEncoder passwordEncoder;

    public AlunoRestController(AlunoRepository alunoRepository,
                                PasswordEncoder passwordEncoder) {
        this.alunoRepository = alunoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ CREATE
    @PostMapping
    public ResponseEntity<Aluno> criar(@RequestBody Aluno aluno) {

        Usuario user = new Usuario();
        user.setEmail(aluno.getUsuario().getEmail());
        user.setSenha(passwordEncoder.encode(aluno.getCpf()));
        user.setPerfis(Set.of(Perfil.ROLE_ALUNO));
        user.setSenhaAlteradaPeloAdmin(false);

        aluno.setUsuario(user);
        aluno.setStatusMensalidade(StatusMensalidade.EM_DIA);
        aluno.setDataMatricula(LocalDate.now());

        Aluno salvo = alunoRepository.save(aluno);
        return ResponseEntity.status(201).body(salvo);
    }

    // ✅ READ ALL
    @GetMapping
    public ResponseEntity<List<Aluno>> listar() {
        return ResponseEntity.ok(alunoRepository.findAll());
    }

    // ✅ READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Aluno> buscarPorId(@PathVariable Long id) {
        return alunoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Aluno> atualizar(@PathVariable Long id,
                                           @RequestBody Aluno alunoAtualizado) {

        return alunoRepository.findById(id)
                .map(aluno -> {
                    aluno.setNome(alunoAtualizado.getNome());
                    aluno.setCpf(alunoAtualizado.getCpf());
                    aluno.setTelefone(alunoAtualizado.getTelefone());
                    aluno.setStatusMensalidade(alunoAtualizado.getStatusMensalidade());
                    aluno.setTurma(alunoAtualizado.getTurma());

                    Aluno salvo = alunoRepository.save(aluno);
                    return ResponseEntity.ok(salvo);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {

        if (!alunoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        alunoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}