package br.com.paraondeir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class ParaOndeIrApplication extends SpringBootServletInitializer {
	
	public static void main(String[] args) {
		SpringApplication.run(ParaOndeIrApplication.class, args);
	}
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		// Indica quais as classes são responsáveis pela configuração do projeto.
		// É passado como parâmetro a própria classe já que, devido a anotação @SpringBootApplication, 
		// definimos que todas as classes, a partir do pacote onde esta está declarada, devem ser scaneadas.
		return application.sources(ParaOndeIrApplication.class);
		
	}
}