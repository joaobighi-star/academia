package com.academia.controller.api;

import com.academia.model.Aluno;
import com.academia.model.AulaParticular;
import com.academia.model.Disponibilidade;
import com.academia.model.Professor;
import com.academia.model.Usuario;
import com.academia.repository.AlunoRepository;
import com.academia.repository.AulaParticularRepository;
import com.academia.repository.DisponibilidadeRepository;
import com.academia.repository.ProfessorRepository;
import com.academia.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/mobile")
public class MobileRestController {

    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final DisponibilidadeRepository disponibilidadeRepository;
    private final AulaParticularRepository aulaParticularRepository;
    private final PasswordEncoder passwordEncoder;

    public MobileRestController(UsuarioRepository usuarioRepository,
                                AlunoRepository alunoRepository,
                                ProfessorRepository professorRepository,
                                DisponibilidadeRepository disponibilidadeRepository,
                                AulaParticularRepository aulaParticularRepository,
                                PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.aulaParticularRepository = aulaParticularRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String senha = credentials.get("senha");

        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            return ResponseEntity.badRequest().body("Email e senha são obrigatórios");
        }

        return usuarioRepository.findByEmail(email)
            .map(user -> {
                if (!passwordEncoder.matches(senha, user.getSenha())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Senha incorreta");
                }
                return ResponseEntity.ok(buildLoginResponse(user));
            })
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não encontrado"));
    }

    @GetMapping("/professores")
    public List<Map<String, Object>> listarProfessores() {
        return professorRepository.findAll().stream().map(this::professorToDto).toList();
    }

    @GetMapping("/professores/{professorId}/disponibilidades")
    public ResponseEntity<?> listarDisponibilidades(@PathVariable Long professorId,
                                                    @RequestParam(required = false) String inicio,
                                                    @RequestParam(required = false) String fim,
                                                    @RequestParam(defaultValue = "false") boolean incluirReservados) {
        if (!professorRepository.existsById(professorId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Professor não encontrado");
        }

        LocalDate dataInicio = parseDataOrDefault(inicio, LocalDate.now());
        LocalDate dataFim = parseDataOrDefault(fim, dataInicio.plusDays(14));

        List<Disponibilidade> disponibilidades = disponibilidadeRepository
            .findByProfessorIdAndDataBetweenOrderByDataAscHoraAsc(professorId, dataInicio, dataFim);

        if (!incluirReservados) {
            disponibilidades = disponibilidades.stream().filter(d -> !d.isReservado()).toList();
        }

        return ResponseEntity.ok(disponibilidades.stream().map(this::disponibilidadeToDto).toList());
    }

    @PostMapping("/professores/{professorId}/disponibilidades")
    public ResponseEntity<?> criarDisponibilidade(@PathVariable Long professorId, @RequestBody Map<String, String> payload) {
        Optional<Professor> professorOpt = professorRepository.findById(professorId);
        if (professorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Professor não encontrado");
        }

        String dataTxt = payload.get("data");
        String horaTxt = payload.get("hora");
        if (dataTxt == null || horaTxt == null) {
            return ResponseEntity.badRequest().body("Informe data e hora");
        }

        try {
            LocalDate data = LocalDate.parse(dataTxt);
            Integer hora = Integer.valueOf(horaTxt);
            if (hora < 0 || hora > 23) {
                return ResponseEntity.badRequest().body("Hora inválida");
            }

            Professor professor = professorOpt.get();
            if (disponibilidadeRepository.existsByProfessorAndDataAndHora(professor, data, hora)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Horário já cadastrado");
            }

            Disponibilidade disponibilidade = new Disponibilidade();
            disponibilidade.setProfessor(professor);
            disponibilidade.setData(data);
            disponibilidade.setHora(hora);
            disponibilidade.setReservado(false);
            disponibilidade.setConfirmado(false);

            Disponibilidade salva = disponibilidadeRepository.save(disponibilidade);
            return ResponseEntity.status(HttpStatus.CREATED).body(disponibilidadeToDto(salva));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Formato inválido. Use data yyyy-MM-dd e hora numérica");
        }
    }

    @DeleteMapping("/professores/{professorId}/disponibilidades/{disponibilidadeId}")
    public ResponseEntity<?> removerDisponibilidade(@PathVariable Long professorId, @PathVariable Long disponibilidadeId) {
        Optional<Disponibilidade> disponibilidadeOpt = disponibilidadeRepository.findById(disponibilidadeId);
        if (disponibilidadeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Disponibilidade não encontrada");
        }

        Disponibilidade disponibilidade = disponibilidadeOpt.get();
        if (disponibilidade.getProfessor() == null || !disponibilidade.getProfessor().getId().equals(professorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Esta disponibilidade não pertence ao professor");
        }
        if (disponibilidade.isReservado()) {
            return ResponseEntity.badRequest().body("Não é possível remover horário reservado");
        }

        disponibilidadeRepository.delete(disponibilidade);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/professores/{professorId}/aulas")
    public ResponseEntity<?> listarAulasProfessor(@PathVariable Long professorId,
                                                  @RequestParam(required = false) String status) {
        if (!professorRepository.existsById(professorId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Professor não encontrado");
        }

        List<AulaParticular> aulas = (status == null || status.isBlank())
            ? aulaParticularRepository.findByProfessorIdOrderByDataHoraAsc(professorId)
            : aulaParticularRepository.findByProfessorIdAndStatus(professorId, status.toUpperCase());

        return ResponseEntity.ok(aulas.stream().map(this::aulaToDto).toList());
    }

    @GetMapping("/alunos/{alunoId}/aulas")
    public ResponseEntity<?> listarAulasAluno(@PathVariable Long alunoId) {
        if (!alunoRepository.existsById(alunoId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aluno não encontrado");
        }

        List<AulaParticular> aulas = aulaParticularRepository.findByAlunoIdOrderByDataHoraAsc(alunoId);
        return ResponseEntity.ok(aulas.stream().map(this::aulaToDto).toList());
    }

    @PostMapping("/alunos/{alunoId}/solicitar-aula/{disponibilidadeId}")
    public ResponseEntity<?> solicitarAula(@PathVariable Long alunoId, @PathVariable Long disponibilidadeId) {
        Optional<Aluno> alunoOpt = alunoRepository.findById(alunoId);
        Optional<Disponibilidade> disponibilidadeOpt = disponibilidadeRepository.findById(disponibilidadeId);

        if (alunoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aluno não encontrado");
        }
        if (disponibilidadeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Disponibilidade não encontrada");
        }

        Disponibilidade disponibilidade = disponibilidadeOpt.get();
        if (disponibilidade.isReservado()) {
            return ResponseEntity.badRequest().body("Horário já reservado");
        }

        Aluno aluno = alunoOpt.get();
        disponibilidade.setReservado(true);
        disponibilidade.setAluno(aluno);
        disponibilidade.setConfirmado(false);
        disponibilidadeRepository.save(disponibilidade);

        AulaParticular aula = new AulaParticular();
        aula.setAluno(aluno);
        aula.setProfessor(disponibilidade.getProfessor());
        aula.setDisponibilidade(disponibilidade);
        aula.setDataHora(LocalDateTime.of(disponibilidade.getData(), LocalTime.of(disponibilidade.getHora(), 0)));
        aula.setStatus("PENDENTE");

        AulaParticular salva = aulaParticularRepository.save(aula);
        return ResponseEntity.status(HttpStatus.CREATED).body(aulaToDto(salva));
    }

    @PutMapping("/aulas/{aulaId}/status")
    public ResponseEntity<?> atualizarStatusAula(@PathVariable Long aulaId, @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        String mensagem = payload.getOrDefault("mensagemProfessor", "");

        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body("Status é obrigatório");
        }

        Optional<AulaParticular> aulaOpt = aulaParticularRepository.findById(aulaId);
        if (aulaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aula não encontrada");
        }

        String novoStatus = status.toUpperCase();
        AulaParticular aula = aulaOpt.get();
        aula.setStatus(novoStatus);
        aula.setMensagemProfessor(mensagem);

        Disponibilidade disponibilidade = aula.getDisponibilidade();
        if (disponibilidade != null) {
            if ("CONFIRMADA".equals(novoStatus)) {
                disponibilidade.setConfirmado(true);
            } else if ("RECUSADA".equals(novoStatus)) {
                disponibilidade.setReservado(false);
                disponibilidade.setConfirmado(false);
                disponibilidade.setAluno(null);
            } else if ("CONCLUIDA".equals(novoStatus)) {
                disponibilidade.setConfirmado(true);
                aula.setPresencaConfirmada(true);
            }
            disponibilidadeRepository.save(disponibilidade);
        }

        AulaParticular salva = aulaParticularRepository.save(aula);
        return ResponseEntity.ok(aulaToDto(salva));
    }

    private Map<String, Object> buildLoginResponse(Usuario user) {
        Map<String, Object> response = new HashMap<>();
        String role = user.getRolePrincipal();
        response.put("idUsuario", user.getId());
        response.put("email", user.getEmail());
        response.put("role", role);

        if ("ROLE_ALUNO".equals(role)) {
            alunoRepository.findByUsuarioEmail(user.getEmail()).ifPresent(aluno -> {
                response.put("idVinculado", aluno.getId());
                response.put("nome", aluno.getNome());
            });
        } else if ("ROLE_PROFESSOR".equals(role)) {
            professorRepository.findByUsuarioEmail(user.getEmail()).ifPresent(professor -> {
                response.put("idVinculado", professor.getId());
                response.put("nome", professor.getNome());
            });
        }

        return response;
    }

    private Map<String, Object> professorToDto(Professor professor) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", professor.getId());
        dto.put("nome", professor.getNome());
        dto.put("faixa", professor.getFaixa() != null ? professor.getFaixa().name() : null);
        dto.put("especialidade", professor.getEspecialidade());
        return dto;
    }

    private Map<String, Object> disponibilidadeToDto(Disponibilidade disponibilidade) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", disponibilidade.getId());
        dto.put("data", disponibilidade.getData() != null ? disponibilidade.getData().toString() : null);
        dto.put("hora", disponibilidade.getHora());
        dto.put("reservado", disponibilidade.isReservado());
        dto.put("confirmado", disponibilidade.isConfirmado());
        dto.put("professorId", disponibilidade.getProfessor() != null ? disponibilidade.getProfessor().getId() : null);
        dto.put("professorNome", disponibilidade.getProfessor() != null ? disponibilidade.getProfessor().getNome() : null);
        dto.put("alunoId", disponibilidade.getAluno() != null ? disponibilidade.getAluno().getId() : null);
        dto.put("alunoNome", disponibilidade.getAluno() != null ? disponibilidade.getAluno().getNome() : null);
        return dto;
    }

    private Map<String, Object> aulaToDto(AulaParticular aula) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", aula.getId());
        dto.put("status", aula.getStatus());
        dto.put("dataHora", aula.getDataHora() != null ? aula.getDataHora().toString() : null);
        dto.put("observacoes", aula.getObservacoes());
        dto.put("mensagemProfessor", aula.getMensagemProfessor());
        dto.put("presencaConfirmada", aula.isPresencaConfirmada());
        dto.put("professorId", aula.getProfessor() != null ? aula.getProfessor().getId() : null);
        dto.put("professorNome", aula.getProfessor() != null ? aula.getProfessor().getNome() : null);
        dto.put("alunoId", aula.getAluno() != null ? aula.getAluno().getId() : null);
        dto.put("alunoNome", aula.getAluno() != null ? aula.getAluno().getNome() : null);
        dto.put("disponibilidadeId", aula.getDisponibilidade() != null ? aula.getDisponibilidade().getId() : null);
        return dto;
    }

    private LocalDate parseDataOrDefault(String valor, LocalDate padrao) {
        try {
            return valor == null || valor.isBlank() ? padrao : LocalDate.parse(valor);
        } catch (Exception e) {
            return padrao;
        }
    }
}
