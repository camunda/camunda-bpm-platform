package org.camunda.bpm.spring.boot.starter.property;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

public class AuthorizationProperty {

  /**
   * Enables authorization.
   */
  private boolean enabled = Defaults.INSTANCE.isAuthorizationEnabled();

  /**
   * Enables authorization for custom code.
   */
  private boolean enabledForCustomCode = Defaults.INSTANCE.isAuthorizationEnabledForCustomCode();

  private String authorizationCheckRevokes = Defaults.INSTANCE.getAuthorizationCheckRevokes();

  /**
   * If the value of this flag is set <code>true</code> then the process engine
   * performs tenant checks to ensure that an authenticated user can only access
   * data that belongs to one of his tenants.
   */
  private boolean tenantCheckEnabled = true;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEnabledForCustomCode() {
    return enabledForCustomCode;
  }

  public void setEnabledForCustomCode(boolean enabledForCustomCode) {
    this.enabledForCustomCode = enabledForCustomCode;
  }

  public String getAuthorizationCheckRevokes() {
    return authorizationCheckRevokes;
  }

  public void setAuthorizationCheckRevokes(String authorizationCheckRevokes) {
    this.authorizationCheckRevokes = authorizationCheckRevokes;
  }

  public boolean isTenantCheckEnabled() {
    return tenantCheckEnabled;
  }

  public void setTenantCheckEnabled(boolean tenantCheckEnabled) {
    this.tenantCheckEnabled = tenantCheckEnabled;
  }

  @Override
  public String toString() {
    return joinOn(this.getClass())
      .add("enabled=" + enabled)
      .add("enabledForCustomCode=" + enabledForCustomCode)
      .add("authorizationCheckRevokes=" + authorizationCheckRevokes)
      .add("tenantCheckEnabled=" + tenantCheckEnabled)
      .toString();
  }

}
