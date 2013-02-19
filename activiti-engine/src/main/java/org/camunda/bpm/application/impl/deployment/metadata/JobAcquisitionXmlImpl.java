package org.camunda.bpm.application.impl.deployment.metadata;

import java.util.Map;

import org.camunda.bpm.application.impl.deployment.metadata.spi.JobAcquisitionXml;

/**
 * <p>Implementation of the {@link JobAcquisitionXml} SPI interface</p>
 * 
 * @author Daniel Meyer
 * 
 */
public class JobAcquisitionXmlImpl implements JobAcquisitionXml {

  private String name;
  private String acquisitionStrategy;
  private Map<String, String> properties;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAcquisitionStrategy() {
    return acquisitionStrategy;
  }

  public void setAcquisitionStrategy(String acquisitionStrategy) {
    this.acquisitionStrategy = acquisitionStrategy;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

}
