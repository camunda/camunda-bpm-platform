package com.camunda.fox.cycle.configuration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.entity.User;
import com.camunda.fox.cycle.repository.UserRepository;

/**
 * Application configuration component
 * 
 * @author nico.rehwaldt
 */
@Component
public class CycleConfiguration {

  @Inject
  private UserRepository userRepository;
  
  private boolean useJaas = false;

  public boolean isUseJaas() {
    return useJaas;
  }

  public void setUseJaas(boolean useJaas) {
    this.useJaas = useJaas;
  }
  
  @PostConstruct
  public void initInitialUser() {
    if (userRepository.findAll().isEmpty()) {
      User newUser = new User();
      newUser.setName("admin");
      newUser.setPassword("admin");
      newUser.setEmail("admin@camunda.com");
      newUser.setAdmin(true);
      
      userRepository.saveAndFlush(newUser);
    }
  }
}
