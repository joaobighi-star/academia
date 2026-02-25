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

	public Faixa getFaixa() {
		return faixa;
	}

	public void setFaixa(Faixa faixa) {
		this.faixa = faixa;
	}

	public List<String> getDisponibilidadeAulasParticulares() {
		return disponibilidadeAulasParticulares;
	}

	public void setDisponibilidadeAulasParticulares(List<String> disponibilidadeAulasParticulares) {
		this.disponibilidadeAulasParticulares = disponibilidadeAulasParticulares;
	}

	public String getEspecialidade() {
		return especialidade;
	}

	public void setEspecialidade(String especialidade) {
		this.especialidade = especialidade;
	}
    
}