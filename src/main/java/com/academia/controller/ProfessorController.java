package com.academia.controller;

import com.academia.model.*;
import com.academia.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/professor")
public class ProfessorController {

    private final ProfessorRepository professorRepository;
    private final TurmaRepository turmaRepository;
    private final AulaParticularRepository aulaParticularRepository;
    private final AlunoRepository alunoRepository;
    private final DisponibilidadeRepository disponibilidadeRepository;

    public ProfessorController(ProfessorRepository professorRepository, 
                               TurmaRepository turmaRepository, 
                               AulaParticularRepository aulaParticularRepository,
                               AlunoRepository alunoRepository,
                               DisponibilidadeRepository disponibilidadeRepository) {
        this.professorRepository = professorRepository;
        this.turmaRepository = turmaRepository;
        this.aulaParticularRepository = aulaParticularRepository;
        this.alunoRepository = alunoRepository;
        this.disponibilidadeRepository = disponibilidadeRepository;
    }

    @GetMapping("/painel")
    public String painelProfessor(Authentication auth, Model model) {
        Professor prof = buscarProfessorLogado(auth);
        model.addAttribute("professor", prof);
        
        List<Turma> turmas = turmaRepository.findAll().stream()
            .filter(t -> t.getProfessorResponsavel() != null && t.getProfessorResponsavel().getId().equals(prof.getId()))
            .toList();
            
        model.addAttribute("turmas", turmas);
        model.addAttribute("solicitacoes", aulaParticularRepository.findByProfessorOrderByDataHoraAsc(prof));

        List<Aluno> todosAlunos = alunoRepository.findAll();
        
        List<Aluno> solicitacoesFaixa = todosAlunos.stream()
            .filter(Aluno::isSolicitouMudanca)
            .collect(Collectors.toList());
            
        List<Aluno> alertasGraus = todosAlunos.stream()
            .filter(a -> {
                double p = a.getPercentualProgresso();
                Integer ultimoGrau = (a.getUltimoGrauRecebido() != null) ? a.getUltimoGrauRecebido() : 0;
                
                if (p >= 75 && ultimoGrau < 3) return true;
                if (p >= 50 && ultimoGrau < 2) return true;
                if (p >= 25 && ultimoGrau < 1) return true;
                return false;
            }).collect(Collectors.toList());

        model.addAttribute("solicitacoesFaixa", solicitacoesFaixa);
        model.addAttribute("alertasGraus", alertasGraus);
        
        return "professor/painel";
    }

    @PostMapping("/confirmar-graduacao")
    public String confirmarGraduacao(@RequestParam Long alunoId, @RequestParam String tipo) {
        Aluno aluno = alunoRepository.findById(alunoId).orElseThrow();
        
        if ("grau".equals(tipo)) {
            double p = aluno.getPercentualProgresso();
            if (p >= 75) aluno.setUltimoGrauRecebido(3);
            else if (p >= 50) aluno.setUltimoGrauRecebido(2);
            else if (p >= 25) aluno.setUltimoGrauRecebido(1);
        } else if ("faixa".equals(tipo)) {
            aluno.setSolicitouMudanca(false);
            aluno.setAulasAssistidas(0);
            aluno.setUltimoGrauRecebido(0);
        }
        
        alunoRepository.save(aluno);
        return "redirect:/professor/painel";
    }

    @PostMapping("/registrar-presenca")
    public String registrarPresenca(@RequestParam Long turmaId, 
                                    @RequestParam(required = false) List<Long> alunoIds) {
        if (alunoIds != null) {
            for (Long id : alunoIds) {
                Aluno a = alunoRepository.findById(id).orElseThrow();
                a.setAulasAssistidas(a.getAulasAssistidas() + 1);
                alunoRepository.save(a);
            }
        }
        return "redirect:/professor/painel?sucessoChamada=true";
    }

    @GetMapping("/agenda")
    public String verAgenda(Authentication auth, 
                            @RequestParam(required = false) String dataBase, 
                            Model model) {
        Professor prof = buscarProfessorLogado(auth);
        LocalDate dataRef = (dataBase == null) ? LocalDate.now() : LocalDate.parse(dataBase);
        LocalDate segundaFeira = dataRef.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        List<LocalDate> diasDaSemana = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            diasDaSemana.add(segundaFeira.plusDays(i));
        }

        List<Disponibilidade> abertas = disponibilidadeRepository.findByProfessorAndDataBetween(
            prof, segundaFeira, segundaFeira.plusDays(6)
        );

        model.addAttribute("professor", prof);
        model.addAttribute("dias", diasDaSemana);
        model.addAttribute("horas", IntStream.rangeClosed(7, 21).boxed().toList());
        model.addAttribute("disponibilidades", abertas);
        model.addAttribute("dataRef", dataRef);
        model.addAttribute("proximaSemana", dataRef.plusWeeks(1));
        model.addAttribute("semanaAnterior", dataRef.minusWeeks(1));

        return "professor/agenda";
    }

    @PostMapping("/agenda/alternar")
    public String alternarHorario(Authentication auth, 
                                 @RequestParam String data, 
                                 @RequestParam Integer hora) {
        Professor prof = buscarProfessorLogado(auth);
        LocalDate dataClick = LocalDate.parse(data);

        Optional<Disponibilidade> existente = disponibilidadeRepository
                .findByProfessorAndDataAndHora(prof, dataClick, hora);

        if (existente.isPresent()) {
            if (!existente.get().isReservado()) {
                disponibilidadeRepository.delete(existente.get());
            }
        } else {
            Disponibilidade nova = new Disponibilidade();
            nova.setProfessor(prof);
            nova.setData(dataClick);
            nova.setHora(hora);
            nova.setReservado(false);
            disponibilidadeRepository.save(nova);
        }

        return "redirect:/professor/agenda?dataBase=" + data;
    }

    @GetMapping("/chamada/{turmaId}")
    public String telaChamada(@PathVariable Long turmaId, Model model) {
        Turma turma = turmaRepository.findById(turmaId).orElseThrow();
        model.addAttribute("turma", turma);
        model.addAttribute("alunos", turma.getAlunos());
        return "professor/chamada";
    }

    @PostMapping("/aula-particular/decidir")
    public String decidirAula(@RequestParam Long aulaId, 
                             @RequestParam String decisao,
                             @RequestParam(required = false) String mensagem) {
        AulaParticular aula = aulaParticularRepository.findById(aulaId).orElseThrow();
        
        aula.setMensagemProfessor(mensagem);
        
        if (decisao.equals("aceitar")) {
            aula.setStatus("CONFIRMADA");
        } else if (decisao.equals("concluir")) {
            aula.setStatus("CONCLUIDA");
            Aluno a = aula.getAluno();
            a.setAulasAssistidas(a.getAulasAssistidas() + 1);
            alunoRepository.save(a);
        } else if (decisao.equals("recusar")) {
            aula.setStatus("RECUSADA");
            if (aula.getDisponibilidade() != null) {
                Disponibilidade d = aula.getDisponibilidade();
                d.setReservado(false);
                disponibilidadeRepository.save(d);
            }
        }
        
        aulaParticularRepository.save(aula);
        return "redirect:/professor/painel";
    }

    private Professor buscarProfessorLogado(Authentication auth) {
        return professorRepository.findAll().stream()
            .filter(p -> p.getUsuario().getEmail().equals(auth.getName()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Professor não encontrado"));
    }
}