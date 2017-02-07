package br.com.paraondeirwebservice.controller;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.paraondeirwebservice.model.Firebase;
import br.com.paraondeirwebservice.repository.IFirebaseDao;
import br.com.paraondeirwebservice.sinc.SincronizacaoAutomatica;

@RestController
@RequestMapping(value = "/token")
public class FirebaseController {
	
	@Autowired
	private IFirebaseDao dao;
	
	@RequestMapping(value = "/", method = RequestMethod.POST, produces = "application/json")
	public String sincronizarToken(@RequestBody String jsonToken){
		try {
			JSONObject json = new JSONObject(jsonToken);
			String token = json.getString("token");
			Firebase firebase = new Firebase(token);
			dao.save(firebase);
			
			JSONStringer builder = new JSONStringer();
            builder.object();
            builder.key("token").value(token);
            builder.endObject();
			return builder.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}		
	}
	
	@RequestMapping(value = "/notificar", method = RequestMethod.GET)
	public String notificarSincronizacao() {
		try {
			SincronizacaoAutomatica sinc = new SincronizacaoAutomatica();
			sinc.solicitarSincronizacao();
			JSONStringer builder = new JSONStringer();
			builder.object();
			builder.key("success").value("ok");
			builder.endObject();
			return builder.toString();
		} catch (Exception ex){
			ex.printStackTrace();			
			return "error: " + ex.getMessage();
		}
	}
}
