package org.camunda.bpm.rest.distro.property;

import java.util.Arrays;
import java.util.List;

public class CamundaAuthenticationProperty {

  public static final String PREFIX = CamundaRestDistroProperties.PREFIX + ".auth";
  public static final String DEFAULT_AUTH = "basic";
  public static final List<String> AUTH_METHODS = Arrays.asList(DEFAULT_AUTH);

  boolean enabled = true;
  String authentication = DEFAULT_AUTH;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getAuthentication() {
    return authentication;
  }

  public void setAuthentication(String authentication) {
    if (authentication != null && !AUTH_METHODS.contains(authentication)) {
      throw new RuntimeException("Please provide a valid authentication method. The available ones are: " + AUTH_METHODS.toString());
    }
    this.authentication = authentication;
  }

  @Override
  public String toString() {
    return "CamundaAuthenticationProperty [enabled=" + enabled + ", authentication=" + authentication + "]";
  }
}
