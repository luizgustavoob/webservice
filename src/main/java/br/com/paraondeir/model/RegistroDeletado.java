package br.com.paraondeirwebservice.model;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "registro_deletado")
public class RegistroDeletado implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
	private RegistroDeletadoPK registroDeletadoId;
	
	@Column(name = "data_exclusao", nullable = false)
	private Calendar dataExclusao;

	public RegistroDeletadoPK getRegistroDeletadoId() {
		return registroDeletadoId;
	}

	public void setRegistroDeletadoId(RegistroDeletadoPK registroDeletadoId) {
		this.registroDeletadoId = registroDeletadoId;
	}

	public Calendar getDataExclusao() {
		return dataExclusao;
	}

	public void setDataExclusao(Calendar dataExclusao) {
		this.dataExclusao = dataExclusao;
	}

}
