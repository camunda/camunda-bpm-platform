package com.camunda.fox.cycle.aspect;

import javax.inject.Inject;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorLoginMode;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.exception.CycleException;
import com.camunda.fox.cycle.repository.ConnectorCredentialsRepository;

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
          
        }
      }
    }else{
      throw new CycleException("@Secured must only be used on Connector methods");
    }
  }
  
}
