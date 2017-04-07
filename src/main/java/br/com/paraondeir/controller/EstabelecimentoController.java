package br.com.paraondeir.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.paraondeir.model.Estabelecimento;
import br.com.paraondeir.repository.IEstabelecimentoDao;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@RestController
@RequestMapping(value = "/estab")
public class EstabelecimentoController {

	@Autowired
	private IEstabelecimentoDao estabelecimentoDao;

	@RequestMapping(value = "/sincronizaEstab", method = RequestMethod.GET, produces = "application/json")
	public String sincronizarEstabelecimentos() {
		List<Estabelecimento> estabelecimentos = estabelecimentoDao.findAll();
		Gson gson = new Gson();
		JsonElement element = gson.toJsonTree(estabelecimentos, new TypeToken<List<Estabelecimento>>() {}.getType());
		return element.getAsJsonArray().toString();
	}
}
