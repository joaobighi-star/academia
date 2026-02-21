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

    // --- PAINEL PRINCIPAL ---
    @GetMapping("/painel")
    public String painelProfessor(Authentication auth, Model model) {
        Professor prof = buscarProfessorLogado(auth);
        model.addAttribute("professor", prof);
        
        // Filtra turmas onde este professor é o responsável
        List<Turma> turmas = turmaRepository.findAll().stream()
            .filter(t -> t.getProfessorResponsavel().getId().equals(prof.getId()))
            .toList();
            
        model.addAttribute("turmas", turmas);
        model.addAttribute("solicitacoes", aulaParticularRepository.findByProfessor(prof));
        
        return "professor/painel";
    }

    // --- CHAMADA COLETIVA (Resolve o Erro 404) ---
    @GetMapping("/chamada/{turmaId}")
    public String telaChamada(@PathVariable Long turmaId, Model model) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        
        model.addAttribute("turma", turma);
        model.addAttribute("alunos", turma.getAlunos());
        return "professor/chamada";
    }

    @PostMapping("/chamada/salvar")
    public String salvarPresenca(@RequestParam(value = "alunosPresentes", required = false) List<Long> idsPresentes) {
        if (idsPresentes != null) {
            for (Long id : idsPresentes) {
                Aluno aluno = alunoRepository.findById(id).orElseThrow();
                aluno.setAulasAssistidas(aluno.getAulasAssistidas() + 1);
                alunoRepository.save(aluno);
            }
        }
        return "redirect:/professor/painel?sucessoChamada=true";
    }

    // --- GESTÃO DE AULAS PARTICULARES ---
    @PostMapping("/aula-particular/decidir")
    public String decidirAula(@RequestParam Long aulaId, @RequestParam String decisao) {
        AulaParticular aula = aulaParticularRepository.findById(aulaId).orElseThrow();
        
        if (decisao.equals("aceitar")) {
            aula.setStatus("CONFIRMADA");
        } else if (decisao.equals("concluir")) {
            aula.setStatus("CONCLUIDA");
            Aluno aluno = aula.getAluno();
            aluno.setAulasAssistidas(aluno.getAulasAssistidas() + 1);
            alunoRepository.save(aluno);
        } else {
            aula.setStatus("RECUSADA");
            // Se recusar, liberamos o slot na agenda para outro aluno
            if (aula.getDisponibilidade() != null) {
                Disponibilidade disp = aula.getDisponibilidade();
                disp.setReservado(false);
                disponibilidadeRepository.save(disp);
            }
        }
        
        aulaParticularRepository.save(aula);
        return "redirect:/professor/painel";
    }

    // --- GRADE DE DISPONIBILIDADE (AGENDA) ---
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

        disponibilidadeRepository.findByProfessorAndDataBetween(prof, dataClick, dataClick)
            .stream()
            .filter(d -> d.getHora().equals(hora))
            .findFirst()
            .ifPresentOrElse(
                disponibilidadeRepository::delete,
                () -> {
                    Disponibilidade nova = new Disponibilidade();
                    nova.setProfessor(prof);
                    nova.setData(dataClick);
                    nova.setHora(hora);
                    disponibilidadeRepository.save(nova);
                }
            );

        return "redirect:/professor/agenda?dataBase=" + data;
    }

    private Professor buscarProfessorLogado(Authentication auth) {
        return professorRepository.findAll().stream()
            .filter(p -> p.getUsuario().getEmail().equals(auth.getName()))
            .findFirst().orElseThrow();
    }
}