package br.com.paraondeir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.paraondeir.model.Usuario;

@Repository
public interface IUsuarioDao extends JpaRepository<Usuario, String> {
	
	Usuario findByfcmid(String fcmid);
}
