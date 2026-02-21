package com.academia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class AulaParticular {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "disponibilidade_id")
    private Disponibilidade disponibilidade;

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    private LocalDateTime dataHora;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    // Novo campo para a mensagem de feedback do professor
    @Column(columnDefinition = "TEXT")
    private String mensagemProfessor;

    private boolean presencaConfirmada = false;
}