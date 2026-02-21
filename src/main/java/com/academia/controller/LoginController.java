package com.academia.controller;

import com.academia.model.Aluno;
import com.academia.model.Usuario;
import com.academia.repository.AlunoRepository;
import com.academia.repository.UsuarioRepository;
import com.academia.repository.ProfessorRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.UUID;

@Controller
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public LoginController(UsuarioRepository usuarioRepository, 
                           AlunoRepository alunoRepository, 
                           ProfessorRepository professorRepository,
                           PasswordEncoder passwordEncoder,
                           JavaMailSender mailSender) {
        this.usuarioRepository = usuarioRepository;
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String redirectByRole(Authentication auth) {
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/painel-mestre-bjj";
        } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROFESSOR"))) {
            return "redirect:/professor/painel";
        } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ALUNO"))) {
            return "redirect:/aluno/perfil";
        }
        return "home";
    }

    @GetMapping("/painel-mestre-bjj")
    public String adminDashboard(Authentication auth, Model model) {
        Usuario admin = usuarioRepository.findByEmail(auth.getName()).orElseThrow();
        model.addAttribute("trocarSenha", !admin.isSenhaAlteradaPeloAdmin());
        return "admin/dashboard";
    }

    @GetMapping("/painel-mestre-bjj/trocar-senha")
    public String trocarSenhaPage() {
        return "admin/trocar-senha";
    }

    @PostMapping("/painel-mestre-bjj/executar-troca")
    public String executarTroca(@RequestParam String senhaAtual, 
                               @RequestParam String novaSenha, 
                               @RequestParam String confirmarSenha,
                               Authentication auth, 
                               RedirectAttributes ra) {
        Usuario admin = usuarioRepository.findByEmail(auth.getName()).orElseThrow();

        if (!passwordEncoder.matches(senhaAtual, admin.getSenha())) {
            ra.addFlashAttribute("error", "Senha atual incorreta.");
            return "redirect:/painel-mestre-bjj/trocar-senha";
        }

        if (!novaSenha.equals(confirmarSenha)) {
            ra.addFlashAttribute("error", "As novas senhas não coincidem.");
            return "redirect:/painel-mestre-bjj/trocar-senha";
        }

        admin.setSenha(passwordEncoder.encode(novaSenha));
        admin.setSenhaAlteradaPeloAdmin(true);
        usuarioRepository.save(admin);

        ra.addFlashAttribute("success", "Senha alterada com sucesso!");
        return "redirect:/painel-mestre-bjj";
    }

    @GetMapping("/recuperar-senha")
    public String esqueciSenha() {
        return "recuperar-senha";
    }

    @PostMapping("/recuperar-senha/enviar")
    public String enviarEmailRecuperacao(@RequestParam String email, RedirectAttributes ra) {
        Optional<Usuario> user = usuarioRepository.findByEmail(email);
        if (user.isPresent()) {
            String novaSenhaTemp = UUID.randomUUID().toString().substring(0, 8);
            user.get().setSenha(passwordEncoder.encode(novaSenhaTemp));
            user.get().setSenhaAlteradaPeloAdmin(false);
            usuarioRepository.save(user.get());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Recuperação de Senha - Academia BJJ");
            message.setText("Olá, uma nova senha temporária foi gerada para seu acesso: " + novaSenhaTemp + 
                            "\n\nAo logar, recomendamos trocar sua senha imediatamente.");
            mailSender.send(message);
        }
        ra.addFlashAttribute("success", "Se o e-mail existir em nossa base, as instruções foram enviadas.");
        return "redirect:/login";
    }

    @GetMapping("/aluno/perfil")
    public String perfilAluno(Authentication auth, Model model) {
        String email = auth.getName();
        Aluno aluno = alunoRepository.findAll().stream()
            .filter(a -> a.getUsuario().getEmail().equals(email))
            .findFirst().orElseThrow();
        
        model.addAttribute("aluno", aluno);
        return "aluno/perfil";
    }
}