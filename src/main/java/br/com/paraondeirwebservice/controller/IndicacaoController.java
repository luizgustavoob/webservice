package br.com.paraondeirwebservice.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.paraondeirwebservice.model.Estabelecimento;
import br.com.paraondeirwebservice.repository.IAvaliacaoDao;
import br.com.paraondeirwebservice.repository.IEstabelecimentoDao;
import br.com.paraondeirwebservice.utils.Constantes;
import br.com.paraondeirwebservice.utils.ListaUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@RestController
@RequestMapping(value = "/indicacao")
public class IndicacaoController {

	@Autowired
	private IEstabelecimentoDao estabDao;
	@Autowired
	private IAvaliacaoDao avaliacaoDao;
	
	@RequestMapping(value = "/", method = RequestMethod.POST, produces = "application/json")
	public String solicitaIndicacao(@RequestBody String json) throws JSONException {		
		boolean calculaConfianca = true;
		
		// Recuperar o usuário que está solicitando indicação.
		JSONObject objeto = new JSONObject(json);
		String usuario = objeto.getString("usuario");		
		if (usuario == null || usuario.isEmpty()){
			calculaConfianca = false;
		}
		
		// Cálculo do suporte primeiramente
		List<int[]> listaItemsets = criaItemsetInicial();		
		
		do {
			listaItemsets = calculaSuporte(listaItemsets);
			listaItemsets = atualizaItemsets(listaItemsets);			
		} while (listaItemsets.size() != 1);
		
		// Cálculo da confiança.
		if (calculaConfianca){
			
		}
		
		List<Estabelecimento> listaRetorno = toListEstabelecimento(listaItemsets);
		Gson gson = new Gson();
		JsonElement element = gson.toJsonTree(listaRetorno, new TypeToken<List<Estabelecimento>>() {}.getType());
		return element.getAsJsonArray().toString();		
	}


	/**
	 * Cria a lista de itemSets inicial, com os elementos de tamanho 1
	 * @return lista de itemsets
	 */
	private List<int[]> criaItemsetInicial(){
		List<int[]> retorno = new ArrayList<>();
		for (Estabelecimento estab : estabDao.findAll()) {
			int[] id = {estab.getIdEstabelecimento()};
			retorno.add(id);
		}
		return retorno;
	}
	
	
	/**
	 * TODO
	 * Filtra os itemSets aplicando o cálculo do suporte
	 * @param listaItemsets = lista com os itemSets atuais
	 */
	private List<int[]> calculaSuporte(List<int[]> listaItemsetsAtual) {
		List<int[]> listaTemp = new ArrayList<>();	
		
		for (int i = 0; i < listaItemsetsAtual.size(); i++){
			List<Integer> listaIds = ListaUtils.arrayAsListInteger(listaItemsetsAtual.get(i));
			double total = avaliacaoDao.countAvaliacoesByEstabelecimentos(listaIds);
			double totalSim = avaliacaoDao.countAvaliacoesByEstabelecimentosAndGostou(listaIds, "S");
			double resultado = totalSim / total;
			
			if (resultado >= Constantes.SUPORTE_MINIMO){
				listaTemp.add(listaItemsetsAtual.get(i));
			}
		}
		
		// Lista de itemsets só com os que passaram pelo calculo do suporte
		return listaTemp;
	}
	
	
	/**
	 * Atualiza a lista de itemsets usada no cálculo do suporte, incrementando seu tamanho em 1. 
	 * @param listaItemsetsAtual
	 * @return listaAtualizada com tamanho n + 1
	 */
	private List<int[]> atualizaItemsets(List<int[]> listaItemsetsAtual) {
		int tamanhoAtualItemset = listaItemsetsAtual.get(0).length;
		List<int[]> novaLista = new ArrayList<>();
		
		for (int i = 0; i < listaItemsetsAtual.size(); i++) {			
			for (int j = i + 1; j < listaItemsetsAtual.size(); j++) {				
				int[] tempI = listaItemsetsAtual.get(i);
                int[] tempJ = listaItemsetsAtual.get(j);
                int[] novoArray = new int[tamanhoAtualItemset + 1];
                
                for (int k = 0; k < novoArray.length-1; k++) {
                	novoArray[k] = tempI[k];
                }
                
                int diferente = 0;                
                for (int l = 0; l < tempJ.length; l++) {                	
                	boolean encontrou = false;                	
                    for (int p = 0; p < tempI.length; p++) {                    	
                    	if (tempI[p] == tempJ[l]) { 
                    		encontrou = true;
                    		break;
                    	}
                	}
                    
                	if (!encontrou) {
                		diferente++;
                		novoArray[novoArray.length -1] = tempJ[l];
                	}
            	}               
                
                if (diferente == 1) {
                	Arrays.sort(novoArray);
                	novaLista.add(novoArray);
                }
			}
		}
		
		return novaLista;
	}
	
	
	@RequestMapping(value = "/regrasAssociacao", method = RequestMethod.POST, produces = "application/json")
	public String geraListaItemsetParaCalculoConfiança(@RequestBody String json) {
		List<int[]> lista = new ArrayList<>();
		
		char[] tempChar = json.toCharArray();
		int[] temp = new int[tempChar.length];		
		for (int m = 0; m < temp.length; m++){
			temp[m] = Character.getNumericValue(tempChar[m]);
		}		
																
		int tamanhoLimiteItemset = (temp.length - 1);			
		while (tamanhoLimiteItemset > 0){		
			int[] teste = new int[tamanhoLimiteItemset];									
						
			tamanhoLimiteItemset--;
		}
		return "";		
	}
	
	/**
	 * Converte a lista contendo os códigos dos estabelecimentos em uma lista de objetos da classe Estabelecimento
	 * @param listParam = lista com os ids dos estabelecimentos
	 * @return lista de objetos da classe Estabelecimento
	 */
	private List<Estabelecimento> toListEstabelecimento(List<int[]> listParam){
		List<Estabelecimento> listaRetorno = new ArrayList<>();
		
		for (int i = 0; i < listParam.size(); i++){
			int[] arrayTemp = listParam.get(i);
			
			for (int j = 0; j < arrayTemp.length; j++){
				Estabelecimento estab = estabDao.findOne(arrayTemp[i]);
				
				if (!listaRetorno.contains(estab)){
					listaRetorno.add(estab);
				}
			}
		}
		return listaRetorno;
	}
}
