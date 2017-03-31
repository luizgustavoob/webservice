package br.com.paraondeirwebservice.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.paraondeirwebservice.controller.NotificacaoController;

@Component
@EnableScheduling	
public class JobSincronizacao {

	@Autowired
	private NotificacaoController controller;
	
	private static final String TIME_ZONE = "America/Sao_Paulo";

	@Scheduled(cron = "0 0 1 * * *", zone = TIME_ZONE) //Sempre 1 da manhã  
	public void solicitarSincronizacao() throws Exception {
		try {
			controller.notificarSincronizacao();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
