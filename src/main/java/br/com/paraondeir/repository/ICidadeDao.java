package br.com.paraondeirwebservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.paraondeirwebservice.model.Cidade;

public interface ICidadeDao extends JpaRepository<Cidade, Integer>{

}
