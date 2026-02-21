package com.academia.enums;

import lombok.Getter; // Se estiver usando Lombok

@Getter // Adicione isso se tiver o Lombok no projeto
public enum DiaSemana {
    SEGUNDA("Segunda-feira"),
    TERCA("Terça-feira"),
    QUARTA("Quarta-feira"),
    QUINTA("Quinta-feira"),
    SEXTA("Sexta-feira"),
    SABADO("Sábado"),
    DOMINGO("Domingo");

    private final String descricao;

    // Construtor do Enum
    DiaSemana(String descricao) {
        this.descricao = descricao;
    }

    // SE NÃO USAR LOMBOK, VOCÊ PRECISA DESTE MÉTODO MANUALMENTE:
    public String getDescricao() {
        return descricao;
    }
}