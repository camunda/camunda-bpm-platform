package org.camunda.bpm.run.property;

public class CamundaBpmRunCorsProperty {

  public static final String PREFIX = CamundaBpmRunProperties.PREFIX + ".cors";
  public static final String DEFAULT_ORIGINS = "*";

  boolean enabled;
  String allowedOrigins;

  public boolean isEnabled() {
    return this.enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getAllowedOrigins() {
    if(enabled) {
      return allowedOrigins == null ? DEFAULT_ORIGINS : allowedOrigins;
    }
    return null;
  }

  public void setAllowedOrigins(String allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }

  @Override
  public String toString() {
    return "CamundaBpmRunCorsProperty [enabled=" + enabled + ", allowedOrigins=" + allowedOrigins + "]";
  }
}