package br.com.webservice.paraondeir.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.webservice.paraondeir.model.Endereco;
import br.com.webservice.paraondeir.repository.IEnderecoDao;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@RestController
@RequestMapping(value = "/endereco")
public class EnderecoController {

	@Autowired
	private IEnderecoDao enderecoDao;

	@RequestMapping(value = "/sincronizaEndereco", method = RequestMethod.GET, produces = "application/json")
	public String sincronizarEnderecos() {
		List<Endereco> enderecos = enderecoDao.findAll();
		Gson gson = new Gson();
		JsonElement element = gson.toJsonTree(enderecos, new TypeToken<List<Endereco>>() {}.getType());
		return element.getAsJsonArray().toString();
	}
}
