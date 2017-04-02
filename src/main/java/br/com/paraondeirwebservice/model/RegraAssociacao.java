package br.com.paraondeirwebservice.model;

import java.util.List;

public class RegraAssociacao {

	private List<Integer> se;
	private List<Integer> entao;
	
	public RegraAssociacao(){
	}
	
	public List<Integer> getSe() {
		return se;
	}
	
	public void setSe(List<Integer> se) {
		this.se = se;
	}
	
	public List<Integer> getEntao() {
		return entao;
	}
	
	public void setEntao(List<Integer> entao) {
		this.entao = entao;
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}
}
