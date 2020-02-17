package org.camunda.bpm.run.property;

import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(CamundaBpmRunProperties.PREFIX)
public class CamundaBpmRunProperties {

  public static final String PREFIX = CamundaBpmProperties.PREFIX + ".run";

  @NestedConfigurationProperty
  private CamundaBpmRunAuthenticationProperties auth = new CamundaBpmRunAuthenticationProperties();

  @NestedConfigurationProperty
  private CamundaBpmRunCorsProperty cors = new CamundaBpmRunCorsProperty();

  public CamundaBpmRunAuthenticationProperties getAuth() {
    return auth;
  }

  public void setAuth(CamundaBpmRunAuthenticationProperties auth) {
    this.auth = auth;
  }

  public CamundaBpmRunCorsProperty getCors() {
    return cors;
  }

  public void setCors(CamundaBpmRunCorsProperty cors) {
    this.cors = cors;
  }

  @Override
  public String toString() {
    return "CamundaRestDistroProperties [auth=" + auth + ", cors=" + cors + "]";
  }
}
