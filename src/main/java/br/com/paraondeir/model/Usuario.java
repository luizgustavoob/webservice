package br.com.paraondeir.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "usuario")
	private String usuario;
	
	@Column(name = "fcmid")
	private String fcmid;
	
	public Usuario(){
		super();
	}
	
	public Usuario(String usuario, String fcmid){
		super();
		this.usuario = usuario;
		this.fcmid = fcmid;
	}
	
	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getFcmid() {
		return fcmid;
	}

	public void setFcmid(String fcmid) {
		this.fcmid = fcmid;
	}
}
