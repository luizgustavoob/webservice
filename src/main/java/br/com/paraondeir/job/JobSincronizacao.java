package br.com.paraondeir.job;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.paraondeir.model.Usuario;
import br.com.paraondeir.repository.IUsuarioDao;
import br.com.paraondeir.utils.NotificacaoUtils;

@Component
@EnableScheduling	
public class JobSincronizacao {

	@Autowired
	private IUsuarioDao dao;
	
	private static final String TIME_ZONE = "America/Sao_Paulo";

	@Scheduled(cron = "0 0 21 * * *", zone = TIME_ZONE) //Sempre 1 da manhã  
	public void notificarSincronizacao() throws Exception {
		try {
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
						
			NotificacaoUtils.notificar(json);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
