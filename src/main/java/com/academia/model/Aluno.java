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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getNomeResponsavel() {
		return nomeResponsavel;
	}

	public void setNomeResponsavel(String nomeResponsavel) {
		this.nomeResponsavel = nomeResponsavel;
	}

	public LocalDate getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(LocalDate dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public LocalDate getDataMatricula() {
		return dataMatricula;
	}

	public void setDataMatricula(LocalDate dataMatricula) {
		this.dataMatricula = dataMatricula;
	}

	public byte[] getFoto() {
		return foto;
	}

	public void setFoto(byte[] foto) {
		this.foto = foto;
	}

	public Faixa getFaixa() {
		return faixa;
	}

	public void setFaixa(Faixa faixa) {
		this.faixa = faixa;
	}

	public Integer getAulasAssistidas() {
		return aulasAssistidas;
	}

	public void setAulasAssistidas(Integer aulasAssistidas) {
		this.aulasAssistidas = aulasAssistidas;
	}

	public Integer getMetaAulasNovaFaixa() {
		return metaAulasNovaFaixa;
	}

	public void setMetaAulasNovaFaixa(Integer metaAulasNovaFaixa) {
		this.metaAulasNovaFaixa = metaAulasNovaFaixa;
	}

	public StatusMensalidade getStatusMensalidade() {
		return statusMensalidade;
	}

	public void setStatusMensalidade(StatusMensalidade statusMensalidade) {
		this.statusMensalidade = statusMensalidade;
	}

	public LocalDate getDataVencimento() {
		return dataVencimento;
	}

	public void setDataVencimento(LocalDate dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	public Turma getTurma() {
		return turma;
	}

	public void setTurma(Turma turma) {
		this.turma = turma;
	}
    
}