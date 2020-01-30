package org.camunda.bpm.rest.distro.property;

import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(CamundaRestDistroProperties.PREFIX)
public class CamundaRestDistroProperties {

  public static final String PREFIX = CamundaBpmProperties.PREFIX + ".rest-distro";
  
  @NestedConfigurationProperty
  private CamundaAuthenticationProperty auth = new CamundaAuthenticationProperty();

  @NestedConfigurationProperty
  private CamundaCorsProperty cors = new CamundaCorsProperty();

  public CamundaAuthenticationProperty getAuth() {
    return auth;
  }

  public void setAuth(CamundaAuthenticationProperty auth) {
    this.auth = auth;
  }

  public CamundaCorsProperty getCors() {
    return cors;
  }

  public void setCors(CamundaCorsProperty cors) {
    this.cors = cors;
  }

  @Override
  public String toString() {
    return "CamundaRestDistroProperties [auth=" + auth + ", cors=" + cors + "]";
  }
}
