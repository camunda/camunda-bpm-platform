package org.camunda.bpm.application.impl.deployment.spi;

import java.util.Map;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;

/**
 * <p>Java API representation of a ProcessEngine definition inside an XML
 * deployment descriptor.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public interface ProcessEngineXml {

  /**
   * @return the name of the process engine. Must not be null.
   */
  public String getName();

  /**
   * @return true if the process engine is the default process engine.
   */
  public boolean isDefault();

  /**
   * @return the name of the Java Class that is to be used in order to create
   *         the process engine instance. Must be a subclass of
   *         {@link ProcessEngineConfiguration}. If no value is specified,
   *         {@link StandaloneProcessEngineConfiguration} is used.
   */
  public String getConfigurationClass();
  
  /**
   * @return the JNDI Name of the datasource to be used. 
   */
  public String getDatasource();

  /**
   * @return a set of additional properties. The properties are directly set on
   *         the {@link ProcessEngineConfiguration} class (see
   *         {@link #getConfigurationClass()}). This means that each property
   *         name used here must be a bean property name on the process engine
   *         configuration class and the bean property must be of type
   *         {@link String}.
   */
  public Map<String, String> getProperties();

}
