package com.camunda.fox.cycle.connector;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.camunda.fox.cycle.api.connector.Connector;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ConnectorRegistry {
  
  @Inject
  ApplicationContext appContext;
  
  @Inject
  List<Connector> connectors;
  
  public List<Connector> getConnectors()  {
    ArrayList<Connector> userConnectors = new ArrayList<Connector>();
    try {
      userConnectors.add((Connector) appContext.getBean("vfsConnector").getClass().newInstance());
    }
    catch (Exception e) {
      
    }
    return userConnectors;
  }
  
}
