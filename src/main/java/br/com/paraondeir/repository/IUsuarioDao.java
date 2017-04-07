package br.com.paraondeir.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.paraondeir.model.Usuario;

public interface IUsuarioDao extends JpaRepository<Usuario, String> {
	
	Usuario findByfcmid(String fcmid);
}
