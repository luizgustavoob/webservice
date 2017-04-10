package br.com.paraondeir.controller;

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

import br.com.paraondeir.model.Estabelecimento;
import br.com.paraondeir.model.RegraAssociacao;
import br.com.paraondeir.model.Usuario;
import br.com.paraondeir.repository.IAvaliacaoDao;
import br.com.paraondeir.repository.IEstabelecimentoDao;
import br.com.paraondeir.utils.Constantes;

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
		
		List<Estabelecimento> retorno = new ArrayList<>();
		Usuario usuario = new Gson().fromJson(json, new TypeToken<Usuario>() {}.getType());

		List<String> usuarios = avaliacaoDao.findUsuariosAvaliacao();
		List<HashMap<String, String>> avaliacoes = getAvaliacoes(usuarios, false);
		List<HashMap<String, String>> avaliacoesPositivas = getAvaliacoes(usuarios, true);

		List<int[]> itemsets = criaItemsetInicial();
		do {
			List<int[]> auxiliar = calculaSuporte(itemsets, usuarios,
					avaliacoes, avaliacoesPositivas);
			if (auxiliar.size() > 1){
				itemsets.clear();
				itemsets = atualizaItemset(auxiliar);
			} else {
				itemsets = auxiliar;
			}
		} while (itemsets.size() != 1);

		boolean calculaConfianca = usuarioAvaliou(usuario.getUsuario(),
				usuarios) ? true : false;

		if (calculaConfianca) {
			List<int[]> itemsetsConfianca = geraItemsetsConfianca(itemsets.get(0));
			List<RegraAssociacao> regras = new ArrayList<>();
			for (int i = 0; i < itemsetsConfianca.size(); i++) {
				int[] itemset = itemsetsConfianca.get(i);
				List<RegraAssociacao> regrasItemset = geraRegrasDeAssociacao(itemset);
				for (RegraAssociacao regra : regrasItemset) {
					regra.dadosHashToList();
					double confianca = calculaConfianca(regra, usuarios, avaliacoesPositivas);
					if (confianca >= Constantes.CONFIANCA_MINIMA) {
						regras.add(regra);
					}
				}
			}

			// Confirmar a efetividade desta parte do código.
			for (int i = 0; i < regras.size(); i++) {
				RegraAssociacao r = regras.get(i);
				if (usuarioGostouDaCondicaoSe(usuario.getUsuario(), r.getListSe(), avaliacoesPositivas)) {
					List<Integer> entao = r.getListEntao();
					for (int j = 0; j < entao.size(); j++) {
						Estabelecimento estab = estabDao.findOne(entao.get(j));
						addElemento(estab, retorno);
					}
				}
			}
		} else {
			for (int[] itemset : itemsets) {
				for (int i = 0; i < itemset.length; i++) {
					addElemento(estabDao.findOne(itemset[i]), retorno);
				}
			}
		}

		Gson gson = new Gson();
		JsonElement element = gson.toJsonTree(retorno,
				new TypeToken<List<Estabelecimento>>() {
				}.getType());
		return element.getAsJsonArray().toString();
	}

	/**
	 * Verifica se os estabelecimentos presentes no parâmetro SE da regra foram
	 * avaliados positivamente pelo usuário da solicitação.
	 * 
	 * @param usuario - usuário da solicitação.
	 * @param se - estabelecimentos do parâmetro SE da regra.
	 * @param avaliacoesPositivas - lista de avaliações positivas.
	 * @return true or false.
	 */
	private boolean usuarioGostouDaCondicaoSe(String usuario, List<Integer> se,
			List<HashMap<String, String>> avaliacoesPositivas) {
		int count = 0;
		for (int i = 0; i < avaliacoesPositivas.size(); i++) {
			String avaliacoes = avaliacoesPositivas.get(i).get(usuario);
			if (avaliacoes != null) {
				String[] estabs = avaliacoes.split(",");
				for (int j = 0; j < se.size(); j++) {
					for (int k = 0; k < estabs.length; k++) {
						if (se.get(j) == Integer.parseInt(estabs[k])) {
							count++;
						}
					}
				}
				break;
			}
		}

		return count == se.size() ? true : false;
	}

	/**
	 * Aplicar o cálculo da confiança.
	 * 
	 * @param regra - regra que sofrerá o cálculo.
	 * @param usuarios - usuários que realizaram avaliações.
	 * @param avaliacoesPositivas - lista com as avaliações.
	 * @return valor da confiança.
	 */
	private double calculaConfianca(RegraAssociacao regra,
			List<String> usuarios,
			List<HashMap<String, String>> avaliacoesPositivas) {

		List<Integer> listItemset = new ArrayList<>();
		List<Integer> listSe = new ArrayList<>();
		for (int i = 0; i < regra.getListSe().size(); i++) {
			addElemento(regra.getListSe().get(i), listItemset);
			addElemento(regra.getListSe().get(i), listSe);
		}
		for (int i = 0; i < regra.getListEntao().size(); i++) {
			addElemento(regra.getListEntao().get(i), listItemset);
		}

		int[] itemset = new int[listItemset.size()];
		int[] se = new int[listSe.size()];
		for (int i = 0; i < listItemset.size(); i++) {
			itemset[i] = listItemset.get(i);
		}
		for (int i = 0; i < listSe.size(); i++) {
			se[i] = listSe.get(i);
		}

		double total = contarAvaliacoes(itemset, usuarios, avaliacoesPositivas);
		double totalSe = contarAvaliacoes(se, usuarios, avaliacoesPositivas);
		return total / totalSe;
	}

	/**
	 * Verifica se determinado usuário já realizou alguma avaliação.
	 * 
	 * @param usuario - usuário da solicitação.
	 * @param usuarios - lista de usuários que já realizaram avaliações.
	 * @return true or false
	 */
	private boolean usuarioAvaliou(String usuario, List<String> usuarios) {
		boolean retorno = false;
		for (int i = 0; i < usuarios.size(); i++) {
			if (usuarios.get(i).equalsIgnoreCase(usuario)) {
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
	 * Aplica a fórmula do suporte.
	 * 
	 * @param itemsets - lista com os itemsets atualizados.
	 * @param usuarios - utilizados como transações.
	 * @param avaliacoes - todas as avaliações de cada usuário.
	 * @param avaliacoesPositivas - todas as avaliações positivas de cada usuário.
	 * @return lista com os itemsets cujo valor de suporte é maior que o mínimo proposto.
	 */
	private List<int[]> calculaSuporte(List<int[]> itemsets,
			List<String> usuarios, List<HashMap<String, String>> avaliacoes,
			List<HashMap<String, String>> avaliacoesPositivas) {

		List<int[]> retorno = new ArrayList<>();

		for (int i = 0; i < itemsets.size(); i++) {
			int[] itemset = itemsets.get(i);
			double total = contarAvaliacoes(itemset, usuarios, avaliacoes);
			double totalSim = contarAvaliacoes(itemset, usuarios,
					avaliacoesPositivas);
			double resultado = totalSim / total;

			if (resultado >= Constantes.SUPORTE_MINIMO) {
				addElemento(itemset, retorno);
			}
		}

		return retorno;
	}

	/**
	 * Retorna todas as avaliações realizadas pelos usuários.
	 * 
	 * @param usuarios - usuários que realizaram avaliações.
	 * @param filtraParamGostou - Indica se a consulta deve considerar o preenchimento do 
	 * campo "Gostou".
	 * @return lista com as avaliações realizadas, separadas por usuário.
	 */
	private List<HashMap<String, String>> getAvaliacoes(List<String> usuarios,
			boolean filtraParamGostou) {
		List<HashMap<String, String>> retorno = new ArrayList<>();

		for (String usuario : usuarios) {
			List<Integer> estabelecimentos = filtraParamGostou ? 
					avaliacaoDao.findEstabsByUsuarioAndGostou(usuario, "S") : 
					avaliacaoDao.findEstabsByUsuario(usuario);

			StringBuilder sb = new StringBuilder();
			for (Integer estab : estabelecimentos) {
				sb.append(estab);
				sb.append(",");
			}
			HashMap<String, String> hash = new HashMap<String, String>();
			String ids = sb.toString();
			hash.put(usuario, ids.substring(0, ids.length() - 1));
			retorno.add(hash);
		}

		return retorno;
	}

	/**
	 * Contar as avaliações de um determinado grupo de estabelecimentos
	 * (itemsets).
	 * 
	 * @param itemset - estabelecimentos avaliados.
	 * @param usuarios - usuários que realizaram avaliações.
	 * @param avaliacoes - lista de avaliações para contagem.
	 * @return total de avaliações realizadas sobre os estabelecimentos passados 
	 * por parâmetro.
	 */
	private int contarAvaliacoes(int[] itemset, List<String> usuarios,
			List<HashMap<String, String>> avaliacoes) {
		int countRetorno = 0;
		int countAuxiliar = 0;

		for (String usuario : usuarios) {
			for (int i = 0; i < avaliacoes.size(); i++) {
				countAuxiliar = 0;
				String avaliacao = avaliacoes.get(i).get(usuario);
				if (avaliacao != null) {
					String[] estabelecimentos = avaliacao.split(",");
					for (int j = 0; j < itemset.length; j++) {
						for (int k = 0; k < estabelecimentos.length; k++) {
							if (itemset[j] == Integer.parseInt(estabelecimentos[k])) {
								countAuxiliar++;
							}
						}
					}

					if (countAuxiliar == itemset.length) {
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
	 * @param itemsets - lista atual dos itemsets.
	 * @return lista de itemsets com seus elementos com tamanho n+1.
	 */
	private List<int[]> atualizaItemset(List<int[]> itemsets) {
		Integer tamanhoAtualItemset = itemsets.get(0).length;
		List<int[]> novaLista = new ArrayList<>();

		for (int i = 0; i < itemsets.size(); i++) {

			for (int j = i + 1; j < itemsets.size(); j++) {
				int[] tempI = itemsets.get(i);
				int[] tempJ = itemsets.get(j);
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
				
				Arrays.sort(novoArray);

				if (diferente > 0) {					
					addElemento(novoArray, novaLista);					
				}
			}
		}

		return novaLista;
	}

	/**
	 * Verifica e adiciona determinado itemset a lista.
	 * 
	 * @param candidato - array candidato.
	 * @param listaProcura - lista atual.
	 */
	private void addElemento(int[] candidato, List<int[]> listaProcura) {
		boolean achou = false;
		if (listaProcura.size() > 0) {
			for (int i = 0; i < listaProcura.size(); i++) {
				int[] arrayBase = listaProcura.get(i);
				if (temNoArray(candidato, arrayBase)) {
					achou = true;
					break;
				}
			}
		} else {
			listaProcura.add(candidato);
			return;
		}
		
		if (!achou) {
			listaProcura.add(candidato);
		}
	}

	/**
	 * Verifica e adiciona determinado elemento a lista.
	 * 
	 * @param candidato - elemento candidato.
	 * @param listaProcura - lista atual.
	 */
	private void addElemento(int candidato, List<Integer> listaProcura) {
		boolean achou = false;
		if (listaProcura.size() > 0) {
			for (int i = 0; i < listaProcura.size(); i++) {
				int p = listaProcura.get(i);
				if (p == candidato) {
					achou = true;
					break;
				}
			}

			if (!achou) {
				listaProcura.add(candidato);
			}
		} else {
			listaProcura.add(candidato);
		}
	}

	/**
	 * Verifica e adiciona determinado estabelecimento a lista.
	 * 
	 * @param candidato - estabelecimento candidato.
	 * @param listaProcura - lista atual.
	 */
	private void addElemento(Estabelecimento candidato,
			List<Estabelecimento> listaProcura) {
		boolean achou = false;
		if (listaProcura.size() > 0) {
			for (int i = 0; i < listaProcura.size(); i++) {
				Estabelecimento estabelecimento = listaProcura.get(i);
				if (estabelecimento.equals(candidato)) {
					achou = true;
					break;
				}
			}

			if (!achou) {
				listaProcura.add(candidato);
			}
		} else {
			listaProcura.add(candidato);
		}
	}

	/**
	 * Verifica se determinados elementos já fazem parte de um determinado
	 * array.
	 * 
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
	 * Divide um determinado array de estabelecimentos em grupos menores, sendo
	 * o tamanho mínimo 2.
	 * 
	 * @param itemset - array a dividir.
	 * @return lista dos arrays gerados.
	 */
	private List<int[]> geraItemsetsConfianca(int[] itemset) {
		List<int[]> retorno = new ArrayList<int[]>();
		int controle = itemset.length;
		int[] bit = new int[controle];
		int qtdeItemSets = (int) (Math.pow(2, controle) - 1);

		for (int i = 0; i < controle; i++) {
			bit[i] = 0;
		}

		for (int j = 1; j <= qtdeItemSets; j++) {
			List<Integer> listIds = new ArrayList<>();
			somaBit(bit, controle);

			for (int k = 0; k < controle; k++) {
				if (bit[k] == 1) {
					listIds.add(itemset[k]);
				}
			}

			if (listIds.size() > 1) {
				int[] ids = new int[listIds.size()];
				for (int l = 0; l < listIds.size(); l++) {
					ids[l] = listIds.get(l);
				}
				addElemento(ids, retorno);
			}
		}

		return retorno;
	}

	/**
	 * Função auxiliar utilizada na divisão de um itemset em arrays menores.
	 * 
	 * @see geraItemsetsConfianca
	 * @param arrayBits
	 * @param tamanho
	 */
	private void somaBit(int arrayBits[], int tamanho) {
		for (int i = 0; i < tamanho; i++) {
			if (arrayBits[i] == 0) {
				arrayBits[i] = 1;
				break;
			}
			arrayBits[i] = 0;
		}
	}

	/**
	 * Gera as regras de associação de determinado itemset.
	 * @param itemset - itemset com os elementos para geração das regras.
	 * @return lista das regras.
	 */
	private List<RegraAssociacao> geraRegrasDeAssociacao(int[] itemset) {
		if (itemset.length >= 2) {
			List<RegraAssociacao> regras = new ArrayList<>();
			String[] strItemset = new String[itemset.length];
			for (int i = 0; i < itemset.length; i++) {
				strItemset[i] = itemset[i] + "";
			}

			Set<RegraAssociacao> setResultado = new HashSet<>();
			Set<String> set = new HashSet<>(Arrays.asList(strItemset));
			Set<RegraAssociacao> setRegras = geraRegrasDeAssociacao(set);
			geraRegrasDeAssociacao(set, setRegras, setResultado);

			for (RegraAssociacao regraAssociacao : setResultado) {
				regras.add(regraAssociacao);
			}

			return regras;
		}
		return null;
	}

	/**
	 * Gera as regras de associação de determinado itemset.
	 * @param itemset - itemset com os elementos para geração das regras.
	 * @return set das regras.
	 */
	private Set<RegraAssociacao> geraRegrasDeAssociacao(Set<String> itemset) {
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

	/**
	 * Gera as regras de associação de determinado itemset.
	 * @param itemset - itemset com os elementos para a geração das regras.
	 * @param setRegras
	 * @param setResultado
	 */
	private void geraRegrasDeAssociacao(Set<String> itemset,
			Set<RegraAssociacao> setRegras, Set<RegraAssociacao> setResultado) {
		
		Iterator<RegraAssociacao> it = setRegras.iterator();
		while (it.hasNext()) {
			RegraAssociacao regra = it.next();
			setResultado.add(regra);
		}
		
		int k = itemset.size();
		int m = setRegras.iterator().next().getHashSetEntao().size();
		if (k > m + 1) {
			Set<RegraAssociacao> novasRegras = moveUmElementoPraCondicaoEntao(setRegras);
			Iterator<RegraAssociacao> iterator = novasRegras.iterator();
			while (iterator.hasNext()) {
				RegraAssociacao regra = iterator.next();
				setResultado.add(regra);
			}
			geraRegrasDeAssociacao(itemset, novasRegras, setResultado);
		}
	}

	/**
	 * Utilitário para a geração das regras de associação.
	 * @param setRegras
	 * @return
	 */
	private Set<RegraAssociacao> moveUmElementoPraCondicaoEntao(
			Set<RegraAssociacao> setRegras) {
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