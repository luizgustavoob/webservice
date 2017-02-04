package br.com.webservice.paraondeir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.webservice.paraondeir.model.Estado;

public interface IEstadoDao extends JpaRepository<Estado, Integer> {

}
