package org.activiti.engine.management;

public interface ActivityStatistics {

  String getId();
  
  int getInstances();
  
  int getFailedJobs();
}
