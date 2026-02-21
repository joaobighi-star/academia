package com.academia.model;

import com.academia.enums.Faixa;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Professor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String nome;
    
    @Enumerated(EnumType.STRING)
    private Faixa faixa;

    @ElementCollection
    private List<String> disponibilidadeAulasParticulares;

    private String especialidade;
}