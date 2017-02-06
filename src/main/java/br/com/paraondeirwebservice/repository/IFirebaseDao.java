package br.com.paraondeirwebservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.paraondeirwebservice.model.Firebase;

public interface IFirebaseDao extends JpaRepository<Firebase, String> {

}
