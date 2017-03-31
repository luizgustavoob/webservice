package br.com.paraondeirwebservice.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.paraondeirwebservice.model.Usuario;
import br.com.paraondeirwebservice.repository.IUsuarioDao;
import br.com.paraondeirwebservice.utils.Constantes;

@RestController
@RequestMapping(value = "/notificar")
public class NotificacaoController {
	
	@Autowired
	private IUsuarioDao dao;
	
	@RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
	public String notificarSincronizacao() {
		String retorno = "";
		try {
			URL url = new URL(Constantes.LINK_FIREBASE);
			HttpURLConnection conexao = (HttpURLConnection) url.openConnection();			
			conexao.setUseCaches(false);
			conexao.setDoInput(true);
			conexao.setDoOutput(true);
			conexao.setReadTimeout(30000);
			conexao.setConnectTimeout(30000);			
			conexao.setRequestMethod("POST");
			conexao.setRequestProperty("Authorization", Constantes.KEY_FIREBASE);
			conexao.setRequestProperty("Content-Type", "application/json");					
			
			JSONArray array = new JSONArray();
			List<Usuario> usuarios = dao.findAll();
			for (int i = 0; i < usuarios.size(); i++) {
				array.put(usuarios.get(i).getFcmid());
			}			
			
			JSONObject data = new JSONObject();
			data.put("sincronizar", "sincronizar");
			JSONObject json = new JSONObject();
			json.put("registration_ids", array);
			json.put("data", data);
			
			String jsonEnvio = json.toString();
			if (jsonEnvio != "") {
				OutputStreamWriter wr = new OutputStreamWriter(conexao.getOutputStream());
				wr.write(jsonEnvio);
				wr.flush();
				InputStreamReader isr = new InputStreamReader(conexao.getInputStream());
				BufferedReader br = new BufferedReader(isr);
				retorno = br.readLine();
				while (retorno != null){
					retorno = br.readLine();
				}
				br.close();
				return retorno;
			}
		} catch (Exception ex){
			ex.printStackTrace();
			return "erro: " + ex.getMessage();
		}
		return retorno;
	}

}
