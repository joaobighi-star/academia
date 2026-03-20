package com.academia.controller.api;

import com.academia.model.*;
import com.academia.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/alunos")
public class AlunoRestController {

    private final AlunoRepository alunoRepository;
    private final AulaParticularRepository aulaParticularRepository;
    private final DisponibilidadeRepository disponibilidadeRepository;

    public AlunoRestController(AlunoRepository alunoRepository, 
                               AulaParticularRepository aulaParticularRepository,
                               DisponibilidadeRepository disponibilidadeRepository) {
        this.alunoRepository = alunoRepository;
        this.aulaParticularRepository = aulaParticularRepository;
        this.disponibilidadeRepository = disponibilidadeRepository;
    }

    // Listar aulas do aluno logado
    @GetMapping("/{id}/minhas-aulas")
    public List<AulaParticular> minhasAulas(@PathVariable Long id) {
        return aulaParticularRepository.findByAlunoId(id);
    }

    // Solicitar Aula
    @PostMapping("/{alunoId}/solicitar-aula/{disponibilidadeId}")
    public ResponseEntity<?> solicitar(@PathVariable Long alunoId, @PathVariable Long disponibilidadeId) {
        Aluno aluno = alunoRepository.findById(alunoId).orElseThrow();
        Disponibilidade disp = disponibilidadeRepository.findById(disponibilidadeId).orElseThrow();

        if (disp.isReservado()) return ResponseEntity.badRequest().body("Horário já reservado");

        AulaParticular aula = new AulaParticular();
        aula.setAluno(aluno);
        aula.setProfessor(disp.getProfessor());
        aula.setDisponibilidade(disp);
        aula.setDataHora(LocalDateTime.of(disp.getData(), java.time.LocalTime.of(disp.getHora(), 0)));
        aula.setStatus("PENDENTE");

        disp.setReservado(true);
        disp.setAluno(aluno);

        disponibilidadeRepository.save(disp);
        return ResponseEntity.ok(aulaParticularRepository.save(aula));
    }

    // ... Seus outros métodos (listar, criar, etc) permanecem abaixo ...
}