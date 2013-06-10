package org.camunda.bpm.cockpit.plugin.base.resources;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.cockpit.plugin.base.persistence.entity.ActivityInstanceDto;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author nico.rehwaldt
 */
public class ProcessInstanceResourceTest extends AbstractCockpitPluginTest {

  private ProcessEngine processEngine;
  private RuntimeService runtimeService;
  private ProcessInstanceResource resource;

  @Before
  public void setUp() throws Exception {
    super.before();

    processEngine = getProcessEngine();

    runtimeService = processEngine.getRuntimeService();

    resource = new ProcessInstanceResource(processEngine.getName());
  }

  @Test
  @Deployment(resources = {
    "processes/activity-instance-test-process.bpmn",
    "processes/simple-user-task-process.bpmn"
  })
  public void testQuery() {
    // given
    // start and correlate messages
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("activityInstanceTestProcess");

    runtimeService.correlateMessage("Message_A");
    runtimeService.correlateMessage("Message_B");

    List<ActivityInstanceDto> activityInstances = resource.getActivityInstances(processInstance.getId());

//    System.out.println(
//        String.format(
//          "%40s | %40s | %20s | %10s | %10s | %10s | %10s",
//          "processInstanceId", "parentId", "actId",
//          "scope", "evt", "active", "concurrent"));

    // System.out.println(toString(activityInstances));

    assertThat(activityInstances).onProperty("activityId").containsExactly(
        "Task_D", "CallActivity_A", "Task_B", "Task_B", "Task_B", "Task_B", "Task_B", "StartEvent_2", "Task_C"
    );
  }

  protected String toString(List<?> objects) {
    StringBuilder builder = new StringBuilder();

    for (Object object : objects) {
      builder.append(object).append("\n");
    }

    return builder.toString();
  }
}
