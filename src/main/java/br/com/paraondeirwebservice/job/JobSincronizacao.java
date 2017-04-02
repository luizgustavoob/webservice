package br.com.paraondeirwebservice.job;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.paraondeirwebservice.controller.NotificacaoController;
import br.com.paraondeirwebservice.model.Usuario;
import br.com.paraondeirwebservice.repository.IUsuarioDao;

@Component
@EnableScheduling	
public class JobSincronizacao {

	@Autowired
	private NotificacaoController controller;
	@Autowired
	private IUsuarioDao dao;
	
	private static final String TIME_ZONE = "America/Sao_Paulo";

	@Scheduled(cron = "0 0 1 * * *", zone = TIME_ZONE) //Sempre 1 da manhã  
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
						
			controller.notificar(json);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
