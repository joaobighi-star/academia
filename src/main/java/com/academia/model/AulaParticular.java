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

    // Relacionamento com o slot da grade
    // Quando a aula é marcada, ela ocupa uma disponibilidade específica
    @OneToOne
    @JoinColumn(name = "disponibilidade_id")
    private Disponibilidade disponibilidade;

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    // Mantemos esse campo para auditoria ou caso a aula mude de horário
    private LocalDateTime dataHora;

    // Status: PENDENTE, CONFIRMADA, CONCLUIDA, RECUSADA
    private String status;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    // Campos auxiliares para facilitar o somatório de progresso que fizemos antes
    private boolean presencaConfirmada = false;
}