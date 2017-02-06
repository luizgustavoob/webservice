package br.com.paraondeirwebservice.utils;

import java.util.ArrayList;
import java.util.List;

public class ListaUtils {

	public static List<Integer> arrayAsListInteger(int[] arrayParam){
		List<Integer> listaRetorno = new ArrayList<>();
		for (int i = 0; i < arrayParam.length; i++){
			listaRetorno.add(arrayParam[i]);
		}
		return listaRetorno;
	}
}
