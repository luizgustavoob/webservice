package br.com.paraondeirwebservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.paraondeirwebservice.model.Cidade;
import br.com.paraondeirwebservice.repository.ICidadeDao;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@RestController
@RequestMapping(value = "/cidade")
public class CidadeController {

	@Autowired
	private ICidadeDao cidadeDao;

	@RequestMapping(value = "/sincronizaCidade", method = RequestMethod.GET, produces = "application/json")
	public String sincronizarCidades() {
		List<Cidade> cidades = cidadeDao.findAll();
		Gson gson = new Gson();
		JsonElement element = gson.toJsonTree(cidades, new TypeToken<List<Cidade>>() {}.getType());
		return element.getAsJsonArray().toString();
	}
}
