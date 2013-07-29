package org.camunda.bpm.cockpit.plugin.base;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessDefinitionDto;
import org.camunda.bpm.cockpit.impl.plugin.base.query.parameter.ProcessDefinitionQueryParameter;
import org.camunda.bpm.cockpit.impl.plugin.base.sub.resources.ProcessDefinitionResource;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProcessDefinitionResourceTest extends AbstractCockpitPluginTest {

  private ProcessEngine processEngine;
  private RuntimeService runtimeService;
  private RepositoryService repositoryService;
  private JobExecutorHelper helper;

  private ProcessDefinitionResource resource;
  
  @Before
  public void setUp() throws Exception {
    super.before();

    processEngine = getProcessEngine();

    helper = new JobExecutorHelper(processEngine);

    runtimeService = processEngine.getRuntimeService();
    repositoryService = processEngine.getRepositoryService();
  }
  
  @Test
  @Deployment(resources = {
    "processes/failing-process.bpmn",
    "processes/call-activity.bpmn"
  })
  public void testCalledProcessDefinitionBySuperProcessDefinitionId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("CallActivity");
    resource = new ProcessDefinitionResource(getProcessEngine().getName(), processInstance.getProcessDefinitionId());

    helper.waitForJobExecutorToProcessAllJobs(15000);
    
    ProcessDefinition failingProcess = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("FailingProcess")
        .singleResult();

    ProcessDefinitionQueryParameter queryParameter = new ProcessDefinitionQueryParameter();
    
    List<ProcessDefinitionDto> result = resource.queryCalledProcessDefinitions(queryParameter);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
    
    ProcessDefinitionDto dto = result.get(0);
    
    assertThat(dto.getId()).isEqualTo(failingProcess.getId());
    assertThat(dto.getKey()).isEqualTo(failingProcess.getKey());
    assertThat(dto.getName()).isEqualTo(failingProcess.getName());
    assertThat(dto.getVersion()).isEqualTo(failingProcess.getVersion());
    assertThat(dto.getCalledFromActivityIds()).hasSize(1);
    
    String calledFrom = dto.getCalledFromActivityIds().get(0);
    
    assertThat(calledFrom).isEqualTo("CallActivity_1");
    
  }
  
  @Test
  @Deployment(resources = {
    "processes/failing-process.bpmn",
    "processes/two-parallel-call-activities-calling-same-process.bpmn"
  })
  public void testCalledProcessDefinitionBySuperProcessDefinitionIdWithTwoActivityCallingSameProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingSameProcess");
    resource = new ProcessDefinitionResource(getProcessEngine().getName(), processInstance.getProcessDefinitionId());
    
    helper.waitForJobExecutorToProcessAllJobs(15000);
    
    ProcessDefinition failingProcess = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("FailingProcess")
        .singleResult();

    ProcessDefinitionQueryParameter queryParameter = new ProcessDefinitionQueryParameter();
    
    List<ProcessDefinitionDto> result = resource.queryCalledProcessDefinitions(queryParameter);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
    
    ProcessDefinitionDto dto = result.get(0);
    
    assertThat(dto.getId()).isEqualTo(failingProcess.getId());
    assertThat(dto.getKey()).isEqualTo(failingProcess.getKey());
    assertThat(dto.getName()).isEqualTo(failingProcess.getName());
    assertThat(dto.getVersion()).isEqualTo(failingProcess.getVersion());
    assertThat(dto.getCalledFromActivityIds()).hasSize(2);
    
    for (String activityId : dto.getCalledFromActivityIds()) {
      if (activityId.equals("firstCallActivity")) {
        assertThat(activityId).isEqualTo("firstCallActivity");    
      } else if (activityId.equals("secondCallActivity")) {
        assertThat(activityId).isEqualTo("secondCallActivity");
      } else {
        Assert.fail("Unexpected activity id:" + activityId);
      }
    }
  }
  
  @Test
  @Deployment(resources = {
    "processes/failing-process.bpmn",
    "processes/two-parallel-call-activities-calling-same-process.bpmn",
    "processes/call-activity.bpmn"
  })
  public void testCalledProcessDefinitionByCallingSameProcessFromDifferentProcessDefinitions() {
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingSameProcess");
    ProcessDefinitionResource resource1 = new ProcessDefinitionResource(getProcessEngine().getName(), processInstance1.getProcessDefinitionId());
    
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("CallActivity");
    ProcessDefinitionResource resource2 = new ProcessDefinitionResource(getProcessEngine().getName(), processInstance2.getProcessDefinitionId());

    helper.waitForJobExecutorToProcessAllJobs(15000);
    
    ProcessDefinition failingProcess = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("FailingProcess")
        .singleResult();

    ProcessDefinitionQueryParameter queryParameter1 = new ProcessDefinitionQueryParameter();
    
    List<ProcessDefinitionDto> result1 = resource1.queryCalledProcessDefinitions(queryParameter1);
    assertThat(result1).isNotEmpty();
    assertThat(result1).hasSize(1);
    
    ProcessDefinitionDto dto1 = result1.get(0);
    
    assertThat(dto1.getId()).isEqualTo(failingProcess.getId());
    assertThat(dto1.getKey()).isEqualTo(failingProcess.getKey());
    assertThat(dto1.getName()).isEqualTo(failingProcess.getName());
    assertThat(dto1.getVersion()).isEqualTo(failingProcess.getVersion());
    assertThat(dto1.getCalledFromActivityIds()).hasSize(2);
    
    for (String activityId : dto1.getCalledFromActivityIds()) {
      if (activityId.equals("firstCallActivity")) {
        assertThat(activityId).isEqualTo("firstCallActivity");    
      } else if (activityId.equals("secondCallActivity")) {
        assertThat(activityId).isEqualTo("secondCallActivity");
      } else {
        Assert.fail("Unexpected activity id:" + activityId);
      }
    }
    
    ProcessDefinitionQueryParameter queryParameter2 = new ProcessDefinitionQueryParameter();
    
    List<ProcessDefinitionDto> result2 = resource2.queryCalledProcessDefinitions(queryParameter2);
    assertThat(result2).isNotEmpty();
    assertThat(result2).hasSize(1);
    
    ProcessDefinitionDto dto2 = result2.get(0);
    
    assertThat(dto2.getId()).isEqualTo(failingProcess.getId());
    assertThat(dto2.getKey()).isEqualTo(failingProcess.getKey());
    assertThat(dto2.getName()).isEqualTo(failingProcess.getName());
    assertThat(dto2.getVersion()).isEqualTo(failingProcess.getVersion());
    assertThat(dto2.getCalledFromActivityIds()).hasSize(1);
    
    String calledFrom = dto2.getCalledFromActivityIds().get(0);
    
    assertThat(calledFrom).isEqualTo("CallActivity_1");
  }
  
  @Test
  @Deployment(resources = {
    "processes/failing-process.bpmn",
    "processes/another-failing-process.bpmn",
    "processes/two-parallel-call-activities-calling-different-process.bpmn"
  })
  public void testCalledProcessDefinitionByCallingDifferentProcessFromSameProcessDefinitions() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingDifferentProcess");
    resource = new ProcessDefinitionResource(getProcessEngine().getName(), processInstance.getProcessDefinitionId());
    
    helper.waitForJobExecutorToProcessAllJobs(15000);
    
    ProcessDefinition failingProcess = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("FailingProcess")
        .singleResult();

    ProcessDefinition anotherFailingProcess = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("AnotherFailingProcess")
        .singleResult();
    
    ProcessDefinitionQueryParameter queryParameter = new ProcessDefinitionQueryParameter();
    
    List<ProcessDefinitionDto> result = resource.queryCalledProcessDefinitions(queryParameter);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);
    
    ProcessDefinition compareWith = null;
    for (ProcessDefinitionDto dto : result) {
      String id = dto.getId();
      if (id.equals(failingProcess.getId())) {
        compareWith = failingProcess;
        assertThat(dto.getCalledFromActivityIds()).hasSize(1);
        
        String calledFrom = dto.getCalledFromActivityIds().get(0);
        
        assertThat(calledFrom).isEqualTo("firstCallActivity");
        
      } else if (id.equals(anotherFailingProcess.getId())) {
        compareWith = anotherFailingProcess;
        assertThat(dto.getCalledFromActivityIds()).hasSize(1);
        
        String calledFrom = dto.getCalledFromActivityIds().get(0);
        
        assertThat(calledFrom).isEqualTo("secondCallActivity");
        
      } else {
        Assert.fail("Unexpected process definition: " + id);
      }
      
      assertThat(dto.getId()).isEqualTo(compareWith.getId());
      assertThat(dto.getKey()).isEqualTo(compareWith.getKey());
      assertThat(dto.getName()).isEqualTo(compareWith.getName());
      assertThat(dto.getVersion()).isEqualTo(compareWith.getVersion());
    }

  }
  
  @Test
  @Deployment(resources = {
    "processes/failing-process.bpmn",
    "processes/process-with-two-parallel-failing-services.bpmn",
    "processes/dynamic-call-activity.bpmn"
  })
  public void testCalledProcessDefinitionByCallingDifferentProcessFromSameCallActivity() {
    Map<String, Object> vars1 = new HashMap<String, Object>();
    vars1.put("callProcess", "FailingProcess");
    runtimeService.startProcessInstanceByKey("DynamicCallActivity", vars1);

    Map<String, Object> vars2 = new HashMap<String, Object>();
    vars2.put("callProcess", "processWithTwoParallelFailingServices");
    runtimeService.startProcessInstanceByKey("DynamicCallActivity", vars2);
    
    helper.waitForJobExecutorToProcessAllJobs(15000);
    
    ProcessDefinition dynamicCallActivity = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("DynamicCallActivity")
        .singleResult();
    
    resource = new ProcessDefinitionResource(getProcessEngine().getName(), dynamicCallActivity.getId());
    
    ProcessDefinition failingProcess = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("FailingProcess")
        .singleResult();

    ProcessDefinition processWithTwoParallelFailingServices = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("processWithTwoParallelFailingServices")
        .singleResult();
    
    ProcessDefinitionQueryParameter queryParameter = new ProcessDefinitionQueryParameter();
    
    List<ProcessDefinitionDto> result = resource.queryCalledProcessDefinitions(queryParameter);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);
    
    ProcessDefinition compareWith = null;
    for (ProcessDefinitionDto dto : result) {
      String id = dto.getId();
      if (id.equals(failingProcess.getId())) {
        compareWith = failingProcess;
      } else if (id.equals(processWithTwoParallelFailingServices.getId())) {
        compareWith = processWithTwoParallelFailingServices;
      } else {
        Assert.fail("Unexpected process definition: " + id);
      }
      
      assertThat(dto.getId()).isEqualTo(compareWith.getId());
      assertThat(dto.getKey()).isEqualTo(compareWith.getKey());
      assertThat(dto.getName()).isEqualTo(compareWith.getName());
      assertThat(dto.getVersion()).isEqualTo(compareWith.getVersion());
      assertThat(dto.getCalledFromActivityIds()).hasSize(1);
      
      String calledFrom = dto.getCalledFromActivityIds().get(0);
      
      assertThat(calledFrom).isEqualTo("dynamicCallActivity");
    }
  }
  
  @Test
  @Deployment(resources = {
      "processes/failing-process.bpmn",
      "processes/call-activity.bpmn",
      "processes/nested-call-activity.bpmn"
    })
  public void testCalledProcessDefinitionQueryBySuperProcessDefinitionId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("NestedCallActivity");
    
    ProcessDefinition callActivityProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("CallActivity")
        .singleResult();
    
    ProcessDefinition failingProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("FailingProcess")
        .singleResult();
    
    resource = new ProcessDefinitionResource(getProcessEngine().getName(), callActivityProcess.getId());
    ProcessDefinitionQueryParameter queryParameter = new ProcessDefinitionQueryParameter();
    queryParameter.setSuperProcessDefinitionId(processInstance.getProcessDefinitionId());
    
    List<ProcessDefinitionDto> result = resource.queryCalledProcessDefinitions(queryParameter);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
    
    ProcessDefinitionDto dto = result.get(0);
    
    assertThat(dto.getId()).isEqualTo(failingProcess.getId());
    assertThat(dto.getKey()).isEqualTo(failingProcess.getKey());
    assertThat(dto.getName()).isEqualTo(failingProcess.getName());
    assertThat(dto.getVersion()).isEqualTo(failingProcess.getVersion());
    assertThat(dto.getCalledFromActivityIds()).hasSize(1);
    
    String calledFrom = dto.getCalledFromActivityIds().get(0);
    
    assertThat(calledFrom).isEqualTo("CallActivity_1");
  }
  
  @Test
  @Deployment(resources = {
    "processes/failing-process.bpmn",
    "processes/another-failing-process.bpmn",
    "processes/two-parallel-call-activities-calling-different-process.bpmn"
  })
  public void testCalledProcessDefinitionQueryByActivityId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingDifferentProcess");
    resource = new ProcessDefinitionResource(getProcessEngine().getName(), processInstance.getProcessDefinitionId());
    
    helper.waitForJobExecutorToProcessAllJobs(15000);
    
    ProcessDefinition failingProcess = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("FailingProcess")
        .singleResult();

    ProcessDefinition anotherFailingProcess = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("AnotherFailingProcess")
        .singleResult();
    
    ProcessDefinitionQueryParameter queryParameter1 = new ProcessDefinitionQueryParameter();
    String[] activityIds1 = {"firstCallActivity"};
    queryParameter1.setActivityIdIn(activityIds1);
    
    List<ProcessDefinitionDto> result1 = resource.queryCalledProcessDefinitions(queryParameter1);
    assertThat(result1).isNotEmpty();
    assertThat(result1).hasSize(1);
    
    ProcessDefinitionDto dto1 = result1.get(0);
    
    assertThat(dto1.getId()).isEqualTo(failingProcess.getId());
    assertThat(dto1.getKey()).isEqualTo(failingProcess.getKey());
    assertThat(dto1.getName()).isEqualTo(failingProcess.getName());
    assertThat(dto1.getVersion()).isEqualTo(failingProcess.getVersion());
    assertThat(dto1.getCalledFromActivityIds()).hasSize(1);
    
    String calledFrom1 = dto1.getCalledFromActivityIds().get(0);
    
    assertThat(calledFrom1).isEqualTo("firstCallActivity");
    
    ProcessDefinitionQueryParameter queryParameter2 = new ProcessDefinitionQueryParameter();
    String[] activityIds2 = {"secondCallActivity"};
    queryParameter2.setActivityIdIn(activityIds2);
    
    List<ProcessDefinitionDto> result2 = resource.queryCalledProcessDefinitions(queryParameter2);
    assertThat(result2).isNotEmpty();
    assertThat(result2).hasSize(1);
    
    ProcessDefinitionDto dto2 = result2.get(0);
    
    assertThat(dto2.getId()).isEqualTo(anotherFailingProcess.getId());
    assertThat(dto2.getKey()).isEqualTo(anotherFailingProcess.getKey());
    assertThat(dto2.getName()).isEqualTo(anotherFailingProcess.getName());
    assertThat(dto2.getVersion()).isEqualTo(anotherFailingProcess.getVersion());
    assertThat(dto2.getCalledFromActivityIds()).hasSize(1);
    
    String calledFrom2 = dto2.getCalledFromActivityIds().get(0);
    
    assertThat(calledFrom2).isEqualTo("secondCallActivity");
    
    ProcessDefinitionQueryParameter queryParameter3 = new ProcessDefinitionQueryParameter();
    String[] activityIds3 = {"firstCallActivity", "secondCallActivity"};
    queryParameter3.setActivityIdIn(activityIds3);
    
    List<ProcessDefinitionDto> result3 = resource.queryCalledProcessDefinitions(queryParameter3);
    assertThat(result3).isNotEmpty();
    assertThat(result3).hasSize(2);
    
  }

}
