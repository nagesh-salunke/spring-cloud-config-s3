package com.spring.cloud.config.s3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Entry point of spring boot application.
 *
 * @author Nagesh Salunke
 *
 */
@SpringBootApplication
@EnableConfigServer
public class Application {

  /**
   * main method - starts the tomcat server.
   *
   * @param args arguments to spring application.
   */
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
