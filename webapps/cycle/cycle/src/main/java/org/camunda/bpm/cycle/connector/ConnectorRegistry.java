package org.camunda.bpm.cycle.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.camunda.bpm.cycle.aspect.LoginAspect;
import org.camunda.bpm.cycle.aspect.ThreadsafeAspect;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.camunda.bpm.cycle.exception.CycleException;
import org.camunda.bpm.cycle.repository.ConnectorConfigurationRepository;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.stereotype.Component;


@Component
public class ConnectorRegistry {

  private static final Logger logger = Logger.getLogger(ConnectorRegistry.class.getSimpleName());
  
  @Inject
  private LoginAspect loginAspect;

  @Inject
  private ThreadsafeAspect threadsafeAspect;

  private ConnectorCache cache;

  @Inject
  public void setConnectorCache(ConnectorCache cache) {
    this.cache = cache;
  }

  /**
   * Default connector configurations are configured in the spring application context. 
   * They are used as blueprints for actual connectors.
   */
  @Inject
  private List<ConnectorConfiguration> connectorDefinitions;

  @Inject
  private ConnectorConfigurationRepository connectorConfigurationRepository;

  /**
   * Return a list of default configurations
   * @return 
   */
  public List<ConnectorConfiguration> getConnectorDefinitions() {
    return connectorDefinitions;
  }

  /**
   * Returns the connector definition for the given connector class
   * or <code>null</code> if no definition was found.
   * 
   * @param cls
   * @return 
   */
  public ConnectorConfiguration getConnectorDefinition(Class<? extends Connector> cls) {
    for (ConnectorConfiguration definition : connectorDefinitions) {
      if (definition.getConnectorClass().equals(cls.getName())) {
        return definition;
      }
    }
    
    return null;
  }
  
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
  public List<ConnectorConfiguration> getConnectorConfigurations() {
    return connectorConfigurationRepository.findAll();
  }

  /**
   * Returns a list of connector configurations for a given connector class
   * @param cls
   * @return 
   */
  public List<ConnectorConfiguration> getConnectorConfigurations(Class<? extends Connector> cls) {
    return connectorConfigurationRepository.findByConnectorClass(cls.getName());
  }

  /**
   * Return the first connector with the given class or null if none was found
   * @param cls
   * @return 
   */
  public Connector getConnector(Class<? extends Connector> cls) {
    List<ConnectorConfiguration> configs = getConnectorConfigurations(cls);
    if (!configs.isEmpty()) {
      return getConnector(configs.get(0).getId());
    } else {
      return null;
    }
  }

  /**
   * Return a connector with the given id
   * 
   * @param connectorId
   * @return the connector
   * 
   * @throws CycleException if the connector is unavailable
   */
  public synchronized Connector getConnector(long connectorId) {
    Connector connector = cache.get(connectorId);
    if (connector == null) {
      connector = instantiateConnector(connectorId);
      cache.put(connectorId, connector);
    }

    return connector;
  }

  public ConnectorCache getCache() {
    return cache;
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

    return new ArrayList<Connector>(cache.values());
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
      return null;
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
    return instantiateConnector(config, true);
  }

  private Connector instantiateConnector(ConnectorConfiguration config, boolean addLoginAspect) {
    try {
      AspectJProxyFactory factory = new AspectJProxyFactory(Class.forName(config.getConnectorClass()).newInstance());
      
      if (addLoginAspect) {
        factory.addAspect(loginAspect);
      }
      
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
  

  public ConnectorStatus testConnectorConfiguration(ConnectorConfiguration config) {
    Connector connector = null;

    try {
      connector = instantiateConnector(config, true);

      return executeTest(connector);
    } catch (Exception e) {
      logger.log(Level.WARNING, "Error while testing connector configuration: " + config.getName(), e);
      return ConnectorStatus.inError(e);
    } finally {
      if (connector != null) {
        connector.dispose();
      }
    }
  }
  
  public ConnectorStatus testConnectorConfiguration(ConnectorConfiguration config, String username, String password) {
    Connector connector = null;
    try {
      connector = instantiateConnector(config, false);
      connector.login(username, password);
      
      return executeTest(connector);
    } catch (Exception e) {
      logger.log(Level.WARNING, "Error while testing connector configuration: " + config.getName(), e);
      return ConnectorStatus.inError(e);
    } finally {
      if (connector != null) {
        connector.dispose();
      }
    }
  }
  
  private ConnectorStatus executeTest(Connector connector) {
    try {
      
      ConnectorNode root = connector.getRoot();
      // list children of root
      connector.getChildren(root);

      // everything ok
      return ConnectorStatus.ok();
    } catch (Exception e) {
      logger.log(Level.WARNING, "Error while testing connector configuration: " + connector.getConfiguration().getName(), e);
      return ConnectorStatus.inError(e);
    }
  }
}
