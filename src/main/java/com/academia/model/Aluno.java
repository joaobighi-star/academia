package com.academia.model;

import com.academia.enums.Faixa;
import com.academia.enums.StatusMensalidade;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Aluno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String nome;
    private String cpf;
    private String telefone; // Novo: Contato rápido
    private String nomeResponsavel; // Novo: Para menores de 18 anos
    private LocalDate dataNascimento; // Refinado
    private LocalDate dataMatricula = LocalDate.now(); // Novo: Valor padrão é hoje
    
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] foto;

    @Enumerated(EnumType.STRING)
    private Faixa faixa;

    private Integer aulasAssistidas = 0;
    private Integer metaAulasNovaFaixa = 50;

    @Enumerated(EnumType.STRING)
    private StatusMensalidade statusMensalidade;

    private LocalDate dataVencimento;

    @ManyToOne
    @JoinColumn(name = "turma_id")
    private Turma turma;
}