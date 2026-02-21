package com.academia.controller;

import com.academia.enums.Perfil;
import com.academia.enums.StatusMensalidade;
import com.academia.enums.DiaSemana; // Importe o novo Enum
import com.academia.model.Aluno;
import com.academia.model.Professor;
import com.academia.model.Turma;
import com.academia.model.Usuario;
import com.academia.repository.AlunoRepository;
import com.academia.repository.ProfessorRepository;
import com.academia.repository.TurmaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

@Controller
@RequestMapping("/painel-mestre-bjj")
public class AdminController {

    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final TurmaRepository turmaRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AlunoRepository alunoRepository, 
                           ProfessorRepository professorRepository, 
                           TurmaRepository turmaRepository, 
                           PasswordEncoder passwordEncoder) {
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.turmaRepository = turmaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/professores/novo")
    public String formularioProfessor(Professor professor) {
        return "admin/cadastro-professor";
    }

    @PostMapping("/professores/salvar")
    public String salvarProfessor(Professor professor) {
        Usuario user = new Usuario();
        user.setEmail(professor.getUsuario().getEmail());
        user.setSenha(passwordEncoder.encode("prof123"));
        user.setPerfis(Set.of(Perfil.ROLE_PROFESSOR));
        user.setSenhaAlteradaPeloAdmin(false);
        professor.setUsuario(user);
        professorRepository.save(professor);
        return "redirect:/painel-mestre-bjj";
    }

    // ATUALIZADO: Agora envia os dias da semana para o formulário
    @GetMapping("/turmas/nova")
    public String formularioTurma(Model model) {
        model.addAttribute("turma", new Turma());
        model.addAttribute("professores", professorRepository.findAll());
        model.addAttribute("todosDias", DiaSemana.values()); // Adicionado aqui
        return "admin/cadastro-turma";
    }

    @PostMapping("/turmas/salvar")
    public String salvarTurma(Turma turma) {
        turmaRepository.save(turma);
        return "redirect:/painel-mestre-bjj/turmas/listar";
    }

    @GetMapping("/alunos/novo")
    public String formularioAluno(Model model) {
        Aluno novoAluno = new Aluno();
        novoAluno.setDataMatricula(LocalDate.now());
        model.addAttribute("aluno", novoAluno);
        model.addAttribute("turmas", turmaRepository.findAll());
        return "admin/cadastro-aluno";
    }

    @PostMapping("/alunos/salvar")
    public String salvarAluno(Aluno aluno, @RequestParam("imagem") MultipartFile imagem) throws IOException {
        if (!imagem.isEmpty()) {
            aluno.setFoto(imagem.getBytes());
        } else if (aluno.getId() != null) {
            Aluno alunoExistente = alunoRepository.findById(aluno.getId()).orElseThrow();
            aluno.setFoto(alunoExistente.getFoto());
        }
        
        if (aluno.getId() == null) {
            Usuario user = new Usuario();
            user.setEmail(aluno.getUsuario().getEmail());
            user.setSenha(passwordEncoder.encode(aluno.getCpf()));
            user.setPerfis(Set.of(Perfil.ROLE_ALUNO));
            user.setSenhaAlteradaPeloAdmin(false);
            aluno.setUsuario(user);
            aluno.setStatusMensalidade(StatusMensalidade.EM_DIA);
            
            if (aluno.getDataMatricula() == null) {
                aluno.setDataMatricula(LocalDate.now());
            }
        }
        
        alunoRepository.save(aluno);
        return "redirect:/painel-mestre-bjj/turmas/listar";
    }

    @GetMapping("/turmas/listar")
    public String listarTurmas(Model model) {
        model.addAttribute("turmas", turmaRepository.findAll());
        return "admin/lista-turmas";
    }

    @GetMapping("/turmas/detalhes/{id}")
    public String detalhesTurma(@PathVariable Long id, Model model) {
        Turma turma = turmaRepository.findById(id).orElseThrow();
        model.addAttribute("turma", turma);
        model.addAttribute("alunos", turma.getAlunos()); 
        return "admin/detalhes-turma";
    }

    @GetMapping("/alunos/foto/{id}")
    @ResponseBody
    public byte[] exibirFoto(@PathVariable("id") Long id) {
        return alunoRepository.findById(id)
                .map(Aluno::getFoto)
                .orElse(null);
    }
}