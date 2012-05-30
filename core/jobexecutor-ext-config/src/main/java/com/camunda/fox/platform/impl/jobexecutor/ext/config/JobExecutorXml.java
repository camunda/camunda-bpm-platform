package com.camunda.fox.platform.impl.jobexecutor.ext.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.camunda.fox.platform.jobexecutor.spi.JobAcquisitionConfiguration;


/**
 * @author roman.smirnov
 */
@XmlRootElement(name = "job-executor")
@XmlAccessorType(XmlAccessType.FIELD)
public class JobExecutorXml {
  
  @XmlTransient
  private String resourceName;
  
  @XmlElements(@XmlElement(name = "job-acquisition", type = JobAcquisitionXml.class))
  private List<JobAcquisitionConfiguration> jobAcquisitions = new ArrayList<JobAcquisitionConfiguration>();

  public String getResourceName() {
    return resourceName;
  }
  
  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }
  
  public List<JobAcquisitionConfiguration> getJobAcquisitions() {
    return jobAcquisitions;
  }
  
  public void setJobAcquisitions(List<JobAcquisitionConfiguration> jobAcquisitions) {
    this.jobAcquisitions = jobAcquisitions;
  }

}
