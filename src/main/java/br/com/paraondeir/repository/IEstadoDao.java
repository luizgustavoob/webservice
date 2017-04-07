package br.com.paraondeir.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.paraondeir.model.Estado;

public interface IEstadoDao extends JpaRepository<Estado, Integer> {

}
