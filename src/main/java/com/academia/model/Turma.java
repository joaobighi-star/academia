package com.academia.model;

import com.academia.enums.DiaSemana; // Vamos criar este Enum abaixo
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Entity
@Data
public class Turma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String horario;
    private String faixaEtaria;
    private String nivelFaixa;

    // Adicionando a coleção de dias da semana
    @ElementCollection(targetClass = DiaSemana.class)
    @CollectionTable(name = "turma_dias", joinColumns = @JoinColumn(name = "turma_id"))
    @Enumerated(EnumType.STRING)
    private Set<DiaSemana> diasSemana;

    @ManyToOne
    private Professor professorResponsavel;

    @OneToMany(mappedBy = "turma")
    private List<Aluno> alunos;
}