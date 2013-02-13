package com.camunda.fox.security;

import com.camunda.fox.license.FoxLicenseService;
import com.camunda.fox.license.impl.FoxLicenseServiceImpl;

/**
 *
 * @author nico.rehwaldt
 * @author Daniel Meyer
 */
public class SecurityConfiguration {

  private boolean useJaas = false;
  
  private FoxLicenseService foxLicenseService;
  
  public boolean isUseJaas() {
    return useJaas;
  }

  public void setUseJaas(boolean useJaas) {
    this.useJaas = useJaas;
  }

  public FoxLicenseService getFoxLicenseService() {
    if(foxLicenseService == null) {
      initFoxLicenseService();
    }
    return foxLicenseService;
  }
  
  protected void initFoxLicenseService() {
    foxLicenseService = new FoxLicenseServiceImpl();
  }

  public void setFoxLicenseService(FoxLicenseService foxLicenseService) {
    this.foxLicenseService = foxLicenseService;
  }
}
