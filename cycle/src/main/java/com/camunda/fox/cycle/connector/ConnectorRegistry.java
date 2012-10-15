package com.camunda.fox.cycle.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.camunda.fox.cycle.aspect.LoginAspect;
import com.camunda.fox.cycle.aspect.ThreadsafeAspect;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.exception.CycleException;
import com.camunda.fox.cycle.repository.ConnectorConfigurationRepository;

@Component
@Scope(
  value = WebApplicationContext.SCOPE_SESSION, 
  proxyMode = ScopedProxyMode.TARGET_CLASS
)
public class ConnectorRegistry {
  
  @Inject
  private LoginAspect loginAspect;
  
  @Inject
  private ThreadsafeAspect threadsafeAspect;
  
  /**
   * Currently, connector singletons are fetched from the application context
   * Will change to fetching from database at some point.
   */
  @Inject
  private List<ConnectorConfiguration> configurations;
  
  @Inject
  private ConnectorConfigurationRepository connectorConfigurationRepository;
  
  /**
   * Connector cache 
   */
  private Map<Long, Connector> connectorCache = new HashMap<Long, Connector>();
  
  /**
   * Return connector configuration for given id or null
   * 
   * @param connectorId
   * @return 
   */
  private ConnectorConfiguration getConnectorConfiguration(long connectorId) {
    for (ConnectorConfiguration c: getConnectorConfigurations()) {
      if (c.getId() == connectorId) {
        return c;
      }
    }
    
    return null;
  }
  
  /**
   * Returns a list of all connector configurations known to this registry
   * @return 
   */
//  public List<ConnectorConfiguration> getConnectorConfigurations() {
//    return configurations;
//  }
  // TODO: kp: change this method to get only the connector configuration from the database
  // delete the fallback to get the configuration from the context.xml and insert it into the database
  public List<ConnectorConfiguration> getConnectorConfigurations() {
    List<ConnectorConfiguration> connectorConfigurationsList = connectorConfigurationRepository.findAll();
    if (connectorConfigurationsList != null && !connectorConfigurationsList.isEmpty()) {
      return connectorConfigurationsList;
    } else {
      for (ConnectorConfiguration connectorConfiguration : configurations) {
        connectorConfigurationRepository.saveAndFlush(connectorConfiguration);
      }
    }
    
    return connectorConfigurationRepository.findAll();
  }

  /**
   * Return the connector with the given class or null if none was found
   * @param cls
   * @return 
   */
  public Connector getConnector(Class<? extends Connector> cls) {
    for (ConnectorConfiguration config: getConnectorConfigurations()) {
      if (config.getConnectorClass().equals(cls.getName())) {
        return getConnector(config.getId());
      }
    }
    return null;
  }

  /**
   * Return a connector with the given id
   * 
   * @param connectorId
   * @return the connector
   * 
   * @throws CycleException if the connector is unavailable
   */
  public Connector getConnector(long connectorId) {
    Connector connector = connectorCache.get(connectorId);
    if (connector == null) {
      connector = instantiateConnector(connectorId);
      connectorCache.put(connectorId, connector);
    }
    
    return connector;
  }
  
  public Connector updateConnectorInCache(long connectorId) {
    Connector connector = connectorCache.get(connectorId);
    if (connector != null) {
      connector.dispose();
      connector = instantiateConnector(connectorId);
      connectorCache.put(connectorId, connector);
    }
    return connector;
  }
  
  public void deleteConnectorFromCache(long connectorId) {
    Connector connector = connectorCache.get(connectorId);
    if (connector != null) {
      connector.dispose();
      connectorCache.remove(connectorId);
    }
  }
    
  
  public Connector addConnectorToCache(long connectorId) {
    Connector connector = instantiateConnector(connectorId);
    connectorCache.put(connectorId, connector);
    return connector;
  }

  /**
   * Eagerly loads all connectors for which a connector 
   * configuration is available and returns the connectors
   * 
   * @return list of connectors loaded in this registry
   */
  public List<Connector> getConnectors() {
    for (ConnectorConfiguration c: getConnectorConfigurations()) {
      getConnector(c.getId());
    }
    
    return new ArrayList<Connector>(connectorCache.values());
  }

  /**
   * Instantiate connector with the given id.
   * 
   * @param connectorId
   * @return 
   * 
   * @throws CycleException if connector could not be instantiated
   */
  private Connector instantiateConnector(long connectorId) {
    ConnectorConfiguration config = getConnectorConfiguration(connectorId);
    if (config == null) {
      throw new CycleException("Connector configuration for connectorId " + connectorId + " not available");
    }
    
    return instantiateConnector(config);
  }
  
  /**
   * Initializes a connector from the given configuration and returns it
   * 
   * Does not perform any caching.
   * 
   * @param config
   * @return the newly instantiated connector
   */
  Connector instantiateConnector(ConnectorConfiguration config) {
    try {
      AspectJProxyFactory factory = new AspectJProxyFactory(Class.forName(config.getConnectorClass()).newInstance());
      factory.addAspect(loginAspect);
      factory.addAspect(threadsafeAspect);
      Connector instance = factory.getProxy();

      // TODO: set name / configuration from user configuration entities
      instance.setConfiguration(config);
      instance.init();

      return instance;
    } catch (Exception e) {
      throw new CycleException("Could not init connector", e);
    }
  }
}
