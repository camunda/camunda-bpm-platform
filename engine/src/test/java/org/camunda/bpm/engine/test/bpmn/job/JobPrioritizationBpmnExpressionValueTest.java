/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.bpmn.job;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobPriorityProvider;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class JobPrioritizationBpmnExpressionValueTest extends PluggableProcessEngineTestCase {

  protected static final int EXPECTED_DEFAULT_PRIORITY = 0;

  protected void setUp() throws Exception {
    processEngineConfiguration.setProducePrioritizedJobs(true);
    processEngineConfiguration.setJobPriorityProvider(new DefaultJobPriorityProvider());
  }

  protected void tearDown() throws Exception {
    processEngineConfiguration.setProducePrioritizedJobs(false);
    processEngineConfiguration.setJobPriorityProvider(null);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioExpressionProcess.bpmn20.xml")
  public void testConstantValueExpressionPrioritization() {
    // when
    runtimeService
      .createProcessInstanceByKey("jobPrioExpressionProcess")
      .startBeforeActivity("task2")
      .execute();

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(15, job.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioExpressionProcess.bpmn20.xml")
  public void testConstantValueHashExpressionPrioritization() {
    // when
    runtimeService
      .createProcessInstanceByKey("jobPrioExpressionProcess")
      .startBeforeActivity("task4")
      .execute();

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(16, job.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioExpressionProcess.bpmn20.xml")
  public void testVariableValueExpressionPrioritization() {
    // when
    runtimeService
      .createProcessInstanceByKey("jobPrioExpressionProcess")
      .startBeforeActivity("task1")
      .setVariable("priority", 22)
      .execute();

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(22, job.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioExpressionProcess.bpmn20.xml")
  public void testVariableValueExpressionPrioritizationFailsWhenVariableMisses() {
    // when
    try {
      runtimeService
        .createProcessInstanceByKey("jobPrioExpressionProcess")
        .startBeforeActivity("task1")
        .execute();
      fail("this should not succeed since the priority variable is not defined");
    } catch (ProcessEngineException e) {

      assertTextPresentIgnoreCase("Unknown property used in expression: ${priority}. "
          + "Cause: Cannot resolve identifier 'priority'",
          e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioExpressionProcess.bpmn20.xml")
  public void testExecutionExpressionPrioritization() {
    // when
    runtimeService
      .createProcessInstanceByKey("jobPrioExpressionProcess")
      .startBeforeActivity("task1")
      .setVariable("priority", 25)
      .execute();

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(25, job.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioExpressionProcess.bpmn20.xml")
  public void testExpressionEvaluatesToNull() {
    // when
    try {
      runtimeService
        .createProcessInstanceByKey("jobPrioExpressionProcess")
        .startBeforeActivity("task3")
        .setVariable("priority", null)
        .execute();
      fail("this should not succeed since the priority variable is not defined");
    } catch (ProcessEngineException e) {
      assertTextPresentIgnoreCase("Priority value is not an Integer", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioExpressionProcess.bpmn20.xml")
  public void testExpressionEvaluatesToNonNumericalValue() {
    // when
    try {
      runtimeService
        .createProcessInstanceByKey("jobPrioExpressionProcess")
        .startBeforeActivity("task3")
        .setVariable("priority", "aNonNumericalVariableValue")
        .execute();
      fail("this should not succeed since the priority must be integer");
    } catch (ProcessEngineException e) {
      assertTextPresentIgnoreCase("Priority value is not an Integer", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioExpressionProcess.bpmn20.xml")
  public void testExpressionEvaluatesToNonIntegerValue() {
    // when
    try {
      runtimeService
        .createProcessInstanceByKey("jobPrioExpressionProcess")
        .startBeforeActivity("task3")
        .setVariable("priority", 4.2d)
        .execute();
      fail("this should not succeed since the priority must be integer");
    } catch (ProcessEngineException e) {
      assertTextPresentIgnoreCase("Priority value must be either Short, Integer, or Long in Integer range",
          e.getMessage());
    }

    try {
      runtimeService
        .createProcessInstanceByKey("jobPrioExpressionProcess")
        .startBeforeActivity("task3")
        .setVariable("priority", Long.MAX_VALUE)
        .execute();
      fail("this should not succeed since the priority must be integer");
    } catch (ProcessEngineException e) {
      assertTextPresentIgnoreCase("Priority value must be either Short, Integer, or Long in Integer range",
          e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioExpressionProcess.bpmn20.xml")
  public void testConcurrentLocalVariablesAreAccessible() {
    // when
    runtimeService
      .createProcessInstanceByKey("jobPrioExpressionProcess")
      .startBeforeActivity("task2")
      .startBeforeActivity("task1")
      .setVariableLocal("priority", 14) // this is a local variable on the
                                        // concurrent execution entering the activity
      .execute();

    // then
    Job job = managementService.createJobQuery().activityId("task1").singleResult();
    assertNotNull(job);
    assertEquals(14, job.getPriority());
  }

}
