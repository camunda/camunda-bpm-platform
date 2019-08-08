package org.camunda.camunda.rest.distro.rest;

import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CamundaRestDistro {
  public static void main(String... args) {
    SpringApplication.run(CamundaRestDistro.class, args);
  }
}
