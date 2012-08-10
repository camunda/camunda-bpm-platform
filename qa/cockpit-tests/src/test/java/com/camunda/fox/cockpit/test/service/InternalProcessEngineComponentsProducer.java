package com.camunda.fox.cockpit.test.service;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import com.camunda.fox.cdi.FoxProcessEngineLookup;

/**
 *
 * @author nico.rehwaldt
 */
public class InternalProcessEngineComponentsProducer {

  @Inject
  private FoxProcessEngineLookup engineLookup;
  
  @Produces
  @RequestScoped
  public JobExecutor getJobExecutor() {
    return ((ProcessEngineImpl) engineLookup.getProcessEngine()).getProcessEngineConfiguration().getJobExecutor();
  }
}
