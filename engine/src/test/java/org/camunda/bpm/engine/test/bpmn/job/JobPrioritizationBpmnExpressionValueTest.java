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
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobPriorityProvider;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class JobPrioritizationBpmnExpressionValueTest extends PluggableProcessEngineTestCase {

  protected static final long EXPECTED_DEFAULT_PRIORITY = 123;
  protected static final long EXPECTED_DEFAULT_PRIORITY_ON_RESOLUTION_FAILURE = 296;

  protected long originalDefaultPriority;
  protected long originalDefaultPriorityOnFailure;

  protected void setUp() throws Exception {
    originalDefaultPriority = DefaultJobPriorityProvider.DEFAULT_PRIORITY;
    originalDefaultPriorityOnFailure = DefaultJobPriorityProvider.DEFAULT_PRIORITY_ON_RESOLUTION_FAILURE;

    DefaultJobPriorityProvider.DEFAULT_PRIORITY = EXPECTED_DEFAULT_PRIORITY;
    DefaultJobPriorityProvider.DEFAULT_PRIORITY_ON_RESOLUTION_FAILURE = EXPECTED_DEFAULT_PRIORITY_ON_RESOLUTION_FAILURE;
  }

  protected void tearDown() throws Exception {
    // reset default priorities
    DefaultJobPriorityProvider.DEFAULT_PRIORITY = originalDefaultPriority;
    DefaultJobPriorityProvider.DEFAULT_PRIORITY_ON_RESOLUTION_FAILURE = originalDefaultPriorityOnFailure;
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

  /**
   * Can't distinguish this case from the cases we have to tolerate due to CAM-4207
   */
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioExpressionProcess.bpmn20.xml")
  public void FAILING_testVariableValueExpressionPrioritizationFailsWhenVariableMisses() {
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
      assertTextPresentIgnoreCase("Priority value must be either Short, Integer, or Long",
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

  /**
   * This test case asserts that a non-resolving expression does not fail job creation;
   * This is a unit test scenario, where simply the variable misses (in general a human-made error), but
   * the actual case covered by the behavior are missing beans (e.g. in the case the engine can't perform a
   * context switch)
   */
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioExpressionProcess.bpmn20.xml")
  public void testDefaultPriorityWhenBeanMisses() {
    // creating a job with a priority that can't be resolved does not fail entirely but uses a default priority
    runtimeService
      .createProcessInstanceByKey("jobPrioExpressionProcess")
      .startBeforeActivity("task1")
      .execute();

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertEquals(EXPECTED_DEFAULT_PRIORITY_ON_RESOLUTION_FAILURE, job.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioExpressionProcess.bpmn20.xml")
  public void testDisableGracefulDegradation() {
    try {
      processEngineConfiguration.setEnableGracefulDegradationOnContextSwitchFailure(false);

      try {
        runtimeService
          .createProcessInstanceByKey("jobPrioExpressionProcess")
          .startBeforeActivity("task1")
          .execute();
        fail("should not succeed due to missing variable");
      } catch (ProcessEngineException e) {
        assertTextPresentIgnoreCase("unknown property used in expression", e.getMessage());
      }

    } finally {
      processEngineConfiguration.setEnableGracefulDegradationOnContextSwitchFailure(true);
    }
  }

  public void testDefaultEngineConfigurationSetting() {
    ProcessEngineConfigurationImpl config = new StandaloneInMemProcessEngineConfiguration();

    assertTrue(config.isEnableGracefulDegradationOnContextSwitchFailure());
  }

}
