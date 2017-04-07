package br.com.paraondeir.model;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "avaliacao")
public class Avaliacao implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "avaliacao_id")
	@SequenceGenerator(name = "avaliacao_id", sequenceName = "avaliacao_id", allocationSize = 1)
	@Column(name = "avaliacaoid")
	private int avaliacaoid;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idestabelecimento", referencedColumnName = "idestabelecimento")
	private Estabelecimento estabelecimento;
	
	@Column(name = "usuario")
	private String usuario;
	
	@Column(name = "gostou", nullable = false)
	private String gostou;
	
	@Transient
	private String data;
	
	@Column(name = "dtavaliacao", columnDefinition = "timestamp")
	private Calendar dtAvaliacao;

	public int getAvaliacaoid() {
		return avaliacaoid;
	}

	public void setAvaliacaoid(int avaliacaoid) {
		this.avaliacaoid = avaliacaoid;
	}

	public Estabelecimento getEstabelecimento() {
		return estabelecimento;
	}

	public void setEstabelecimento(Estabelecimento estabelecimento) {
		this.estabelecimento = estabelecimento;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getGostou() {
		return gostou;
	}

	public void setGostou(String gostou) {
		this.gostou = gostou;
	}
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Calendar getDtAvaliacao() {
		return dtAvaliacao;
	}

	public void setDtAvaliacao(Calendar dtAvaliacao) {
		this.dtAvaliacao = dtAvaliacao;
	}
}