package com.academia.model;

import com.academia.enums.Perfil;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Entity
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senha;

    private boolean senhaAlteradaPeloAdmin = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Perfil> perfis;

    // Métodos para o Flutter identificar o tipo de usuário
    public String getRolePrincipal() {
        if (perfis == null || perfis.isEmpty()) return "ROLE_USER";
        return perfis.iterator().next().name(); // Retorna ROLE_ALUNO ou ROLE_PROFESSOR
    }

    // Getters e Setters (Mantidos conforme seu original)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public boolean isSenhaAlteradaPeloAdmin() { return senhaAlteradaPeloAdmin; }
    public void setSenhaAlteradaPeloAdmin(boolean senhaAlteradaPeloAdmin) { this.senhaAlteradaPeloAdmin = senhaAlteradaPeloAdmin; }
    public Set<Perfil> getPerfis() { return perfis; }
    public void setPerfis(Set<Perfil> perfis) { this.perfis = perfis; }
}