package com.camunda.fox.cycle.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.camunda.fox.cycle.api.connector.Connector;
import com.camunda.fox.cycle.aspect.LoginAspect;
import com.camunda.fox.cycle.security.SecurityContext;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ConnectorRegistry {
  
  @Inject
  ApplicationContext appContext;
  
  @Inject
  LoginAspect loginAspect;
  
  @Inject
  SecurityContext securityContext;
  
  /**
   * Get the connector singletons from application context
   */
  @Inject
  Map<String ,Connector> connectors;
  
  Map<String ,Connector> sessionConnectorMap = new HashMap<String, Connector>();
  
  List<Connector> sessionConnectors;
  
  /**
   * Create the instances of the connectors for the current session
   * @return
   */
  public List<Connector> getSessionConnectors()  {
    if (sessionConnectors == null) {
      sessionConnectors = new ArrayList<Connector>();
      for (Entry<String, Connector> connector : connectors.entrySet()) {
        try {
          AspectJProxyFactory factory = new AspectJProxyFactory(connector.getValue().getClass().newInstance()); 
          factory.addAspect(loginAspect);
          Connector newConnectorInstance = factory.getProxy();
          
          // TODO set name / configuration from user configuration entities
          newConnectorInstance.setConnectorId(connector.getKey());
          newConnectorInstance.setName(connector.getKey());
          
          sessionConnectorMap.put(connector.getKey(), newConnectorInstance);
        } catch (Exception e) {
          throw new RuntimeException(e);
        } 
      }
      sessionConnectors = new ArrayList<Connector>(sessionConnectorMap.values());
    }
    
    return sessionConnectors;
  }
  
  public Map<String, Connector> getSessionConnectorMap() {
    return sessionConnectorMap;
  }
  
  public Map<String, Connector> getConnectors() {
    return connectors;
  }
  
}
