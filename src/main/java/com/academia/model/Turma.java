package com.academia.model;

import com.academia.enums.DiaSemana;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
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

    @ElementCollection(targetClass = DiaSemana.class)
    @CollectionTable(name = "turma_dias", joinColumns = @JoinColumn(name = "turma_id"))
    @Enumerated(EnumType.STRING)
    private Set<DiaSemana> diasSemana;

    @ManyToOne
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Professor professorResponsavel;

    @OneToMany(mappedBy = "turma")
    @ToString.Exclude // ESSENCIAL: Evita o loop infinito com a classe Aluno
    @EqualsAndHashCode.Exclude
    private List<Aluno> alunos;

    // --- Getters e Setters (Mantidos para garantir compatibilidade com o Spring) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getFaixaEtaria() {
        return faixaEtaria;
    }

    public void setFaixaEtaria(String faixaEtaria) {
        this.faixaEtaria = faixaEtaria;
    }

    public String getNivelFaixa() {
        return nivelFaixa;
    }

    public void setNivelFaixa(String nivelFaixa) {
        this.nivelFaixa = nivelFaixa;
    }

    public Set<DiaSemana> getDiasSemana() {
        return diasSemana;
    }

    public void setDiasSemana(Set<DiaSemana> diasSemana) {
        this.diasSemana = diasSemana;
    }

    public Professor getProfessorResponsavel() {
        return professorResponsavel;
    }

    public void setProfessorResponsavel(Professor professorResponsavel) {
        this.professorResponsavel = professorResponsavel;
    }

    public List<Aluno> getAlunos() {
        return alunos;
    }

    public void setAlunos(List<Aluno> alunos) {
        this.alunos = alunos;
    }
}