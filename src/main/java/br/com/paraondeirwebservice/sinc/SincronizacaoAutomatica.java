package br.com.paraondeirwebservice.sinc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.paraondeirwebservice.controller.FirebaseController;

@Component
@EnableScheduling	
public class SincronizacaoAutomatica {

	@Autowired
	private FirebaseController controller;
	
	private static final String TIME_ZONE = "America/Sao_Paulo";

	@Scheduled(cron = "0 */5 * * * *", zone = TIME_ZONE) //Sempre 1 da manhã 0 0 1 * * * 
	public void solicitarSincronizacao() throws Exception {
		try {
			controller.notificarSincronizacao();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
