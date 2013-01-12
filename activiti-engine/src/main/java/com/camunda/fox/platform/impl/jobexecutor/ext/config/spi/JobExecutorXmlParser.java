package com.camunda.fox.platform.impl.jobexecutor.ext.config.spi;

import java.util.List;

import com.camunda.fox.platform.impl.jobexecutor.ext.config.JobExecutorXml;


public interface JobExecutorXmlParser {
  
  public List<JobExecutorXml> parseJobExecutorXml(String jobExecutorXmlLocation);

}
