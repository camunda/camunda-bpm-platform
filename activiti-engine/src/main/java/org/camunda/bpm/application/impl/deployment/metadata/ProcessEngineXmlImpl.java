package org.camunda.bpm.application.impl.deployment.metadata;

import java.util.Map;

import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessEngineXml;

/**
 * <p>Implementation of the {@link ProcessEngineXml} descriptor.</p>
 * 
 * @author Daniel Meyer
 */
public class ProcessEngineXmlImpl implements ProcessEngineXml {
  
  private String name;
  private boolean isDefault;
  private String configurationClass;
  private String jobAcquisitionName;
  private String datasource;
  private Map<String, String> properties;
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public boolean isDefault() {
    return isDefault;
  }
  
  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }
  
  public String getConfigurationClass() {
    return configurationClass;
  }
  
  public void setConfigurationClass(String configurationClass) {
    this.configurationClass = configurationClass;
  }
  
  public Map<String, String> getProperties() {
    return properties;
  }
  
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
  
  public String getDatasource() {
    return datasource;
  }
  
  public void setDatasource(String datasource) {
    this.datasource = datasource;
  }
  
  public String getJobAcquisitionName() {
    return jobAcquisitionName;
  }
  
  public void setJobAcquisitionName(String jobAcquisitionName) {
    this.jobAcquisitionName = jobAcquisitionName;
  }

}
