package br.com.paraondeir.model;

import java.io.Serializable;

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

@Entity
@Table(name = "endereco")
public class Endereco implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "endereco_id")
	@SequenceGenerator(name = "endereco_id", sequenceName = "endereco_id", allocationSize = 1)
	@Column(name = "idend")
	private int idend;
	
	@Column(name = "logradouro", nullable = false, length = 100)
	private String logradouro;
	
	@Column(name = "bairro", nullable = false, length = 100)
	private String bairro;
	
	@Column(name = "numero", nullable = false, length = 10)
	private String numero;
	
	@Column(name = "cep", nullable = false, length = 20)
	private String cep;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idcidade", referencedColumnName = "idcidade")
	private Cidade cidade;

	public int getIdend() {
		return idend;
	}

	public void setIdend(int idend) {
		this.idend = idend;
	}

	public String getLogradouro() {
		return logradouro;
	}

	public void setLogradouro(String logradouro) {
		this.logradouro = logradouro;
	}

	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public String getCep() {
		return cep;
	}

	public void setCep(String cep) {
		this.cep = cep;
	}

	public Cidade getCidade() {
		return cidade;
	}

	public void setCidade(Cidade cidade) {
		this.cidade = cidade;
	}

}
