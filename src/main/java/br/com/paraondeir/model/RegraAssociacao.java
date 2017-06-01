package br.com.paraondeir.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RegraAssociacao {

	private final Set<Integer> setSe = new HashSet<>();
	private final Set<Integer> setEntao = new HashSet<>();
	private List<Integer> listSe;
	private List<Integer> listEntao;
	
	public RegraAssociacao(Set<Integer> se, Set<Integer> entao){
		this.setSe.addAll(se);
        this.setEntao.addAll(entao);
        this.listSe = new ArrayList<Integer>();
        this.listEntao = new ArrayList<Integer>();
	}
	
	public Set<Integer> getHashSetSe() {
		return Collections.<Integer>unmodifiableSet(setSe);
	}
	
	public Set<Integer> getHashSetEntao() {
		return Collections.<Integer>unmodifiableSet(setEntao);
	}
	
	public List<Integer> getListSe() {
		return listSe;
	}

	public List<Integer> getListEntao() {
		return listEntao;
	}

	public void dadosHashToList(){
		Iterator<Integer> itSe = setSe.iterator();
		while (itSe.hasNext()){
			listSe.add(itSe.next());
		}
		
		Iterator<Integer> itEntao = setEntao.iterator();
		while (itEntao.hasNext()){
			listEntao.add(itEntao.next());
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
