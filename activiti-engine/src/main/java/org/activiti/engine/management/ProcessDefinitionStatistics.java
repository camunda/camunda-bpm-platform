package org.activiti.engine.management;

import org.activiti.engine.repository.ProcessDefinition;

public interface ProcessDefinitionStatistics extends ProcessDefinition {

  int getInstances();
  
  int getFailedJobs();
}
