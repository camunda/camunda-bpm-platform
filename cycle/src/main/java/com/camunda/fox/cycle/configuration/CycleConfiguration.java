package com.camunda.fox.cycle.configuration;

import org.springframework.stereotype.Component;

/**
 * Application configuration component
 * 
 * @author nico.rehwaldt
 */
@Component
public class CycleConfiguration {

  private boolean useUserManagement = false;

  public boolean isUseUserManagement() {
    return useUserManagement;
  }

  public void setUseUserManagement(boolean useUserManagement) {
    this.useUserManagement = useUserManagement;
  }
}
