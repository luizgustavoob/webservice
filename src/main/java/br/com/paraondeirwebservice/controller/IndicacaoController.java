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

import br.com.paraondeirwebservice.model.Cidade;
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
		List<String> listaUsuarios = avaliacaoDao.findUsuarios();

		List<HashMap<String, String>> listaAvaliacoes = getAvaliacoes(
				listaUsuarios, false);

		List<HashMap<String, String>> listaAvaliacoesPositivas = getAvaliacoes(
				listaUsuarios, true);

		List<int[]> listaItemsets = criaItemsetInicial();
		do {
			List<int[]> listaAuxiliar = 
					calculaSuporte(listaItemsets, listaUsuarios, listaAvaliacoes, listaAvaliacoesPositivas);
			listaItemsets.clear();
			listaItemsets = atualizaItemset(listaAuxiliar);
		} while (listaItemsets.size() != 1);

		// boolean calculaConfianca = usuarioAvaliou(usuario, listaUsuarios) ?
		// true : false;
		boolean calculaConfianca = false;

		// Calcula confiança.
		if (calculaConfianca) {
			int[] idsEstab = listaItemsets.get(0);
			List<int[]> listaConfianca = geraItemsetsConfianca(idsEstab.length, idsEstab);
			List<RegraAssociacao> regras = new ArrayList<>();

			// Percorre a lista com os itemsets gerados pra confiança.
			for (int i = 0; i < listaConfianca.size(); i++) {
				int[] ids = listaConfianca.get(i);
				double numRegrasPossiveis = 
						Math.pow(3, ids.length) - (Math.pow(2, ids.length + 1)) + 1;
				int n = 0;
				while (n <= numRegrasPossiveis) {
					// gerar a regra. PRECISO DE AJUDA AQUI TAMBÉM.
					int[] se = null;
					int[] entao = null;
					RegraAssociacao regra = new RegraAssociacao(se, entao);

					double confianca = 
							calculaConfianca(regra, listaUsuarios, listaAvaliacoesPositivas);
					
					if (confianca >= Constantes.CONFIANCA_MINIMA) {
						regras.add(regra);
					}
					n++;
				}

				for (int j = 0; j < regras.size(); j++) {
					RegraAssociacao r = regras.get(j);
					if (usuarioGostouDoSe(usuario, r.getSe(), listaAvaliacoesPositivas)) {
						int[] entao = r.getEntao();
						for (int k = 0; k < entao.length; k++) {
							int e = entao[k];
							Estabelecimento estab = estabDao.findOne(e);
							listaRetorno.add(estab);
						}
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
				new TypeToken<List<Cidade>>() {}.getType());
		return element.getAsJsonArray().toString();
	}

	/**
	 * Verifica se os estabelecimentos presentes no parâmetro SE da regra foram
	 * avaliados positivamente pelo usuário da solicitação.
	 * 
	 * @param usuario- usuário da solicitação.
	 * @param estabSe - estabelecimentos do parâmetro SE da regra.
	 * @param listaAvaliacoesPositivas - lista de avaliações positivas.
	 * 
	 * @return true or false.
	 */
	private boolean usuarioGostouDoSe(String usuario, int[] estabSe,
			List<HashMap<String, String>> listaAvaliacoesPositivas) {
		int count = 0;

		for (int i = 0; i < listaAvaliacoesPositivas.size(); i++) {
			String ids = listaAvaliacoesPositivas.get(i).get(usuario);
			if (ids != null) {
				String[] idsArray = ids.split(",");
				for (int j = 0; j < estabSe.length; j++) {
					for (int k = 0; k < idsArray.length; k++) {
						if (estabSe[j] == Integer.parseInt(idsArray[k])) {
							count++;
						}
					}
				}
				break;
			}
		}

		return count == estabSe.length ? true : false;
	}

	/**
	 * TODO. Aplicar o cálculo da confiança.
	 * 
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
	 * 
	 * @param usuario - usuário da solicitação.
	 * @param listaUsuarios - lista de usuários que já realizaram avaliações.
	 * 
	 * @return avaliou ou não alguma vez.
	 */
	private boolean usuarioAvaliou(String usuario, List<String> listaUsuarios) {
		boolean retorno = false;
		for (int i = 0; i < listaUsuarios.size(); i++) {
			if (listaUsuarios.get(i) == usuario) {
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
	 * 
	 * @param listaAtualItemset - lista com os itemsets atualizados.
	 * @param listaUsuarios - utilizados como transações.
	 * @param listaAvaliacoes - todas as avaliações de cada usuário.
	 * @param listaAvaliacoesPositivas - todas as avaliações positivas de cada usuário.
	 * 
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
	 * 
	 * @param listaUsuarios - usuários que realizaram avaliações.
	 * @param filtraGostou - Indica se a consulta deve considerar o preenchimento do 
	 * campo "Gostou".
	 * 
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
	 * 
	 * @param idsEstab - estabelecimentos avaliados.
	 * @param listaUsuarios - usuários que realizaram avaliações.
	 * @param listaAvaliacoes - lista de avaliações para contagem.
	 * 
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
	 * 
	 * @param listaAtualItemset - lista atual dos itemsets.
	 * 
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
	 * 
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
	 * 
	 * @param elementos - estabelecimentos candidatos.
	 * @param arrayProcura - array base para verificação dos elementos.
	 * 
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
	 * TODO. Divide um determinado array de estabelecimentos em grupos menores,
	 * sendo o tamanho mínimo 2.
	 * 
	 * @param tamanhoLimite - tamanho máximo dos arrays a serem gerados.
	 * @param idsEstab - array a dividir.
	 * 
	 * @return lista dos arrays gerados.
	 */
	private List<int[]> geraItemsetsConfianca(int tamanhoLimite, int[] idsEstab) {
		/**
		 * Exemplo: Dado um array [2, 3, 4, 5], gerar os arrays [2, 3], [2, 4],
		 * [2, 5], [3, 4], [3, 5], [4, 5], [2, 3, 4], [2, 3, 5] e [3, 4, 5].
		 */
		List<int[]> listaRetorno = new ArrayList<int[]>();
		addElemento(idsEstab, listaRetorno);
		return listaRetorno;
	}

}
