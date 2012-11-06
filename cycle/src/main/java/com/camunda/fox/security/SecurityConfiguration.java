package com.camunda.fox.security;

/**
 *
 * @author nico.rehwaldt
 */
public class SecurityConfiguration {

  private boolean useJaas = false;

  
  public boolean isUseJaas() {
    return useJaas;
  }

  public void setUseJaas(boolean useJaas) {
    this.useJaas = useJaas;
  }
}
