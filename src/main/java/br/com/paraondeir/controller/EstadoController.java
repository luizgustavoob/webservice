package br.com.paraondeir.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.paraondeir.model.Estado;
import br.com.paraondeir.repository.IEstadoDao;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@RestController
@RequestMapping(value = "/estado")
public class EstadoController {

	@Autowired
	private IEstadoDao ufDao;
	
	@RequestMapping(value = "/sincronizaEstado", method = RequestMethod.GET, produces = "application/json")
	public String sincronizarEstados() {
		List<Estado> estados = ufDao.findAll();
		Gson gson = new Gson();
		JsonElement element = gson.toJsonTree(estados, new TypeToken<List<Estado>>() {}.getType());
		return element.getAsJsonArray().toString();
	}
}
