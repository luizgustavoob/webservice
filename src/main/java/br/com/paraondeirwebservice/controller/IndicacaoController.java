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
import br.com.paraondeirwebservice.model.RegraAssociacao;
import br.com.paraondeirwebservice.repository.IAvaliacaoDao;
import br.com.paraondeirwebservice.repository.IEstabelecimentoDao;
import br.com.paraondeirwebservice.utils.Constantes;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@RestController
@RequestMapping(value = "/indicacao")
public class IndicacaoController {
	
	@Autowired
	private IAvaliacaoDao avaliacaoDao;
	@Autowired
	private IEstabelecimentoDao estabDao;

	@RequestMapping(value = "/", method = RequestMethod.POST, produces = "application/json")
	public String solicitaIndicacao(@RequestBody String json)
			throws JSONException {
		List<Estabelecimento> listaRetorno = new ArrayList<>();

		JSONObject jsonUsuario = new JSONObject(json);
		String usuario = jsonUsuario.getString("usuario");

		// Calcula suporte.
		List<String> listaUsuarios = avaliacaoDao.findUsuariosAvaliacao();
		List<HashMap<String, String>> listaAvaliacoes = getAvaliacoes(listaUsuarios, false);
		List<HashMap<String, String>> listaAvaliacoesPositivas = getAvaliacoes(listaUsuarios, true);

		List<int[]> listaItemsets = criaItemsetInicial();
		do {
			List<int[]> listaAuxiliar = 
					calculaSuporte(listaItemsets, listaUsuarios, listaAvaliacoes, listaAvaliacoesPositivas);
			listaItemsets.clear();
			listaItemsets = atualizaItemset(listaAuxiliar);
		} while (listaItemsets.size() != 1);

		boolean calculaConfianca = usuarioAvaliou(usuario, listaUsuarios) ? true : false;

		// Calcula confiança.
		if (calculaConfianca) {		
			double numRegrasPossiveis = 
					Math.pow(3, listaItemsets.get(0).length) 
					- (Math.pow(2, listaItemsets.get(0).length + 1)) + 1;
			
			List<int[]> listaConfianca = geraItemsetsConfianca(listaItemsets.get(0));
			List<RegraAssociacao> regras = new ArrayList<>();
			while (regras.size() != numRegrasPossiveis) {
				for (int i = 0; i < listaConfianca.size(); i++) {
					// gerar a regra. 
					int[] idsParaRegra = listaConfianca.get(i); // [1,2]
					double numRegrasItemsetMenor = Math.pow(2, idsParaRegra.length) - 2; // 2 regras
					int k = 0;
					k++;
					RegraAssociacao regra = geraRegraDeAssociacao(idsParaRegra);					
					double confianca = calculaConfianca(regra, listaUsuarios, 
							listaAvaliacoesPositivas);					
					if (confianca >= Constantes.CONFIANCA_MINIMA) {
						regras.add(regra);
					}
				}	
			}
			
			for (int j = 0; j < regras.size(); j++) {
				RegraAssociacao r = regras.get(j);
				if (usuarioGostouDoSe(usuario, r.getSe(), listaAvaliacoesPositivas)) {
					List<Integer> entao = r.getEntao();
					for (int k = 0; k < entao.size(); k++) {
						int e = entao.get(k);
						Estabelecimento estab = estabDao.findOne(e);
						listaRetorno.add(estab);
					}
				}
			}
		} else {
			for (int[] is : listaItemsets) {
				for (int l = 0; l < is.length; l++) {
					listaRetorno.add(estabDao.findOne(is[l]));
				}
			}
		}

		Gson gson = new Gson();
		JsonElement element = gson.toJsonTree(listaRetorno, new TypeToken<List<Estabelecimento>>(){}.getType());
		return element.getAsJsonArray().toString();
	}

	/**
	 * Verifica se os estabelecimentos presentes no parâmetro SE da regra foram
	 * avaliados positivamente pelo usuário da solicitação.
	 * @param usuario- usuário da solicitação.
	 * @param estabSe - estabelecimentos do parâmetro SE da regra.
	 * @param listaAvaliacoesPositivas - lista de avaliações positivas.
	 * @return true or false.
	 */
	private boolean usuarioGostouDoSe(String usuario, List<Integer> estabSe,
			List<HashMap<String, String>> listaAvaliacoesPositivas) {
		int count = 0;

		for (int i = 0; i < listaAvaliacoesPositivas.size(); i++) {
			String ids = listaAvaliacoesPositivas.get(i).get(usuario);
			if (ids != null) {
				String[] idsArray = ids.split(",");
				for (int j = 0; j < estabSe.size(); j++) {
					for (int k = 0; k < idsArray.length; k++) {
						if (estabSe.get(j) == Integer.parseInt(idsArray[k])) {
							count++;
						}
					}
				}
				break;
			}
		}

		return count == estabSe.size() ? true : false;
	}

	/**
	 * TODO. Aplicar o cálculo da confiança.
	 * @param regra
	 * @param listaUsuarios
	 * @param listaAvaliacoesPositivas
	 * @return
	 */
	private double calculaConfianca(RegraAssociacao regra,
			List<String> listaUsuarios,
			List<HashMap<String, String>> listaAvaliacoesPositivas) {

		return 0;
	}

	/**
	 * Verifica se determinado usuário já realizou alguma avaliação.
	 * @param usuario - usuário da solicitação.
	 * @param listaUsuarios - lista de usuários que já realizaram avaliações.
	 * @return avaliou ou não alguma vez.
	 */
	private boolean usuarioAvaliou(String usuario, List<String> listaUsuarios) {
		boolean retorno = false;
		for (int i = 0; i < listaUsuarios.size(); i++) {
			if (listaUsuarios.get(i).equalsIgnoreCase(usuario)) {
				retorno = true;
				break;
			}
		}
		return retorno;
	}

	/**
	 * Cria o itemset com elementos de tamanho 1.
	 * 
	 * @return lista com itemsets de tamanho 1.
	 */
	private List<int[]> criaItemsetInicial() {
		List<int[]> retorno = new ArrayList<>();
		for (Estabelecimento estab : estabDao.findAll()) {
			int[] id = { estab.getIdEstabelecimento() };
			retorno.add(id);
		}
		return retorno;
	}

	/**
	 * Aplica a fórmula do suporte (total de avaliações positivas do(s)
	 * estabelecimento(s) / total de avaliações do(s) estabelecimento(s).
	 * @param listaAtualItemset - lista com os itemsets atualizados.
	 * @param listaUsuarios - utilizados como transações.
	 * @param listaAvaliacoes - todas as avaliações de cada usuário.
	 * @param listaAvaliacoesPositivas - todas as avaliações positivas de cada usuário.
	 * @return lista com os itemsets cujo valor de suporte é maior que o mínimo proposto.
	 */
	private List<int[]> calculaSuporte(List<int[]> listaAtualItemset,
			List<String> listaUsuarios,
			List<HashMap<String, String>> listaAvaliacoes,
			List<HashMap<String, String>> listaAvaliacoesPositivas) {

		List<int[]> listaTemp = new ArrayList<>();

		for (int i = 0; i < listaAtualItemset.size(); i++) {
			int[] ids = listaAtualItemset.get(i);
			double total = countAvaliacoes(ids, listaUsuarios, listaAvaliacoes);
			double totalSim = countAvaliacoes(ids, listaUsuarios, listaAvaliacoesPositivas);
			double resultado = totalSim / total;

			if (resultado >= Constantes.SUPORTE_MINIMO) {
				listaTemp.add(ids);
			}
		}

		return listaTemp;
	}

	/**
	 * Retorna todas as avaliações realizadas pelos usuários.
	 * @param listaUsuarios - usuários que realizaram avaliações.
	 * @param filtraGostou - Indica se a consulta deve considerar o preenchimento do 
	 * campo "Gostou".
	 * @return lista com as avaliações realizadas, separadas por usuário.
	 */
	private List<HashMap<String, String>> getAvaliacoes(
			List<String> listaUsuarios, boolean filtraGostou) {
		List<HashMap<String, String>> listaRetorno = new ArrayList<>();

		for (String usuario : listaUsuarios) {
			List<Integer> listaEstabs = filtraGostou ? 
					avaliacaoDao.findEstabsByUsuarioAndGostou(usuario, "S") : 
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
	 * Contar as avaliações de um determinado grupo de estabelecimentos
	 * (itemsets).
	 * @param idsEstab - estabelecimentos avaliados.
	 * @param listaUsuarios - usuários que realizaram avaliações.
	 * @param listaAvaliacoes - lista de avaliações para contagem.
	 * @return total de avaliações realizadas sobre os estabelecimentos passados 
	 * por parâmetro.
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
					for (int j = 0; j < idsEstab.length; j++) {
						for (int k = 0; k < idsArray.length; k++) {
							if (idsEstab[j] == Integer.parseInt(idsArray[k])) {
								countAuxiliar++;
							}
						}
					}

					if (countAuxiliar == idsEstab.length) {
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
	 * @param listaAtualItemset - lista atual dos itemsets.
	 * @return lista de itemsets com seus elementos com tamanho n+1.
	 */
	private List<int[]> atualizaItemset(List<int[]> listaAtualItemset) {
		Integer tamanhoAtualItemset = listaAtualItemset.get(0).length;
		List<int[]> novaLista = new ArrayList<>();

		for (int i = 0; i < listaAtualItemset.size(); i++) {

			for (int j = i + 1; j < listaAtualItemset.size(); j++) {
				int[] tempI = listaAtualItemset.get(i);
				int[] tempJ = listaAtualItemset.get(j);
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
	 * Verifica se um determinado itemset deve ser adicionado a lista
	 * atualizada.
	 * @param novoArray - array candidato.
	 * @param listaProcura - lista atual.
	 */
	private void addElemento(int[] novoArray, List<int[]> listaProcura) {
		if (listaProcura.size() > 0) {			
			for (int p = 0; p < listaProcura.size(); p++) {
				int[] arrayBase = listaProcura.get(p);
				if (!temNoArray(novoArray, arrayBase)) {
					Arrays.sort(novoArray);
					listaProcura.add(novoArray);
					break;
				}
			}
		} else {
			Arrays.sort(novoArray);
			listaProcura.add(novoArray);
		}
	}

	/**
	 * Verifica se determinados elementos já fazem parte de um determinado
	 * array.
	 * @param elementos - estabelecimentos candidatos.
	 * @param arrayProcura - array base para verificação dos elementos.
	 * @return elementos existem ou não no array.
	 */
	private boolean temNoArray(int[] elementos, int[] arrayProcura) {
		int contador = 0;
		for (int i = 0; i < arrayProcura.length; i++) {
			for (int k = 0; k < elementos.length; k++) {
				if (arrayProcura[i] == elementos[k]) {
					contador++;
					break;
				}
			}
		}
		
		return contador == elementos.length ? true : false;
	}

	/**
	 * Divide um determinado array de estabelecimentos em grupos menores,
	 * sendo o tamanho mínimo 2.
	 * @param idsEstab - array a dividir.
	 * @return lista dos arrays gerados.
	 * @throws JSONException 
	 */	
	@RequestMapping(value = "/teste", method = RequestMethod.POST, produces = "application/json")
	private List<int[]> geraItemsetsConfianca(int[] idsEstab) {
	//private String geraItemsetsConfianca(@RequestBody String json) throws JSONException {
		List<int[]> listaRetorno = new ArrayList<int[]>();
		
		/*JSONObject jsonObject = new JSONObject(json);
		String idString = jsonObject.getString("array");
		String[] idsString = idString.split(",");
		int[] idsEstab = new int[idsString.length];
		for(int p = 0; p < idsString.length; p++){
			idsEstab[p] = Integer.parseInt(idsString[p]);
		}*/
		
		int controle = idsEstab.length;		
		int[] bit = new int[controle];
		int qtdeItemSets = (int) (Math.pow(2, controle) - 1);
		
		for (int i = 0; i < controle; i++){			
			bit[i] = 0;
		}
		
		for (int j = 1; j <= qtdeItemSets; j++){
			List<Integer> listIds = new ArrayList<>();			
			somaBit(bit, controle);
			
			for (int k = 0; k < controle; k++){
				if (bit[k] == 1){	
					listIds.add(idsEstab[k]);
				}				
			}	
			
			if (listIds.size() > 1){
				int[] ids = new int[listIds.size()];			
				for (int l = 0; l < listIds.size(); l++){
					ids[l] = listIds.get(l);
				}			
				addElemento(ids, listaRetorno);
			}
		}
		
		return listaRetorno;
		/*JSONObject jsonRetorno = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		for (int m = 0; m < listaRetorno.size(); m++){
			jsonArray.put(listaRetorno.get(m));
		}
		jsonRetorno.put("estabs", jsonArray.toString());
		return jsonRetorno.toString();*/
	}	
	
	/**
	 * Função auxiliar utilizada na divisão de um itemset em arrays menores.
	 * @see geraItemsetsConfianca
	 * @param arrayBits
	 * @param tamanho
	 */
	private void somaBit(int arrayBits[], int tamanho){
		for (int i = 0; i < tamanho; i++){
			if (arrayBits[i] == 0){
				arrayBits[i] = 1;
				break;
			}
			arrayBits[i] = 0;
		}
	}

	private RegraAssociacao geraRegraDeAssociacao(int[] idsParaRegra) {
		return null;		
	}
}
