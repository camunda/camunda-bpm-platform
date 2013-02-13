package com.camunda.fox.cycle.aspect;


import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorLoginMode;
import com.camunda.fox.cycle.connector.crypt.EncryptionService;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.entity.ConnectorCredentials;
import com.camunda.fox.cycle.exception.CycleException;
import com.camunda.fox.cycle.exception.CycleMissingCredentialsException;
import com.camunda.fox.cycle.repository.ConnectorCredentialsRepository;
import com.camunda.fox.cycle.security.IdentityHolder;
import com.camunda.fox.security.UserIdentity;

@Component
@Aspect
public class LoginAspect {
  
  @Inject
  protected EncryptionService encryptionService;

  @Inject
  private ConnectorCredentialsRepository connectorCredentialsRepository;
  
  @Before("@annotation(com.camunda.fox.cycle.connector.Secured)")
  private void aroundSecured(JoinPoint jp) throws Throwable {
    if (jp.getTarget() instanceof Connector) {
      Connector connector = (Connector) jp.getTarget();
      doLogin(connector);
    }else{
      throw new CycleException("@Secured must only be used on Connector methods");
    }
  }

  public void doLogin(Connector connector) {
    synchronized (connector) {
      // TODO: better would be a single method and doing the 
      // synchronization in the connector.
      if (connector.needsLogin()) {
        ConnectorConfiguration config = connector.getConfiguration();
        connector.init(config);
  
        loginConnector(connector, config, config.getLoginMode());
      }
    }
  }

  public void setConnectorCredentialsRepository(ConnectorCredentialsRepository connectorCredentialsRepository) {
    this.connectorCredentialsRepository = connectorCredentialsRepository;
  }

  private void loginConnector(Connector connector, ConnectorConfiguration config, ConnectorLoginMode loginMode) {
    if (loginMode == null) {
      return;
    }

    switch (loginMode) {
      case LOGIN_NOT_REQUIRED: 
        break;
      case GLOBAL:
        loginConnector(connector, config.getGlobalUser(), config.getGlobalPassword());
        break;
      case USER:
        loginWithUserCredentials(connector);
        break;
    }
  }

  private void loginConnector(Connector connector, String username, String password) {
    connector.login(username, encryptionService.decryptConnectorPassword(password));
  }

  private void loginWithUserCredentials(Connector connector) {
    UserIdentity identity = IdentityHolder.getIdentity();
    
    if (identity == null) {
      throw missingCredentials("No user identity found. Please relogin into cycle.");
    }
    
    String username = identity.getName();
    Long connectorConfigId = connector.getConfiguration().getId();
    
    if (connectorConfigId == null) {
      throw missingCredentials("No user specific credentials configured for connector '" + connector.getConfiguration().getName() + "'.");
    }
    
    try {
      ConnectorCredentials credentials = connectorCredentialsRepository.findFetchAllByUsernameAndConnectorId(username, connectorConfigId);
      loginConnector(connector, credentials.getUsername(), credentials.getPassword());
    } catch (NoResultException e) {
      throw missingCredentials("No user specific credentials configured for connector '" + connector.getConfiguration().getName() + "'.");
    }
  }
  
  private CycleMissingCredentialsException missingCredentials(String message) {
    return new CycleMissingCredentialsException("Missing credentials: " + message);
  }
}
