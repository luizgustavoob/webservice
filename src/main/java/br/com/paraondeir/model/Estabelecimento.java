package br.com.paraondeir.model;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "estabelecimento")
public class Estabelecimento implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "estabelecimento_id")
	@SequenceGenerator(name = "estabelecimento_id", sequenceName = "estabelecimento_id", allocationSize = 1)
	@Column(name = "idestabelecimento")
	private int idEstabelecimento;
	
	@Column(name = "nome", nullable = false, length = 100)
	private String nome;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idend", referencedColumnName = "idend")
	private Endereco endereco;
	
	@Column(name = "telefone", length = 20)
	private String telefone;
	
	@Lob 
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "imagem")
	//@Type(type="org.hibernate.type.WrappedMaterializedBlobType")
	private byte[] imagem;
	
	public int getIdEstabelecimento() {
		return idEstabelecimento;
	}

	public void setIdEstabelecimento(int idEstabelecimento) {
		this.idEstabelecimento = idEstabelecimento;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public byte[] getImagem() {
		return imagem;
	}

	public void setImagem(byte[] imagem) {
		this.imagem = imagem;
	}
}
