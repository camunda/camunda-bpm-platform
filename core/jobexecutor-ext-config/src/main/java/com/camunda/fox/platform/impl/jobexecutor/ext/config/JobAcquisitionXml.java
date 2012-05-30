package com.camunda.fox.platform.impl.jobexecutor.ext.config;

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

import com.camunda.fox.platform.jobexecutor.spi.JobAcquisitionConfiguration;

@XmlAccessorType(XmlAccessType.FIELD)
public class JobAcquisitionXml implements JobAcquisitionConfiguration {
  
  @XmlAttribute(name = "name", required = true)
  private String acquisitionName;

  @XmlAttribute(name = "acquisition-strategy", required = true)
  private String jobAcquistionStrategy;
  
  @XmlElement(name="properties", required=false)
  private JobAcquisitionXml.Properties properties = new Properties();
  
  @XmlTransient
  private Map<String, Object> map;

  public String getAcquisitionName() {
    return this.acquisitionName;
  }

  public String getJobAcquisitionStrategy() {
    return this.jobAcquistionStrategy;
  }

  public Map<String, Object> getJobAcquisitionProperties() {
    if (this.map == null) {
      this.map = new HashMap<String, Object>();
      for (Properties.Property mapEntry : properties.properties) {
        String value = mapEntry.value;
        if(value.equals("true") || value.equals("false")) {
          this.map.put(mapEntry.name, Boolean.parseBoolean(value));
        } else {
          try {
            this.map.put(mapEntry.name, Integer.valueOf(value));
          } catch (NumberFormatException e) {
            this.map.put(mapEntry.name, value);
          }
        }
      }
      if(!map.containsKey(JobAcquisitionConfiguration.PROP_LOCK_TIME_IN_MILLIS)) {
        map.put(JobAcquisitionConfiguration.PROP_LOCK_TIME_IN_MILLIS, 5 * 60 * 1000);          
      }
      if(!map.containsKey(JobAcquisitionConfiguration.PROP_MAX_JOBS_PER_ACQUISITION)) {
        map.put(JobAcquisitionConfiguration.PROP_MAX_JOBS_PER_ACQUISITION, 3);
      }
      if(!map.containsKey(JobAcquisitionConfiguration.PROP_WAIT_TIME_IN_MILLIS)) {
        map.put(JobAcquisitionConfiguration.PROP_WAIT_TIME_IN_MILLIS, 5 * 1000);
      }
    }
    return this.map;
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