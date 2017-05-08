package br.com.paraondeir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.paraondeir.model.Estado;

@Repository
public interface IEstadoDao extends JpaRepository<Estado, Integer> {

}
