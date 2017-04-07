package br.com.paraondeirwebservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.paraondeirwebservice.model.Usuario;

public interface IUsuarioDao extends JpaRepository<Usuario, String> {
	
	Usuario findByfcmid(String fcmid);
}
