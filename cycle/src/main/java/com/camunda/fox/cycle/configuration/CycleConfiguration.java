package com.camunda.fox.cycle.configuration;

import javax.inject.Inject;
import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.repository.UserRepository;

/**
 * Application configuration component
 * 
 * @author nico.rehwaldt
 */
@Component
public class CycleConfiguration {

  private boolean useUserManagement = false;

  @Inject
  private UserRepository userRepository;
  
  public boolean isUseUserManagement() {
    return useUserManagement;
  }

  public void setUseUserManagement(boolean useUserManagement) {
    this.useUserManagement = useUserManagement;
  }

  /**
   * Returns true if users may be configured
   * @return 
   */
  public boolean isNotConfigured() {
    return userRepository.countAll() == 0 && useUserManagement;
  }
}
