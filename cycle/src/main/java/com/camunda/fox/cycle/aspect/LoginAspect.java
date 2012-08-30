package com.camunda.fox.cycle.aspect;

import java.util.HashMap;
import java.util.Map;

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
  
  private Map<String, Boolean> connectorLoggedIn = new HashMap<String, Boolean>();
  
  @Before("@annotation(com.camunda.fox.cycle.api.connector.Secured)")
  private void aroundSecured(JoinPoint jp) throws Throwable {
    if (jp.getTarget() instanceof Connector) {
      Connector con = (Connector) jp.getTarget();
      if (!this.connectorLoggedIn.get(con.getConnectorId())) {
        ConnectorConfiguration config = con.getConfiguration();
        if (config.getLoginMode() != null && config.getLoginMode().equals(ConnectorLoginMode.GLOBAL)) {
          con.login(config.getGlobalUser(), config.getGlobalPassword());
        }
        // TODO: Handle other cases: LOGIN_NOT_REQUIRED and USER_LOGIN
        this.connectorLoggedIn.put(con.getConnectorId(), true);
      }
    }else{
      throw new CycleException("@Secured must only be used on Connector methods");
    }
  }
  
  public Map<String, Boolean> getConnectorLoggedIn() {
    return this.connectorLoggedIn;
  }
}
