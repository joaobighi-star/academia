package com.academia.controller;

import com.academia.model.*;
import com.academia.repository.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/aluno")
public class AlunoController {

    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final AulaParticularRepository aulaParticularRepository;
    private final DisponibilidadeRepository disponibilidadeRepository;

    public AlunoController(AlunoRepository alunoRepository, 
                           ProfessorRepository professorRepository, 
                           AulaParticularRepository aulaParticularRepository,
                           DisponibilidadeRepository disponibilidadeRepository) {
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.aulaParticularRepository = aulaParticularRepository;
        this.disponibilidadeRepository = disponibilidadeRepository;
    }

    @GetMapping("/perfil")
    public String exibirPerfil(Model model, Authentication auth) {
        Aluno aluno = buscarAlunoLogado(auth);
        
        // Busca as aulas agendadas para exibir no perfil
        List<AulaParticular> minhasAulas = aulaParticularRepository.findByAlunoOrderByDataHoraAsc(aluno);
        
        model.addAttribute("aluno", aluno);
        model.addAttribute("aulas", minhasAulas);
        
        return "aluno/perfil";
    }

    @GetMapping("/foto/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> exibirFoto(@PathVariable Long id) {
        return alunoRepository.findById(id)
                .map(aluno -> {
                    if (aluno.getFoto() == null) return ResponseEntity.notFound().<byte[]>build();
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG)
                            .body(aluno.getFoto());
                }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/agendar-aula")
    public String selecionarProfessor(Model model) {
        model.addAttribute("professores", professorRepository.findAll());
        return "aluno/selecionar-professor";
    }

    @GetMapping("/agendar-aula/professor/{id}")
    public String verGradeProfessor(@PathVariable("id") Long professorId,
                                    @RequestParam(required = false) String dataBase, 
                                    Model model) {
        
        Professor prof = professorRepository.findById(professorId).orElseThrow();
        LocalDate dataRef = (dataBase == null) ? LocalDate.now() : LocalDate.parse(dataBase);
        LocalDate segundaFeira = dataRef.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        List<LocalDate> diasDaSemana = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            diasDaSemana.add(segundaFeira.plusDays(i));
        }

        List<Disponibilidade> disponibilidades = disponibilidadeRepository.findByProfessorAndDataBetween(
            prof, segundaFeira, segundaFeira.plusDays(6)
        );

        model.addAttribute("professor", prof);
        model.addAttribute("dias", diasDaSemana);
        model.addAttribute("horas", IntStream.rangeClosed(7, 21).boxed().toList());
        model.addAttribute("disponibilidades", disponibilidades);
        model.addAttribute("proximaSemana", dataRef.plusWeeks(1));
        model.addAttribute("semanaAnterior", dataRef.minusWeeks(1));

        return "aluno/grade-agendamento";
    }

    @PostMapping("/agendar-aula/confirmar")
    public String confirmarAgendamento(@RequestParam Long disponibilidadeId, Authentication auth) {
        Aluno aluno = buscarAlunoLogado(auth);
        
        Disponibilidade disp = disponibilidadeRepository.findById(disponibilidadeId)
            .orElseThrow(() -> new RuntimeException("Horário não encontrado"));

        if (disp.isReservado()) {
            return "redirect:/aluno/perfil?erro=ja_reservado";
        }

        disp.setReservado(true);
        disponibilidadeRepository.save(disp);

        AulaParticular aula = new AulaParticular();
        aula.setAluno(aluno);
        aula.setProfessor(disp.getProfessor());
        aula.setDisponibilidade(disp);
        aula.setDataHora(disp.getData().atTime(disp.getHora(), 0));
        aula.setStatus("PENDENTE");

        aulaParticularRepository.save(aula);
        
        return "redirect:/aluno/perfil?sucessoAgendamento=true";
    }

    private Aluno buscarAlunoLogado(Authentication auth) {
        return alunoRepository.findAll().stream()
            .filter(a -> a.getUsuario().getEmail().equals(auth.getName()))
            .findFirst().orElseThrow();
    }
}