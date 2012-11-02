package com.camunda.fox.cycle.aspect;

import java.security.Principal;

import javax.inject.Inject;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorLoginMode;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.entity.ConnectorCredentials;
import com.camunda.fox.cycle.exception.CycleException;
import com.camunda.fox.cycle.exception.CycleMissingCredentialsException;
import com.camunda.fox.cycle.repository.ConnectorCredentialsRepository;
import com.camunda.fox.cycle.security.PrincipalHolder;

@Component
@Aspect
public class LoginAspect {

  @Inject
  private ConnectorCredentialsRepository connectorCredentialsRepository;
  
  @Before("@annotation(com.camunda.fox.cycle.connector.Secured)")
  private void aroundSecured(JoinPoint jp) throws Throwable {
    if (jp.getTarget() instanceof Connector) {
      Connector con = (Connector) jp.getTarget();
      if (con.needsLogin()) {
        ConnectorConfiguration config = con.getConfiguration();
        con.init(config);
        ConnectorLoginMode loginMode = config.getLoginMode();
        if (loginMode != null && loginMode.equals(ConnectorLoginMode.GLOBAL)) {
          con.login(config.getGlobalUser(), config.getGlobalPassword());
          return;
        }
        if (loginMode.equals(ConnectorLoginMode.USER)) {
          Principal principal = PrincipalHolder.getPrincipal();
          String username = principal.getName();
          Long connectorConfigId = con.getConfiguration().getId();
          ConnectorCredentials connectorCredentials = null;
          try {
            connectorCredentials = connectorCredentialsRepository.fetchConnectorCredentialsByUsernameAndConnectorConfigId(username, connectorConfigId);
          } catch (Exception e) {
            throw new CycleMissingCredentialsException("The user credentials for connector " + con.getConfiguration().getName() + " are not set.", e);
          }
          con.login(connectorCredentials.getUsername(), connectorCredentials.getPassword());
        }
      }
    }else{
      throw new CycleException("@Secured must only be used on Connector methods");
    }
  }
  
}
