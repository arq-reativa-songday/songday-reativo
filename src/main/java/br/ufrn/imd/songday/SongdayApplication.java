package br.ufrn.imd.songday;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
public class SongdayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SongdayApplication.class, args);
	}

}
