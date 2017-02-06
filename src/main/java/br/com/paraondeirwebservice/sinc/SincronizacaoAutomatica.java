package br.com.paraondeirwebservice.sinc;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONStringer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.paraondeirwebservice.model.Firebase;
import br.com.paraondeirwebservice.repository.IFirebaseDao;
import br.com.paraondeirwebservice.utils.Constantes;

@Component
@EnableScheduling	
public class SincronizacaoAutomatica {

	private IFirebaseDao dao;
	
	@Autowired
	public void setDao(IFirebaseDao dao) {
		this.dao = dao;
	}
	
	private static final String TIME_ZONE = "America/Sao_Paulo";

	@Scheduled(cron = "0 0 1 * * *", zone = TIME_ZONE) //Sempre 1 da manhã 
	public void sincronizar() {
		String jsonEnvio = "";
		try {
			URL url = new URL(Constantes.LINK_FIREBASE);
			HttpURLConnection conexao = (HttpURLConnection) url
					.openConnection();
			conexao.setReadTimeout(15000);
			conexao.setConnectTimeout(15000);
			conexao.setRequestProperty("Authorization", Constantes.KEY_FIREBASE);
			conexao.setRequestProperty("Content-Type", "application/json");
			conexao.setRequestProperty("Method", "POST");		
			conexao.setDoOutput(true);

			jsonEnvio = prepararJSONEnvio();
			if (jsonEnvio != "") {
				OutputStream os = conexao.getOutputStream();
				os.write(jsonEnvio.getBytes("UTF-8"));
				os.close();
				conexao.connect();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	private String prepararJSONEnvio() {
		JSONStringer builder = new JSONStringer();
		try {
			JSONArray array = new JSONArray();
			List<Firebase> tokens = dao.findAll();
			for (int i = 0; i < tokens.size(); i++) {
				array.put(tokens.get(i).getToken());
			}			
			
			builder.object();
			builder.key("registration_id").value(array.toString());
			builder.key("data").value("sincronizar");
			builder.endObject();
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
		return builder.toString();
	}

	
}
