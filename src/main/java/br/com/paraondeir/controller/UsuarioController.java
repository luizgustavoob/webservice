package br.com.paraondeirwebservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.paraondeirwebservice.model.Usuario;
import br.com.paraondeirwebservice.repository.IUsuarioDao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@RestController
@RequestMapping(value = "/usuario")
public class UsuarioController {
	
	@Autowired
	private IUsuarioDao dao;
	
	@RequestMapping(value = "/", method = RequestMethod.POST, produces = "application/json")
	public String sincronizarUsuario(@RequestBody String json){
		try {
			Usuario usuario = new Gson().fromJson(json, new TypeToken<Usuario>(){}.getType());
			Usuario userTemp = dao.findOne(usuario.getUsuario());
			
			if (userTemp == null){
				dao.save(usuario);
			} else if (usuario.getFcmid() != "" && usuario.getFcmid() != null && 
					!usuario.getFcmid().equals(userTemp.getFcmid())) {
				dao.save(usuario);
			}
			
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}		
	}
}
