package com.academia.controller.api;

import com.academia.model.AulaParticular;
import com.academia.repository.AulaParticularRepository;
import com.academia.repository.ProfessorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/professores")
public class ProfessorRestController {

    private final ProfessorRepository professorRepository;
    private final AulaParticularRepository aulaParticularRepository;

    public ProfessorRestController(ProfessorRepository professorRepository, 
                                   AulaParticularRepository aulaParticularRepository) {
        this.professorRepository = professorRepository;
        this.aulaParticularRepository = aulaParticularRepository;
    }

    // Listar solicitações para o professor logado
    @GetMapping("/{id}/solicitacoes")
    public List<AulaParticular> verSolicitacoes(@PathVariable Long id) {
        return aulaParticularRepository.findByProfessorIdAndStatus(id, "PENDENTE");
    }

    // Aprovar ou Rejeitar
    @PutMapping("/responder-aula/{aulaId}")
    public ResponseEntity<?> responder(@PathVariable Long aulaId, @RequestParam String novoStatus) {
        AulaParticular aula = aulaParticularRepository.findById(aulaId).orElseThrow();
        aula.setStatus(novoStatus); // CONFIRMADA ou REJEITADA
        
        if (novoStatus.equals("CONFIRMADA")) {
            aula.getDisponibilidade().setConfirmado(true);
        } else {
            aula.getDisponibilidade().setReservado(false); // Libera o horário se rejeitar
            aula.getDisponibilidade().setAluno(null);
        }

        return ResponseEntity.ok(aulaParticularRepository.save(aula));
    }
}