/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.cdi.test.impl.el;

import org.camunda.bpm.engine.cdi.test.CdiProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.camunda.bpm.engine.cdi.test.impl.el.beans.CdiTaskListenerBean.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Sebastian Menski
 */
public class TaskListenerInvocationTest extends CdiProcessEngineTestCase {

  @Test
  @Deployment
  public void test() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(VARIABLE_NAME, INITIAL_VALUE);

    runtimeService.startProcessInstanceByKey("process", variables);

    Task task = taskService.createTaskQuery().singleResult();
    taskService.setAssignee(task.getId(), "demo");

    assertEquals(UPDATED_VALUE, taskService.getVariable(task.getId(), VARIABLE_NAME));
  }
}
