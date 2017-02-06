package br.com.paraondeirwebservice.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "firebase")
public class Firebase implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "token")
	private String token;

	public Firebase(){
		super();
	}
	
	public Firebase(String token){
		super();
		this.token = token;
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
