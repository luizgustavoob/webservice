package br.com.paraondeir.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RegraAssociacao {

	private final Set<String> setSe = new HashSet<>();
	private final Set<String> setEntao = new HashSet<>();
	private List<Integer> listSe;
	private List<Integer> listEntao;
	
	public RegraAssociacao(Set<String> se, Set<String> entao){
		this.setSe.addAll(se);
        this.setEntao.addAll(entao);
        this.listSe = new ArrayList<Integer>();
        this.listEntao = new ArrayList<Integer>();
	}
	
	public Set<String> getHashSetSe() {
		return Collections.<String>unmodifiableSet(setSe);
	}
	
	public Set<String> getHashSetEntao() {
		return Collections.<String>unmodifiableSet(setEntao);
	}
	
	public List<Integer> getListSe() {
		return listSe;
	}

	public List<Integer> getListEntao() {
		return listEntao;
	}

	public void dadosHashToList(){
		Iterator<String> itSe = setSe.iterator();
		while (itSe.hasNext()){
			listSe.add(Integer.parseInt(itSe.next()));
		}
		
		Iterator<String> itEntao = setEntao.iterator();
		while (itEntao.hasNext()){
			listEntao.add(Integer.parseInt(itEntao.next()));
		}
	}
	
	@Override
	public String toString() {
		return "\"SE " + Arrays.toString(setSe.toArray()) + 
				", ENTÃO " + Arrays.toString(setEntao.toArray()) + "\"";
	}
	
    @Override
    public boolean equals(Object obj) {
        RegraAssociacao r = (RegraAssociacao) obj;

        return setSe.equals(r.getHashSetSe()) &&
               setEntao.equals(r.getHashSetEntao());
    }
    
    @Override
    public int hashCode() {
        return setSe.hashCode() ^ setEntao.hashCode();
    }
}
