package br.com.paraondeirwebservice.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "avaliacao")
public class Avaliacao implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
	private AvaliacaoPK avaliacaoId;
	
	@Column(name = "gostou", nullable = false)
	private String gostou;
	
	public AvaliacaoPK getAvaliacaoId() {
		return avaliacaoId;
	}

	public void setAvaliacaoId(AvaliacaoPK avaliacaoId) {
		this.avaliacaoId = avaliacaoId;
	}

	public String getGostou() {
		return gostou;
	}

	public void setGostou(String gostou) {
		this.gostou = gostou;
	}
}
