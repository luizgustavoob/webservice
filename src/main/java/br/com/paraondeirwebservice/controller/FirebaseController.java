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

@RestController
@RequestMapping(value = "/token")
public class FirebaseController {
	
	@Autowired
	private IFirebaseDao dao;
	
	@RequestMapping(value = "/", method = RequestMethod.POST, produces = "application/json")
	public String sincronizarToken(@RequestBody String jsonToken){
		String response = "";
		JSONObject json;
		try {
			json = new JSONObject(jsonToken);
			String token = json.getString("token");
			Firebase firebase = new Firebase(token);
			dao.save(firebase);
			
			JSONStringer builder = new JSONStringer();
            builder.object();
            builder.key("token").value(token);
            builder.endObject();
			response = builder.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			response = "";
		}		
		return response;
	}
}
