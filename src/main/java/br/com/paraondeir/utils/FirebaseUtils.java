package br.com.paraondeir.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class FirebaseUtils {
	
	public static String notificar(JSONObject json) {
		String retorno = "";
		try {
			URL url = new URL(Constantes.LINK_FIREBASE);
			HttpURLConnection conexao = (HttpURLConnection) url.openConnection();			
			conexao.setUseCaches(false);
			conexao.setDoInput(true);
			conexao.setDoOutput(true);
			conexao.setReadTimeout(Constantes.TIMEOUT);
			conexao.setConnectTimeout(Constantes.TIMEOUT);			
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
		} catch (Exception ex){
			ex.printStackTrace();
			retorno = "erro: " + ex.getMessage();
		}
		return retorno;
	}
}
