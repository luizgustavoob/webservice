package br.com.paraondeirwebservice.model;

public class RegraAssociacao {

	private int[] se;
	private int[] entao;
	
	public RegraAssociacao(){
	}
	
	public RegraAssociacao(int[] se, int[] entao){
		this.se = se;
		this.entao = entao;
	}
	
	public int[] getSe() {
		return se;
	}
	
	public void setSe(int[] se) {
		this.se = se;
	}
	
	public int[] getEntao() {
		return entao;
	}
	
	public void setEntao(int[] entao) {
		this.entao = entao;
	}
}
