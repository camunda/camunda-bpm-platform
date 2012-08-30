package com.camunda.fox.cycle.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.api.connector.Connector;
import com.camunda.fox.cycle.api.connector.ConnectorLoginMode;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.exception.CycleException;

@Component
@Aspect
public class LoginAspect {
  
  @Before("@annotation(com.camunda.fox.cycle.api.connector.Secured)")
  private void aroundSecured(JoinPoint jp) throws Throwable {
    if (jp.getTarget() instanceof Connector) {
      Connector con = (Connector) jp.getTarget();
      if (con.needsLogin()) {
        ConnectorConfiguration config = con.getConfiguration();
        if (config.getLoginMode() != null && config.getLoginMode().equals(ConnectorLoginMode.GLOBAL)) {
          con.login(config.getGlobalUser(), config.getGlobalPassword());
        }
      }
    }else{
      throw new CycleException("@Secured must only be used on Connector methods");
    }
  }
  
}
