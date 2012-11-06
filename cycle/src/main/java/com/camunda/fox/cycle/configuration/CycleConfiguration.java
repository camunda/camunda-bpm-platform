package com.camunda.fox.cycle.configuration;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.repository.UserRepository;
import com.camunda.fox.security.SecurityConfiguration;

/**
 * Application configuration component
 * 
 * @author nico.rehwaldt
 * @author Daniel Meyer
 */
@Component
public class CycleConfiguration extends SecurityConfiguration {

  @Inject
  private UserRepository userRepository;
  
  private String mailSessionName;
  
  /**
   * Returns true if users may be configured
   * @return 
   */
  public boolean isConfigured() {
    return isUseJaas() || userRepository.countAll() > 0;
  }

  /**
   * The mail session name is used for looking up a mail session in JNDI
   * 
   * @return the JNDI name for looking up a mail session
   */
  public String getMailSessionName() {
    return mailSessionName;
  }
  
  public void setMailSessionName(String mailSessionUrl) {
    this.mailSessionName = mailSessionUrl;
  }
}
