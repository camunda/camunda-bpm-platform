package org.camunda.bpm.engine.test.api.runtime;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;

public class BatchModificationHelper extends BatchHelper {

  protected ProcessEngineTestRule testRule;
  protected List<String> currentProcessInstances;

  public BatchModificationHelper(ProcessEngineRule engineRule) {
    super(engineRule);
    this.testRule = new ProcessEngineTestRule(engineRule);
    currentProcessInstances = new ArrayList<String>();
  }

  public Batch startAfterAsync(String key, int numberOfProcessInstances, String activityId, String processDefinitionId) {
    RuntimeService runtimeService = engineRule.getRuntimeService();

    List<String> processInstanceIds = startInstances(key, numberOfProcessInstances);

    return runtimeService
      .createModification(processDefinitionId)
      .startAfterActivity(activityId)
      .processInstanceIds(processInstanceIds)
      .executeAsync();
  }

  public Batch startBeforeAsync(String key, int numberOfProcessInstances, String activityId, String processDefinitionId) {
    RuntimeService runtimeService = engineRule.getRuntimeService();

    List<String> processInstanceIds = startInstances(key, numberOfProcessInstances);

    return runtimeService.createModification(processDefinitionId).startBeforeActivity(activityId).processInstanceIds(processInstanceIds).executeAsync();
  }

  public Batch startTransitionAsync(String key, int numberOfProcessInstances, String transitionId, String processDefinitionId) {
    RuntimeService runtimeService = engineRule.getRuntimeService();

    List<String> processInstanceIds = startInstances(key, numberOfProcessInstances);

    return runtimeService.createModification(processDefinitionId).startTransition(transitionId).processInstanceIds(processInstanceIds).executeAsync();
  }

  public Batch cancelAllAsync(String key, int numberOfProcessInstances, String activityId, String processDefinitionId) {
    RuntimeService runtimeService = engineRule.getRuntimeService();

    List<String> processInstanceIds = startInstances(key, numberOfProcessInstances);

    return runtimeService.createModification(processDefinitionId).cancelAllForActivity(activityId).processInstanceIds(processInstanceIds).executeAsync();
  }

  public List<String> startInstances(String key, int numOfInstances) {
    List<String> instances = new ArrayList<String>();
    for (int i = 0; i < numOfInstances; i++) {
      ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey(key);
      instances.add(processInstance.getId());
    }

    currentProcessInstances = instances;
    return instances;
  }

  @Override
  public JobDefinition getExecutionJobDefinition(Batch batch) {
    return engineRule.getManagementService()
        .createJobDefinitionQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).jobType(Batch.TYPE_PROCESS_INSTANCE_MODIFICATION).singleResult();
  }

}
