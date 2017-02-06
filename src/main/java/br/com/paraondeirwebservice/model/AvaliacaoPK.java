package br.com.paraondeirwebservice.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class AvaliacaoPK implements Serializable {

private static final long serialVersionUID = 1L;
	
	@Column(name = "idestabelecimento")
	private Estabelecimento estabelecimento;
	
	@Column(name = "usuario")
	private String usuario;

	public AvaliacaoPK() {
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
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AvaliacaoPK){
			AvaliacaoPK pk = (AvaliacaoPK) obj;
			
			if (!pk.getEstabelecimento().equals(this.estabelecimento)){
				return false;
			}
			
			if (pk.getUsuario() != this.usuario){
				return false;
			}
			
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {		
		return super.hashCode() + this.estabelecimento.getIdEstabelecimento();
	}
}
