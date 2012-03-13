package com.camunda.fox.platform.impl.jobexecutor.commonj;

import org.activiti.engine.impl.jobexecutor.JobExecutor;

import com.camunda.fox.platform.impl.jobexecutor.spi.JobExecutorFactory;

/**
 * @author Daniel Meyer
 */
public class WorkManagerJobExecutorFactory implements JobExecutorFactory {

  public JobExecutor getJobExecutor() {
    return new WorkManagerJobExecutor();
  }

}
