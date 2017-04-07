package br.com.paraondeir.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class RegistroDeletadoPK implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "nome_tabela")
	private String nomeTabela;
	
	@Column(name = "id_tabela")
	private int idTabela;
	
	public RegistroDeletadoPK() {
	}

	public String getNomeTabela() {
		return nomeTabela;
	}
	
	public void setNomeTabela(String nomeTabela) {
		this.nomeTabela = nomeTabela;
	}
	
	public int getIdTabela() {
		return idTabela;
	}
	
	public void setIdTabela(int idTabela) {
		this.idTabela = idTabela;
	}

	@Override
	public int hashCode() {
		return nomeTabela.hashCode() + idTabela;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RegistroDeletadoPK){
			RegistroDeletadoPK pk = (RegistroDeletadoPK) obj;
			
			if (!pk.getNomeTabela().equals(this.nomeTabela)){
				return false;
			}
			
			if (pk.getIdTabela() != this.idTabela){
				return false;
			}
			
			return true;
		}
		return false;
	}
	
	
		
}
