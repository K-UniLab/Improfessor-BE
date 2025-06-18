package org.unilab.improfessorbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ImprofessorBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImprofessorBeApplication.class, args);
	}

}
