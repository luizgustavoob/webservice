package br.com.paraondeirwebservice.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.paraondeirwebservice.model.Estabelecimento;
import br.com.paraondeirwebservice.model.RegraAssociacao;
import br.com.paraondeirwebservice.model.Usuario;
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
		Usuario usuario = new Gson().fromJson(json, new TypeToken<Usuario>(){}.getType());

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

		boolean calculaConfianca = usuarioAvaliou(usuario.getUsuario(), listaUsuarios) ? true : false;

		if (calculaConfianca) {		
			List<int[]> listaConfianca = geraItemsetsConfianca(listaItemsets.get(0));
			List<RegraAssociacao> regras = new ArrayList<>();			
			for (int i = 0; i < listaConfianca.size(); i++) {
				int[] itemset = listaConfianca.get(i);
				List<RegraAssociacao> regrasItemset = gerarRegrasDeAssociacao(itemset);
				for (RegraAssociacao regraAssociacao : regrasItemset) {
					regraAssociacao.dadosHashToList();
					double confianca = 0.9; //calculaConfianca(regraAssociacao, listaUsuarios, listaAvaliacoesPositivas);					
					if (confianca >= Constantes.CONFIANCA_MINIMA) {
						regras.add(regraAssociacao);
					}
				}
			}	
			
			for (int j = 0; j < regras.size(); j++) {
				RegraAssociacao r = regras.get(j);
				if (usuarioGostouDoSe(usuario.getUsuario(), r.getListSe(), listaAvaliacoesPositivas)) {
					List<Integer> entao = r.getListEntao();
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
		JsonElement element = gson.toJsonTree(listaRetorno, 
				new TypeToken<List<Estabelecimento>>(){}.getType());
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
	 */		
	private List<int[]> geraItemsetsConfianca(int[] idsEstab) {
		List<int[]> listaRetorno = new ArrayList<int[]>();		
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

	private List<RegraAssociacao> gerarRegrasDeAssociacao(int[] itemset){
		if (itemset.length >= 2){
			List<RegraAssociacao> regras = new ArrayList<>();
			String[] strItemset = new String[itemset.length];
			for (int i = 0; i < itemset.length; i++){
				strItemset[i] = itemset[i] + "";
			}
			
			Set<RegraAssociacao> setResultado = new HashSet<>();
			Set<String> set = new HashSet<>(Arrays.asList(strItemset));
			Set<RegraAssociacao> setRegras = gerarRegrasDeAssociacao(set);
			gerarRegrasDeAssociacao(set, setRegras, setResultado);
			
			for (RegraAssociacao regraAssociacao : setResultado) {
				regras.add(regraAssociacao);
			}
			
			return regras;
		}
		return null;
	}

	private Set<RegraAssociacao> gerarRegrasDeAssociacao(Set<String> itemset){	
		Set<RegraAssociacao> retorno = new HashSet<>(itemset.size());
		Set<String> se = new HashSet<>(itemset);
		Set<String> entao = new HashSet<>(1);
		for (String elemento : itemset) {
			se.remove(elemento);
			entao.add(elemento);
			retorno.add(new RegraAssociacao(se, entao));
			se.add(elemento);
			entao.remove(elemento);
		}
		return retorno;
	}
	
	private void gerarRegrasDeAssociacao(Set<String> itemset, Set<RegraAssociacao> setRegras,
			Set<RegraAssociacao> setResultado) {
		int k = itemset.size();
		int m = setRegras.iterator().next().getHashSetEntao().size();
		if (k > m + 1) {
			Set<RegraAssociacao> novasRegras = moveUmElementoPraCondicaoEntao(setRegras);
			Iterator<RegraAssociacao> iterator = novasRegras.iterator();
			while (iterator.hasNext()) {
				RegraAssociacao regra = iterator.next();
				setResultado.add(regra);
			}
			gerarRegrasDeAssociacao(itemset, novasRegras, setResultado);
		} else {
			Iterator<RegraAssociacao> iterator = setRegras.iterator();
			while (iterator.hasNext()) {
				RegraAssociacao regra = iterator.next();
				setResultado.add(regra);
			}
		}
	}

	private Set<RegraAssociacao> moveUmElementoPraCondicaoEntao(Set<RegraAssociacao> setRegras) {
		Set<RegraAssociacao> output = new HashSet<>();
		Set<String> se = new HashSet<>();
		Set<String> entao = new HashSet<>();
		
		for (RegraAssociacao regra : setRegras) {
			se.clear();
			entao.clear();
			se.addAll(regra.getHashSetSe());
			entao.addAll(regra.getHashSetEntao());

			for (String elemento : regra.getHashSetSe()) {
				se.remove(elemento);
				entao.add(elemento);
				RegraAssociacao novaRegra = new RegraAssociacao(se, entao);
				output.add(novaRegra);
				se.add(elemento);
				entao.remove(elemento);
			}
		}
		
		return output;
	}

}
