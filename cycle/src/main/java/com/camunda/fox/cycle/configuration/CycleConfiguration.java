package com.camunda.fox.cycle.configuration;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.repository.UserRepository;

/**
 * Application configuration component
 * 
 * @author nico.rehwaldt
 * @author Daniel Meyer
 */
@Component
public class CycleConfiguration {

  @Inject
  private UserRepository userRepository;
  
  private boolean useJaas = false;
  
  private String mailSessionUrl;

  
  public boolean isUseJaas() {
    return useJaas;
  }

  public void setUseJaas(boolean useJaas) {
    this.useJaas = useJaas;
  }

  /**
   * Returns true if users may be configured
   * @return 
   */
  public boolean isConfigured() {
    return useJaas || userRepository.countAll() > 0;
  }

  public String getMailSessionName() {
    return mailSessionUrl;    
  }
  
  public void setMailSessionName(String mailSessionUrl) {
    this.mailSessionUrl = mailSessionUrl;
  }
}
