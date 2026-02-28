package com.academia.controller;

import com.academia.enums.Perfil;
import com.academia.enums.StatusMensalidade;
import com.academia.enums.DiaSemana;
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

    // --- CRUD PROFESSORES ---
    @GetMapping("/professores/listar")
    public String listarProfessores(Model model) {
        model.addAttribute("professores", professorRepository.findAll());
        return "admin/lista-professores";
    }

    @GetMapping("/professores/novo")
    public String formularioProfessor(Model model) {
        model.addAttribute("professor", new Professor());
        return "admin/cadastro-professor";
    }

    @PostMapping("/professores/salvar")
    public String salvarProfessor(@ModelAttribute Professor professor) {
        if (professor.getId() == null) {
            Usuario user = new Usuario();
            user.setEmail(professor.getUsuario().getEmail());
            user.setSenha(passwordEncoder.encode("prof123"));
            user.setPerfis(Set.of(Perfil.ROLE_PROFESSOR));
            user.setSenhaAlteradaPeloAdmin(false);
            professor.setUsuario(user);
        } else {
            Professor existente = professorRepository.findById(professor.getId()).orElseThrow();
            professor.getUsuario().setId(existente.getUsuario().getId());
            professor.getUsuario().setSenha(existente.getUsuario().getSenha());
            professor.getUsuario().setPerfis(existente.getUsuario().getPerfis());
        }
        professorRepository.save(professor);
        return "redirect:/painel-mestre-bjj/professores/listar";
    }

    @GetMapping("/professores/editar/{id}")
    public String editarProfessor(@PathVariable Long id, Model model) {
        model.addAttribute("professor", professorRepository.findById(id).orElseThrow());
        return "admin/cadastro-professor";
    }

    @GetMapping("/professores/excluir/{id}")
    public String excluirProfessor(@PathVariable Long id) {
        professorRepository.deleteById(id);
        return "redirect:/painel-mestre-bjj/professores/listar";
    }

    // --- CRUD TURMAS ---
    @GetMapping("/turmas/listar")
    public String listarTurmas(Model model) {
        model.addAttribute("turmas", turmaRepository.findAll());
        return "admin/lista-turmas";
    }

    @GetMapping("/turmas/nova")
    public String formularioTurma(Model model) {
        model.addAttribute("turma", new Turma());
        model.addAttribute("professores", professorRepository.findAll());
        model.addAttribute("todosDias", DiaSemana.values());
        return "admin/cadastro-turma";
    }

    @PostMapping("/turmas/salvar")
    public String salvarTurma(Turma turma) {
        turmaRepository.save(turma);
        return "redirect:/painel-mestre-bjj/turmas/listar";
    }
    
    @GetMapping("/turmas/editar/{id}")
    public String editarTurma(@PathVariable Long id, Model model) {
        model.addAttribute("turma", turmaRepository.findById(id).orElseThrow());
        model.addAttribute("professores", professorRepository.findAll());
        model.addAttribute("todosDias", DiaSemana.values());
        return "admin/cadastro-turma";
    }

    @GetMapping("/turmas/excluir/{id}")
    public String excluirTurma(@PathVariable Long id) {
        turmaRepository.deleteById(id);
        return "redirect:/painel-mestre-bjj/turmas/listar";
    }

    @GetMapping("/turmas/detalhes/{id}")
    public String detalhesTurma(@PathVariable Long id, Model model) {
        Turma turma = turmaRepository.findById(id).orElseThrow();
        model.addAttribute("turma", turma);
        model.addAttribute("alunos", turma.getAlunos()); 
        return "admin/detalhes-turma";
    }

    // --- CRUD ALUNOS (REVISADO) ---
    @GetMapping("/alunos/listar")
    public String listarAlunos(Model model) {
        model.addAttribute("alunos", alunoRepository.findAll());
        return "admin/lista-alunos"; // Certifique-se de ter este HTML
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
            user.setSenha(passwordEncoder.encode(aluno.getCpf().replaceAll("\\D", ""))); // Senha é o CPF limpo
            user.setPerfis(Set.of(Perfil.ROLE_ALUNO));
            user.setSenhaAlteradaPeloAdmin(false);
            aluno.setUsuario(user);
            aluno.setStatusMensalidade(StatusMensalidade.EM_DIA);
        } else {
            // Se for edição, mantém os dados de usuário originais
            Aluno existente = alunoRepository.findById(aluno.getId()).orElseThrow();
            aluno.getUsuario().setId(existente.getUsuario().getId());
            aluno.getUsuario().setSenha(existente.getUsuario().getSenha());
            aluno.getUsuario().setPerfis(existente.getUsuario().getPerfis());
        }
        
        alunoRepository.save(aluno);
        // CORREÇÃO: Agora redireciona para a lista de ALUNOS, não de turmas
        return "redirect:/painel-mestre-bjj/alunos/listar";
    }

    @GetMapping("/alunos/editar/{id}")
    public String editarAluno(@PathVariable Long id, Model model) {
        model.addAttribute("aluno", alunoRepository.findById(id).orElseThrow());
        model.addAttribute("turmas", turmaRepository.findAll());
        return "admin/cadastro-aluno";
    }

    @GetMapping("/alunos/excluir/{id}")
    public String excluirAluno(@PathVariable Long id) {
        alunoRepository.deleteById(id);
        return "redirect:/painel-mestre-bjj/alunos/listar";
    }

    @GetMapping("/alunos/foto/{id}")
    @ResponseBody
    public byte[] exibirFoto(@PathVariable("id") Long id) {
        return alunoRepository.findById(id)
                .map(Aluno::getFoto)
                .orElse(null);
    }
}