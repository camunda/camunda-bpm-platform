package org.camunda.camunda.rest.distro;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableProcessApplication
public class CamundaRestDistro {
  public static void main(String... args) {
    SpringApplication.run(CamundaRestDistro.class, args);
  }
}
