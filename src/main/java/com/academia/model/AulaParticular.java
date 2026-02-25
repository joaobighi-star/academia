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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Disponibilidade getDisponibilidade() {
		return disponibilidade;
	}

	public void setDisponibilidade(Disponibilidade disponibilidade) {
		this.disponibilidade = disponibilidade;
	}

	public Aluno getAluno() {
		return aluno;
	}

	public void setAluno(Aluno aluno) {
		this.aluno = aluno;
	}

	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}

	public LocalDateTime getDataHora() {
		return dataHora;
	}

	public void setDataHora(LocalDateTime dataHora) {
		this.dataHora = dataHora;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}

	public String getMensagemProfessor() {
		return mensagemProfessor;
	}

	public void setMensagemProfessor(String mensagemProfessor) {
		this.mensagemProfessor = mensagemProfessor;
	}

	public boolean isPresencaConfirmada() {
		return presencaConfirmada;
	}

	public void setPresencaConfirmada(boolean presencaConfirmada) {
		this.presencaConfirmada = presencaConfirmada;
	}
    
}