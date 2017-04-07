package br.com.paraondeirwebservice.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.paraondeirwebservice.repository.IUsuarioDao;
import br.com.paraondeirwebservice.utils.Constantes;

@Controller
public class NotificacaoController {
	
	@Autowired
	private IUsuarioDao dao;
	
	public String notificar(JSONObject json) {
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
			
			OutputStreamWriter wr = new OutputStreamWriter(conexao.getOutputStream());
			wr.write(json.toString());
			wr.flush();
			InputStreamReader isr = new InputStreamReader(conexao.getInputStream());
			BufferedReader br = new BufferedReader(isr);
			retorno = br.readLine();
			while (retorno != null){
				retorno = br.readLine();
			}
			br.close();
			
			return retorno;
		} catch (Exception ex){
			ex.printStackTrace();
			return "erro: " + ex.getMessage();
		}
	}
}
