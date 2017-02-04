package br.com.webservice.paraondeir.controller;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.webservice.paraondeir.model.RegistroDeletado;
import br.com.webservice.paraondeir.repository.IRegistroDeletadoDao;

@RestController
@RequestMapping(value = "/deletado")
public class RegistroDeletadoController {
	
	@Autowired
	private IRegistroDeletadoDao dao;
	
	@RequestMapping(value = "/sincronizaDeletado", method = RequestMethod.POST, produces = "application/json")
	public String sincronizaRegistrosDeletados(@RequestBody String json) throws ParseException{
		JSONStringer builder = new JSONStringer();
		try {						
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
			JSONObject objeto = new JSONObject(json);
			Date data = null;
			if (objeto.getString("data") != null) {
				data = sdf.parse(objeto.getString("data"));				
			} else {
				data = Calendar.getInstance().getTime();
			}
			
			Timestamp dataSQL = new Timestamp(data.getTime());					
			List<RegistroDeletado> deletados = dao.findRegistrosDeletadosGreaterThanData(dataSQL);
			
			builder.array();
			for (RegistroDeletado registroDeletado : deletados) {
				builder.object();
				builder.key("nome_tabela").value(registroDeletado.getRegistroDeletadoId().getNomeTabela());
				builder.key("chave_tabela").value(registroDeletado.getRegistroDeletadoId().getIdTabela());
				builder.endObject();
			}
			builder.endArray();
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		return builder.toString();
	}
}
