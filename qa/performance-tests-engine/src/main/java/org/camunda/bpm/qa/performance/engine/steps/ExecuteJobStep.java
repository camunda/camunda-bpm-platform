package org.camunda.bpm.qa.performance.engine.steps;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestRunContext;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestStepBehavior;

public class ExecuteJobStep extends ProcessEngineAwareStep implements PerfTestStepBehavior {

  public ExecuteJobStep(ProcessEngine processEngine) {
    super(processEngine);
  }

  @Override
  public void execute(PerfTestRunContext context) {
    ManagementService managementService = this.processEngine.getManagementService();
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());
  }

}
