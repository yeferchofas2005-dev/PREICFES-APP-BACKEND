package com.edu.preicfesGratuito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion backend de PreICFES gratuito.
 *
 * <p>Spring Boot usa esta clase para iniciar el contexto de aplicacion,
 * detectar componentes en el paquete base y levantar el servidor web.</p>
 */
@SpringBootApplication
public class PreicfesGratuitoBackendApplication {

	/**
	 * Inicia la aplicacion Spring Boot.
	 *
	 * @param args argumentos recibidos desde la linea de comandos
	 */
	public static void main(String[] args) {
		SpringApplication.run(PreicfesGratuitoBackendApplication.class, args);
	}

}
