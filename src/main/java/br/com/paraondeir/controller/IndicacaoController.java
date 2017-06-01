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
		
		Usuario usuario = new Gson().fromJson(json, new TypeToken<Usuario>() {}.getType());		
		List<Estabelecimento> retorno = new ArrayList<>();

		List<String> usuarios = avaliacaoDao.findUsuariosAvaliacao();
		List<HashMap<String, Integer[]>> avaliacoes = getAvaliacoes(usuarios, false);
		List<HashMap<String, Integer[]>> avaliacoesPositivas = getAvaliacoes(usuarios, true);

		List<Integer[]> itemsets = criaItemsetInicial();
		do {
			List<Integer[]> auxiliar = calculaSuporte(itemsets, usuarios, avaliacoes, avaliacoesPositivas);
			if (auxiliar.size() > 1){
				itemsets.clear();
				itemsets = atualizaItemset(auxiliar);
			} else {
				itemsets = auxiliar;
			}
		} while (itemsets.size() != 1);

		boolean calculaConfianca = usuarioAvaliou(usuario.getUsuario(), usuarios);

		if (calculaConfianca) {
			List<Integer[]> itemsetsConfianca = geraItemsetsConfianca(itemsets.get(0));
			List<RegraAssociacao> regras = new ArrayList<>();
			for (int i = 0; i < itemsetsConfianca.size(); i++) {
				Integer[] itemset = itemsetsConfianca.get(i);
				List<RegraAssociacao> regrasItemset = geraRegrasDeAssociacao(itemset);
				for (RegraAssociacao regra : regrasItemset) {
					regra.dadosHashToList();
					double confianca = calculaConfianca(regra, usuarios, avaliacoesPositivas);
					if (confianca >= Constantes.CONFIANCA_MINIMA) {
						regras.add(regra);
					}
				}
			}

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
			for (Integer[] itemset : itemsets) {
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

	private boolean usuarioGostouDaCondicaoSe(String usuario, 
			List<Integer> paramSe,
			List<HashMap<String, Integer[]>> avaliacoesPositivas) {
		
		int count = 0;
		for (int i = 0; i < avaliacoesPositivas.size(); i++) {
			Integer[] estabelecimentosAvaliados = avaliacoesPositivas.get(i).get(usuario);
			if (estabelecimentosAvaliados != null) {
				for (int j = 0; j < paramSe.size(); j++) {
					for (int k = 0; k < estabelecimentosAvaliados.length; k++) {
						if (paramSe.get(j) == estabelecimentosAvaliados[k]) {
							count++;
						}
					}
				}
				break;
			}
		}

		return count == paramSe.size();
	}

	private double calculaConfianca(RegraAssociacao regra,
			List<String> usuarios,
			List<HashMap<String, Integer[]>> avaliacoesPositivas) {

		List<Integer> listItemset = new ArrayList<>();
		List<Integer> listParamSe = new ArrayList<>();
		for (int i = 0; i < regra.getListSe().size(); i++) {
			addElemento(regra.getListSe().get(i), listItemset);
			addElemento(regra.getListSe().get(i), listParamSe);
		}
		for (int i = 0; i < regra.getListEntao().size(); i++) {
			addElemento(regra.getListEntao().get(i), listItemset);
		}

		Integer[] itemset = listItemset.toArray(new Integer[listItemset.size()]);
		Integer[] paramSe = listParamSe.toArray(new Integer[listParamSe.size()]);

		double total = contarAvaliacoes(itemset, usuarios, avaliacoesPositivas);
		double totalSe = contarAvaliacoes(paramSe, usuarios, avaliacoesPositivas);
		return total / totalSe;
	}

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

	private List<Integer[]> criaItemsetInicial() {
		List<Integer[]> retorno = new ArrayList<>();
		for (Estabelecimento estab : estabDao.findAll()) {
			Integer[] itemset = { estab.getIdEstabelecimento() };
			retorno.add(itemset);
		}
		return retorno;
	}

	private List<Integer[]> calculaSuporte(List<Integer[]> itemsets,
			List<String> usuarios, 
			List<HashMap<String, Integer[]>> avaliacoes,
			List<HashMap<String, Integer[]>> avaliacoesPositivas) {

		List<Integer[]> retorno = new ArrayList<>();

		for (int i = 0; i < itemsets.size(); i++) {
			Integer[] itemset = itemsets.get(i);
			double total = contarAvaliacoes(itemset, usuarios, avaliacoes);
			double totalSim = contarAvaliacoes(itemset, usuarios, avaliacoesPositivas);
			double resultado = totalSim / total;

			if (resultado >= Constantes.SUPORTE_MINIMO) {
				addElemento(itemset, retorno);
			}
		}

		return retorno;
	}

	private List<HashMap<String, Integer[]>> getAvaliacoes(List<String> usuarios,
			boolean filtraParamGostou) {
		List<HashMap<String, Integer[]>> retorno = new ArrayList<>();

		for (String usuario : usuarios) {
			List<Integer> estabelecimentos = filtraParamGostou ? 
					avaliacaoDao.findEstabsByUsuarioAndGostou(usuario, "S") : 
					avaliacaoDao.findEstabsByUsuario(usuario);
			
			HashMap<String, Integer[]> hash = new HashMap<String, Integer[]>();
			hash.put(usuario, estabelecimentos.toArray(new Integer[estabelecimentos.size()]));
			retorno.add(hash);
		}

		return retorno;
	}

	private int contarAvaliacoes(Integer[] itemset, 
			List<String> usuarios,
			List<HashMap<String, Integer[]>> avaliacoes) {
		int countRetorno = 0;
		int countAuxiliar = 0;

		for (String usuario : usuarios) {
			for (int i = 0; i < avaliacoes.size(); i++) {
				countAuxiliar = 0;
				Integer[] estabelecimentosAvaliados = avaliacoes.get(i).get(usuario);
				if (estabelecimentosAvaliados != null) {
					for (int j = 0; j < itemset.length; j++) {
						for (int k = 0; k < estabelecimentosAvaliados.length; k++) {
							if (itemset[j] == estabelecimentosAvaliados[k]) {
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

	private List<Integer[]> atualizaItemset(List<Integer[]> itemsets) {
		Integer tamanhoAtual = itemsets.get(0).length;
		List<Integer[]> novaLista = new ArrayList<>();

		for (int i = 0; i < itemsets.size(); i++) {
			for (int j = i + 1; j < itemsets.size(); j++) {
				Integer[] itemsetLoopI = itemsets.get(i);
				Integer[] itemsetLoopJ = itemsets.get(j);
				Integer[] novoItemset = new Integer[tamanhoAtual + 1];

				for (int k = 0; k < novoItemset.length - 1; k++) {
					novoItemset[k] = itemsetLoopI[k];
				}

				int diferente = 0;
				for (int l = 0; l < itemsetLoopJ.length; l++) {
					boolean encontrou = false;
					for (int m = 0; m < itemsetLoopI.length; m++) {
						if (itemsetLoopI[m] == itemsetLoopJ[l]) {
							encontrou = true;
							break;
						}
					}

					if (!encontrou) {
						diferente++;
						novoItemset[novoItemset.length - 1] = itemsetLoopJ[l];
					}
				}
				
				Arrays.sort(novoItemset);

				if (diferente > 0) {					
					addElemento(novoItemset, novaLista);					
				}
			}
		}

		return novaLista;
	}

	private void addElemento(Integer[] candidato, List<Integer[]> listaProcura) {
		boolean achou = false;
		if (listaProcura.size() > 0) {
			for (int i = 0; i < listaProcura.size(); i++) {
				Integer[] arrayBase = listaProcura.get(i);
				if (temNoArray(candidato, arrayBase)) {
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

	private void addElemento(Estabelecimento candidato, List<Estabelecimento> listaProcura) {
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

	private boolean temNoArray(Integer[] elementos, Integer[] arrayProcura) {
		int contador = 0;
		for (int i = 0; i < arrayProcura.length; i++) {
			for (int k = 0; k < elementos.length; k++) {
				if (arrayProcura[i] == elementos[k]) {
					contador++;
					break;
				}
			}
		}

		return contador == elementos.length;
	}

	private List<Integer[]> geraItemsetsConfianca(Integer[] itemset) {
		List<Integer[]> retorno = new ArrayList<Integer[]>();
		int controle = itemset.length;
		int[] arrayBit = new int[controle];
		int qtdeItemsets = (int) (Math.pow(2, controle) - 1);

		for (int i = 0; i < controle; i++) {
			arrayBit[i] = 0;
		}

		for (int j = 1; j <= qtdeItemsets; j++) {
			List<Integer> listaIds = new ArrayList<>();
			atualizaBit(arrayBit, controle);

			for (int k = 0; k < controle; k++) {
				if (arrayBit[k] == 1) {
					listaIds.add(itemset[k]);
				}
			}

			if (listaIds.size() > 1) {
				Integer[] ids = listaIds.toArray(new Integer[listaIds.size()]);
				addElemento(ids, retorno);
			}
		}

		return retorno;
	}

	private void atualizaBit(int[] arrayBits, int tamanho) {
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
	private List<RegraAssociacao> geraRegrasDeAssociacao(Integer[] itemset) {
		if (itemset.length >= 2) {
			List<RegraAssociacao> regras = new ArrayList<>();
			Set<RegraAssociacao> setResultado = new HashSet<>();
			Set<Integer> set = new HashSet<>(Arrays.asList(itemset));
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
	private Set<RegraAssociacao> geraRegrasDeAssociacao(Set<Integer> itemset) {
		Set<RegraAssociacao> retorno = new HashSet<>(itemset.size());
		Set<Integer> se = new HashSet<>(itemset);
		Set<Integer> entao = new HashSet<>(1);
		for (Integer elemento : itemset) {
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
	private void geraRegrasDeAssociacao(Set<Integer> itemset,
			Set<RegraAssociacao> setRegras, 
			Set<RegraAssociacao> setResultado) {
		
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
		Set<Integer> se = new HashSet<>();
		Set<Integer> entao = new HashSet<>();

		for (RegraAssociacao regra : setRegras) {
			se.clear();
			entao.clear();
			se.addAll(regra.getHashSetSe());
			entao.addAll(regra.getHashSetEntao());

			for (Integer elemento : regra.getHashSetSe()) {
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