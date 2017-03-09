package br.com.paraondeirwebservice.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

@RestController
@RequestMapping(value = "/indicacao")
public class IndicacaoController {

	@Autowired
	private IAvaliacaoDao avaliacaoDao;
	@Autowired
	private IEstabelecimentoDao estabDao;

	@RequestMapping(value = "/", method = RequestMethod.POST, produces = "application/json")
	public String solicitaIndicacao(@RequestBody String json) throws JSONException {
		boolean calculaConfianca = true;

		// Recuperar o usuário que está solicitando indicação.
		JSONObject objeto = new JSONObject(json);
		String usuario = objeto.getString("usuario");
		if (usuario == null || usuario.isEmpty()) {
			calculaConfianca = false;
		}

		List<String> listaUsuarios = avaliacaoDao.findUsuarios();
		List<HashMap<String, String>> listaAvaliacoes = getAvaliacoes(listaUsuarios, false);
		List<HashMap<String, String>> listaAvaliacoesPositivas = getAvaliacoes(listaUsuarios, true);

		List<int[]> listaItemsets = criaItemsetInicial();							
		do {
			List<int[]> listaAuxiliar = calculaSuporte(listaItemsets, listaUsuarios, 
					listaAvaliacoes, listaAvaliacoesPositivas);
			listaItemsets.clear();
			listaItemsets = atualizaItemset(listaAuxiliar);
		} while(listaItemsets.size() != 1);
		
		/* Testar retorno do cálculo do suporte.
		JSONObject jsonRetorno = new JSONObject();
		JSONArray array = new JSONArray();
		for (int[] is : listaItemsets) {
			array.put(is);
		}
		jsonRetorno.put("estabs", array);
		return jsonRetorno.toString();*/
		
		if (calculaConfianca){
			// Montar as regras de associação.
		}
		return "";
	}

	/**
	 * Cria o itemset com elementos de tamanho 1.
	 * @return lista com itemsets de tamanho 1.
	 */
	private List<int[]> criaItemsetInicial() {
		List<int[]> retorno = new ArrayList<>();
		for (Estabelecimento estab : estabDao.findAll()) {
			int[] id = {estab.getIdEstabelecimento()};
			retorno.add(id);
		}
		return retorno;
	}
	
	/**
	 * Aplica a fórmula do suporte (total de avaliações positivas do(s) estabelecimento(s) / total
	 * de avaliações do(s) estabelecimento(s).
	 * 
	 * @param listaItemsetAtual - lista com os itemsets atualizados.
	 * @param listaUsuarios - utilizados como transações.
	 * @param listaAvaliacoes - todas as avaliações de cada usuário.
	 * @param listaAvaliacoesPositivas - todas as avaliações positivas de cada usuário.
	 * 
	 * @return lista com os itemsets cujo valor de suporte é maior que o mínimo proposto.
	 */
	private List<int[]> calculaSuporte(List<int[]> listaItemsetAtual,
			List<String> listaUsuarios,
			List<HashMap<String, String>> listaAvaliacoes,
			List<HashMap<String, String>> listaAvaliacoesPositivas){
		
		List<int[]> listaTemp = new ArrayList<>();	
		
		for (int i = 0; i < listaItemsetAtual.size(); i++){	
			int[] ids = listaItemsetAtual.get(i);
			double total = countAvaliacoes(ids, listaUsuarios, listaAvaliacoes);
			double totalSim = countAvaliacoes(ids, listaUsuarios, listaAvaliacoesPositivas);
			double resultado = totalSim / total;
			
			if (resultado >= Constantes.SUPORTE_MINIMO){
				listaTemp.add(ids);
			}
		}
		
		return listaTemp;
	}
	
	/**
	 * Retorna todas as avaliações realizadas pelos usuários.
	 * 
	 * @param listaUsuarios - usuários que realizaram avaliações.
	 * @param filtraGostou - Indica se a consulta deve considerar o preenchimento do campo "Gostou".
	 * 
	 * @return lista com as avaliações realizadas, separadas por usuário.
	 */
	private List<HashMap<String, String>> getAvaliacoes(List<String> listaUsuarios, 
			boolean filtraGostou){
		List<HashMap<String, String>> listaRetorno = new ArrayList<HashMap<String, String>>();
		
		for (String usuario : listaUsuarios) {
			List<Integer> listaEstabs = filtraGostou ? avaliacaoDao.findEstabsByUsuarioAndGostou(usuario, "S") : 
				avaliacaoDao.findEstabsByUsuario(usuario);
				
			StringBuilder sb = new StringBuilder();
			for (Integer idEstab : listaEstabs) {
				sb.append(idEstab);
				sb.append(",");
			}
			HashMap<String, String> hash = new HashMap<String, String>();
			String ids = sb.toString();
			hash.put(usuario, ids.substring(0, ids.length() - 1));
			listaRetorno.add(hash);
		}
				
		return listaRetorno;
	}

	/**
	 * Contar as avaliações de um determinado grupo de estabelecimentos (itemsets).
	 * 
	 * @param idsEstab - estabelecimentos avaliados.
	 * @param listaUsuarios - usuários que realizaram avaliações.
	 * @param listaAvaliacoes - lista de avaliações para contagem.
	 * 
	 * @return total de avaliações realizadas sobre os estabelecimentos passados por parâmetro.
	 */
	private int countAvaliacoes(int[] idsEstab, List<String> listaUsuarios, 
			List<HashMap<String, String>> listaAvaliacoes) {
		int countRetorno = 0;
		int countAuxiliar = 0;
		
		for (String usuario : listaUsuarios) {
			for (int i = 0; i < listaAvaliacoes.size(); i++) {
				countAuxiliar = 0;
				String ids = listaAvaliacoes.get(i).get(usuario);
				if (ids != null) {
					String[] idsArray = ids.split(",");
					for (int j = 0; j < idsEstab.length; j++ ){
						for (int k = 0; k < idsArray.length; k++){
							if (idsEstab[j] == Integer.parseInt(idsArray[k])) {
								countAuxiliar++;							
							}
						}
					}
					
					if (countAuxiliar == idsEstab.length){
						countRetorno++;
					}
					
					break;
				}
			}
		}
		
		return countRetorno;
		
	}
	
	/**
	 * Atualiza a lista de itemsets usadas no cálculo do suporte, incremento o 
	 * tamanho do seus elementos em 1.
	 * 
	 * @param listaItemsetsAtual - lista atual dos itemsets.
	 * 
	 * @return lista de itemsets com seus elementos com tamanho n+1.
	 */
	private List<int[]> atualizaItemset(List<int[]> listaItemsetsAtual) {
		Integer tamanhoAtualItemset = listaItemsetsAtual.get(0).length;
		List<int[]> novaLista = new ArrayList<>();

		for (int i = 0; i < listaItemsetsAtual.size(); i++) {

			for (int j = i + 1; j < listaItemsetsAtual.size(); j++) {
				int[] tempI = listaItemsetsAtual.get(i);
				int[] tempJ = listaItemsetsAtual.get(j);
				int[] novoArray = new int[tamanhoAtualItemset + 1];

				for (int k = 0; k < novoArray.length - 1; k++) {
					novoArray[k] = tempI[k];
				}

				int diferente = 0;
				for (int l = 0; l < tempJ.length; l++) {
					boolean encontrou = false;
					for (int m = 0; m < tempI.length; m++) {
						if (tempI[m] == tempJ[l]) {
							encontrou = true;
							break;
						}
					}

					if (!encontrou) {
						diferente++;
						novoArray[novoArray.length - 1] = tempJ[l];
					}
				}

				if (diferente == 1) {
					addElemento(novoArray, novaLista);
				}
			}
		}

		return novaLista;
	}
	
	/**
	 * Verifica se um determinado itemset deve ser adicionado a lista atualizada.
	 * 
	 * @param novoArray - array candidato.
	 * @param novaLista - lista atual.
	 */
	private void addElemento(int[] novoArray, List<int[]> novaLista) {
		if (novaLista.size() > 0){
			for (int p = 0; p < novaLista.size(); p++){
				int[] arrayBase = novaLista.get(p);
				if (!temNoArray(novoArray, arrayBase)){
					Arrays.sort(novoArray);					
					novaLista.add(novoArray);
					break;
				}
			}
		} else {
			Arrays.sort(novoArray);					
			novaLista.add(novoArray);
		}
		
	}

	/**
	 * Verifica se determinados elementos já fazem parte de um determinado array.
	 * @param elementos - estabelecimentos candidatos.
	 * @param arrayBase - array base para verificação dos elementos.
	 * 
	 * @return elementos existem ou não no array.
	 */
	private boolean temNoArray(int[] elementos, int[] arrayBase){		
		int contador = 0;
		for (int i = 0; i < arrayBase.length; i++){
			for (int k = 0; k < elementos.length; k++){
				if (arrayBase[i] == elementos[k]){
					contador++;
					break;
				}
			}
		}
		return contador == elementos.length ? true : false;
	}

}
