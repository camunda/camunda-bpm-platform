package com.camunda.fox.cycle.aspect;

import javax.inject.Inject;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorLoginMode;
import com.camunda.fox.cycle.connector.crypt.EncryptionService;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.exception.CycleException;

@Component
@Aspect
public class LoginAspect {
  
  @Inject
  protected EncryptionService encryptionService;
  
  @Before("@annotation(com.camunda.fox.cycle.connector.Secured)")
  private void aroundSecured(JoinPoint jp) throws Throwable {
    if (jp.getTarget() instanceof Connector) {
      Connector con = (Connector) jp.getTarget();
      doLogin(con);
    }else{
      throw new CycleException("@Secured must only be used on Connector methods");
    }
  }
  
  public void doLogin(Connector con) {
    if (con.needsLogin()) {
      ConnectorConfiguration config = con.getConfiguration();
      con.init(config);
      if (config.getLoginMode() != null && config.getLoginMode().equals(ConnectorLoginMode.GLOBAL)) {
        con.login(config.getGlobalUser(), encryptionService.decrypt(config.getGlobalPassword()));
      }
    }
  }
  
}
