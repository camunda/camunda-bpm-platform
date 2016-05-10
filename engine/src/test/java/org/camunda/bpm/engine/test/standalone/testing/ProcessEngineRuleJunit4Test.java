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

package org.camunda.bpm.engine.test.standalone.testing;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;


/**
 * Test runners follow the this rule:
 *   - if the class extends Testcase, run as Junit 3
 *   - otherwise use Junit 4
 *
 * So this test can be included in the regular test suite without problems.
 *
 * @author Joram Barrez
 */
public class ProcessEngineRuleJunit4Test {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  @Test
  @Deployment
  public void ruleUsageExample() {
    RuntimeService runtimeService = engineRule.getRuntimeService();
    runtimeService.startProcessInstanceByKey("ruleUsage");

    TaskService taskService = engineRule.getTaskService();
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("My Task", task.getName());

    taskService.complete(task.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  /**
   * The rule should work with tests that have no deployment annotation
   */
  @Test
  public void testWithoutDeploymentAnnotation() {
    assertEquals("aString", "aString");
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void requiredHistoryLevelAudit() {

    assertThat(currentHistoryLevel(),
        CoreMatchers.<String>either(is(ProcessEngineConfiguration.HISTORY_AUDIT))
        .or(is(ProcessEngineConfiguration.HISTORY_FULL)));
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void requiredHistoryLevelActivity() {

    assertThat(currentHistoryLevel(),
        CoreMatchers.<String>either(is(ProcessEngineConfiguration.HISTORY_ACTIVITY))
        .or(is(ProcessEngineConfiguration.HISTORY_AUDIT))
        .or(is(ProcessEngineConfiguration.HISTORY_FULL)));
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void requiredHistoryLevelFull() {

    assertThat(currentHistoryLevel(), is(ProcessEngineConfiguration.HISTORY_FULL));
  }

  protected String currentHistoryLevel() {
    return engineRule.getProcessEngine().getProcessEngineConfiguration().getHistory();
  }

}
