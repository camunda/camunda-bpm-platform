package org.camunda.bpm.rest.distro;

import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(CamundaRestDistroProperties.PREFIX)
public class CamundaRestDistroProperties {

  public static final String PREFIX = CamundaBpmProperties.PREFIX + ".rest-distro";
  public static final String DEFAULT_AUTH = "basic";
  public static final List<String> AUTH_METHODS = Arrays.asList(DEFAULT_AUTH);

  public static final String[] DEFAULT_ORIGINS = {"*"};

  String authentication = DEFAULT_AUTH;

  public String getAuthentication() {
    return authentication;
  }

  public void setAuthentication(String authentication) {

    if (authentication != null && !AUTH_METHODS.contains(authentication)) {
      throw new RuntimeException("Please provide a coorect authentication method. The available " +
                                     "ones are: " + AUTH_METHODS.toString());
    }

    this.authentication = authentication;
  }

  @Override
  public String toString() {
    return "CamundaRestDistroProperties{" +
        "authentication='" + authentication + '\'' +
        '}';
  }
}
