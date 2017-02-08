package br.com.paraondeirwebservice.controller;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
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
import br.com.paraondeirwebservice.utils.Constantes;

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
			String jsonEnvio = "";
			URL url = new URL(Constantes.LINK_FIREBASE);
			HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
			conexao.setReadTimeout(30000);
			conexao.setConnectTimeout(30000);
			conexao.setRequestProperty("Authorization", Constantes.KEY_FIREBASE);
			conexao.setRequestProperty("Content-Type", "application/json");
			conexao.setRequestProperty("Method", "POST");		
			conexao.setDoOutput(true);
			
			JSONArray array = new JSONArray();
			List<Firebase> tokens = dao.findAll();
			for (int i = 0; i < tokens.size(); i++) {
				array.put(tokens.get(i).getToken());
			}			
			
			JSONStringer builder = new JSONStringer();
			builder.object();
			builder.key("registration_ids").value(array.toString());
			builder.key("data").value(
					new JSONStringer().object().key("sincronizar").value("sincronizar").endObject().toString());
			builder.endObject();
			
			jsonEnvio = builder.toString();
			if (jsonEnvio != "") {
				OutputStream os = conexao.getOutputStream();
				os.write(jsonEnvio.getBytes("UTF-8"));
				os.close();
				conexao.connect();		
				if (conexao.getResponseCode() == HttpURLConnection.HTTP_OK) {
					InputStream stream = conexao.getInputStream();
					if (stream != null){
						ObjectInputStream ois = new ObjectInputStream(stream);
						Object obj = ois.readObject();						
					}
				}
			}
		} catch (Exception ex){
			ex.printStackTrace();			
			return "error: " + ex.getMessage();
		}
		return "";
	}
}
