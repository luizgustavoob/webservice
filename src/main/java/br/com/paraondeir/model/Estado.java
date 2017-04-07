package br.com.paraondeirwebservice.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "uf")
public class Estado implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "uf_id")
	@SequenceGenerator(name = "uf_id", sequenceName = "uf_id", allocationSize = 1)
	@Column(name = "iduf")
	private int idUf;
	
	@Column(name = "nome", nullable = false, length = 100)
	private String nome;
	
	@Column(name = "sigla", nullable = false, unique = true, length = 2)
	private String sigla;

	public int getIdUf() {
		return idUf;
	}

	public void setIdUf(int idUf) {
		this.idUf = idUf;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSigla() {
		return sigla;
	}

	public void setSigla(String sigla) {
		this.sigla = sigla;
	}
}
