package br.com.paraondeirwebservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.paraondeirwebservice.model.Avaliacao;
import br.com.paraondeirwebservice.repository.IAvaliacaoDao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@RestController
@RequestMapping(value = "/avaliacao")
public class AvaliacaoController {

	@Autowired
	private IAvaliacaoDao avaliacaoDao;

	/**
	 * Salva as avaliações enviadas e as retorna para o app.
	 * 
	 * @return json avaliações.
	 */
	@RequestMapping(value = "/sincronizaAvaliacao", method = RequestMethod.POST, produces = "application/json")
	public String sincronizarAvaliacoes(@RequestBody String jsonAvaliacao) {
		String response = "";
		List<Avaliacao> lista = new Gson().fromJson(jsonAvaliacao, new TypeToken<List<Avaliacao>>(){}.getType());
		try {
			for (Avaliacao avaliacao : lista) {
				int idTemp;
				try {
					idTemp = avaliacaoDao.findIdAvaliacaoByUsuarioAndIdEstabelecimento(
							avaliacao.getUsuario(), avaliacao.getEstabelecimento().getIdEstabelecimento());
				} catch (Exception ex){
					idTemp = 0;
				}
				
				if (idTemp > 0) {
					avaliacao.setAvaliacaoid(idTemp);					
				}
				
				avaliacaoDao.save(avaliacao);
			}
			response = jsonAvaliacao;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return response;
	}
}
