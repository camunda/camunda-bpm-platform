package org.camunda.bpm.application.impl.deployment.parser;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.application.impl.deployment.spi.ProcessArchiveXml;

public class ProcessArchiveXmlImpl implements ProcessArchiveXml {
  
  private String name;
  private String processEngineName;
  private List<String> processResourceNames;
  private Map<String, String> properties;
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getProcessEngineName() {
    return processEngineName;
  }
  
  public void setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
  }
  
  public List<String> getProcessResourceNames() {
    return processResourceNames;
  }
  
  public void setProcessResourceNames(List<String> processResourceNames) {
    this.processResourceNames = processResourceNames;
  }
  
  public Map<String, String> getProperties() {
    return properties;
  }
  
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }


}
