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
}