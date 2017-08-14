package org.camunda.bpm.spring.boot.starter.configuration.impl;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.property.AuthorizationProperty;
import org.camunda.bpm.spring.boot.starter.configuration.CamundaAuthorizationConfiguration;

public class DefaultAuthorizationConfiguration extends AbstractCamundaConfiguration implements CamundaAuthorizationConfiguration {

  @Override
  public void preInit(final SpringProcessEngineConfiguration configuration) {
    final AuthorizationProperty authorization = camundaBpmProperties.getAuthorization();
    configuration.setAuthorizationEnabled(authorization.isEnabled());
    configuration.setAuthorizationEnabledForCustomCode(authorization.isEnabledForCustomCode());
    configuration.setAuthorizationCheckRevokes(authorization.getAuthorizationCheckRevokes());

    configuration.setTenantCheckEnabled(authorization.isTenantCheckEnabled());
  }

}
