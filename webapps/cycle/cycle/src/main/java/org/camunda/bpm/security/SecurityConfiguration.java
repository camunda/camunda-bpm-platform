package org.camunda.bpm.security;


/**
 *
 * @author nico.rehwaldt
 * @author Daniel Meyer
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
