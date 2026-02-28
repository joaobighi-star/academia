package com.academia.enums;

public enum Faixa {
    // GRUPO 1: Meta de 40 aulas (Semestrais/Iniciais)
    BRANCA("Branca", 40),
    
    CINZA_BRANCA("Cinza e Branca", 40),
    CINZA("Cinza", 40),
    CINZA_PRETA("Cinza e Preta", 40),
    
    AMARELA_BRANCA("Amarela e Branca", 40),
    AMARELA("Amarela", 40),
    AMARELA_PRETA("Amarela e Preta", 40),

    // GRUPO 2: Meta de 80 aulas (Dobro de presença)
    LARANJA_BRANCA("Laranja e Branca", 80),
    LARANJA("Laranja", 80),
    LARANJA_PRETA("Laranja e Preta", 80),
    
    VERDE_BRANCA("Verde e Branca", 80),
    VERDE("Verde", 80),
    VERDE_PRETA("Verde e Preta", 80),

    // ADULTOS (Mantendo as metas padrão ou ajuste conforme desejar)
    AZUL("Azul", 80),
    ROXA("Roxa", 160),
    MARROM("Marrom", 320),
    PRETA("Preta", 640);

    private final String nomeExibicao;
    private final int metaAulas;

    Faixa(String nomeExibicao, int metaAulas) {
        this.nomeExibicao = nomeExibicao;
        this.metaAulas = metaAulas;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public int getMetaAulas() {
        return metaAulas;
    }
}