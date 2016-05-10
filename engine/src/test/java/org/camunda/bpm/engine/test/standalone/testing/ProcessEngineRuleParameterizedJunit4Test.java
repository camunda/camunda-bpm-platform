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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


/**
 * @author Thorben Lindhauer
 */
@RunWith(Parameterized.class)
public class ProcessEngineRuleParameterizedJunit4Test {

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
      { 1 }, { 2 }
    });
  }

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  public ProcessEngineRuleParameterizedJunit4Test(int parameter) {

  }

  /**
   * Unnamed @Deployment annotations don't work with parameterized Unit tests
   */
  @Ignore
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

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/standalone/testing/ProcessEngineRuleParameterizedJunit4Test.ruleUsageExample.bpmn20.xml")
  public void ruleUsageExampleWithNamedAnnotation() {
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

}
