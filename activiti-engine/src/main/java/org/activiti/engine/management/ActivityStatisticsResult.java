package org.activiti.engine.management;

public interface ActivityStatisticsResult {

  String getId();
  
  int getInstances();
  
  int getFailedJobs();
}
