package org.camunda.bpm.engine.test.api.runtime;

import static org.junit.Assert.assertNotNull;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;


public class BatchRestartHelper extends BatchHelper {

  public BatchRestartHelper(ProcessEngineRule engineRule) {
    super(engineRule);
  }

  public BatchRestartHelper(PluggableProcessEngineTestCase testCase) {
    super(testCase);
  }

  @Override
  public JobDefinition getExecutionJobDefinition(Batch batch) {
    return getManagementService()
        .createJobDefinitionQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).jobType(Batch.TYPE_PROCESS_INSTANCE_RESTART).singleResult();
  }
  
  
  public void executeJob(Job job) {
    assertNotNull("Job to execute does not exist", job);
    getManagementService().executeJob(job.getId());
  }
}
