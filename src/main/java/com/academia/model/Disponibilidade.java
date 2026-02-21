package com.academia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Disponibilidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    private LocalDate data;
    
    // Representa a hora cheia (ex: 8, 9, 10...)
    private Integer hora; 

    // Status: true se um aluno já clicou e reservou
    private boolean reservado = false; 

    // Status: true se o professor confirmou o agendamento
    private boolean confirmado = false;

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Aluno aluno; // Quem reservou (se houver)
}