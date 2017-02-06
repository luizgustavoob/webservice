package br.com.paraondeirwebservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.paraondeirwebservice.model.Estado;

public interface IEstadoDao extends JpaRepository<Estado, Integer> {

}
