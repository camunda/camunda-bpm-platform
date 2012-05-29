package com.camunda.fox.platform.impl.ext.config.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessEngineXml implements ProcessEngineConfiguration {

  @XmlAttribute(name = "name", required = true)
  public String processEngineName;

  @XmlAttribute(name = "default", required = false)
  public boolean isDefault = false;

  @XmlElement(name = "datasource", required = true)
  public String dataSourceJndiName;

  @XmlElement(name = "history-level", required = false)
  public String historyLevel = "audit";
  
  @XmlElement(name="properties", required=false)
  public ProcessEngineXml.Properties properties = new Properties();
  
  @XmlTransient
  public Map<String, Object> map;
  
  public String getHistoryLevel() {
    return historyLevel;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public String getProcessEngineName() {
    return processEngineName;
  }

  public String getDatasourceJndiName() {
    return dataSourceJndiName;
  }

  public Map<String, Object> getProperties() {
    if (map == null) {
      map = new HashMap<String, Object>();
      for (Properties.Property mapEntry : properties.properties) {
        String value = mapEntry.value;
        if(value.equals("true") || value.equals("false")) {
          map.put(mapEntry.name, Boolean.parseBoolean(value));
        } else {
          map.put(mapEntry.name, value);
        }
      }
      if(!map.containsKey(ProcessEngineConfiguration.PROP_IS_ACTIVATE_JOB_EXECUTOR)) {
        map.put(ProcessEngineConfiguration.PROP_IS_ACTIVATE_JOB_EXECUTOR, false);          
      }
      if(!map.containsKey(ProcessEngineConfiguration.PROP_IS_AUTO_SCHEMA_UPDATE)) {
        map.put(ProcessEngineConfiguration.PROP_IS_AUTO_SCHEMA_UPDATE, false);
      }
      if(!map.containsValue(ProcessEngineConfiguration.PROP_JOB_EXECUTOR_ACQUISITION_NAME)) {
        map.put(ProcessEngineConfiguration.PROP_JOB_EXECUTOR_ACQUISITION_NAME, "default");
      }
    }
    return map;
  }
  
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Properties {
    
    @XmlElements(@XmlElement(name = "property", type = Properties.Property.class))
    public List<Properties.Property> properties = new ArrayList<Properties.Property>();
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Property {
              
      @XmlAttribute(name="name", required=true)
      public String name = "";

      @XmlValue
      public String value;

    }
  }
}