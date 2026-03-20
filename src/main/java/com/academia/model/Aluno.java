package com.academia.model;

import com.academia.enums.Faixa;
import com.academia.enums.StatusMensalidade;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.Period;

@Entity
@Data
public class Aluno {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore // Impede loop infinito com o Usuário no JSON
    private Usuario usuario;

    private String nome;
    
    @Column(length = 9) // BSN holandês
    private String bsn;
    
    private String telefone;
    private String nomeResponsavel;
    private LocalDate dataNascimento;
    private LocalDate dataMatricula = LocalDate.now();
    
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    @JsonIgnore // Remove o peso dos bytes da foto da listagem da API
    private byte[] foto;

    @Enumerated(EnumType.STRING)
    private Faixa faixa;

    private Integer aulasAssistidas = 0;
    private boolean solicitouMudanca = false;
    private Integer ultimoGrauRecebido = 0; 

    @Enumerated(EnumType.STRING)
    private StatusMensalidade statusMensalidade;

    @Column(length = 34) // IBAN Internacional
    private String iban;

    @ManyToOne
    @JoinColumn(name = "turma_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore // Evita carregar toda a estrutura de turma na listagem de alunos
    private Turma turma;

    // --- LÓGICA DE PROGRESSÃO DINÂMICA ---
    public int getIdade() {
        if (this.dataNascimento == null) return 0;
        return Period.between(this.dataNascimento, LocalDate.now()).getYears();
    }

    public Integer getMetaAulasNovaFaixa() {
        if (this.faixa == null) return 40;
        return this.faixa.getMetaAulas();
    }

    public int getAulasPorGrau() {
        Integer meta = getMetaAulasNovaFaixa();
        return (meta != null) ? meta / 4 : 10;
    }

    public boolean temDireitoANovoGrau() {
        int aulasPorGrau = getAulasPorGrau();
        if (aulasPorGrau == 0) return false;
        
        int grausSugeridos = this.aulasAssistidas / aulasPorGrau;
        int grauAtual = (this.ultimoGrauRecebido != null) ? this.ultimoGrauRecebido : 0;
        
        return grausSugeridos > grauAtual && grausSugeridos <= 4;
    }

    public double getPercentualProgresso() {
        Integer meta = getMetaAulasNovaFaixa();
        if (meta == null || meta == 0) return 0;
        double percentual = (double) aulasAssistidas * 100 / meta;
        return Math.min(percentual, 100.0);
    }

    // Getters e Setters mantidos
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getBsn() { return bsn; }
    public void setBsn(String bsn) { this.bsn = bsn; }
    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getNomeResponsavel() { return nomeResponsavel; }
    public void setNomeResponsavel(String nomeResponsavel) { this.nomeResponsavel = nomeResponsavel; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public LocalDate getDataMatricula() { return dataMatricula; }
    public void setDataMatricula(LocalDate dataMatricula) { this.dataMatricula = dataMatricula; }
    public byte[] getFoto() { return foto; }
    public void setFoto(byte[] foto) { this.foto = foto; }
    public Faixa getFaixa() { return faixa; }
    public void setFaixa(Faixa faixa) { this.faixa = faixa; }
    public Integer getAulasAssistidas() { return aulasAssistidas; }
    public void setAulasAssistidas(Integer aulasAssistidas) { this.aulasAssistidas = aulasAssistidas; }
    public boolean isSolicitouMudanca() { return solicitouMudanca; }
    public void setSolicitouMudanca(boolean solicitouMudanca) { this.solicitouMudanca = solicitouMudanca; }
    public Integer getUltimoGrauRecebido() { return ultimoGrauRecebido; }
    public void setUltimoGrauRecebido(Integer ultimoGrauRecebido) { this.ultimoGrauRecebido = ultimoGrauRecebido; }
    public StatusMensalidade getStatusMensalidade() { return statusMensalidade; }
    public void setStatusMensalidade(StatusMensalidade statusMensalidade) { this.statusMensalidade = statusMensalidade; }
    public Turma getTurma() { return turma; }
    public void setTurma(Turma turma) { this.turma = turma; }
}