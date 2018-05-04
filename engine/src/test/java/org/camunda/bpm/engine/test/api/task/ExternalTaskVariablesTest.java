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

package org.camunda.bpm.engine.test.api.task;

import java.util.List;

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;


/**
 * @author Mike Shauneu
 */
public class ExternalTaskVariablesTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/task/ExternalTaskVariablesTest.testExternalTaskVariablesLocal.bpmn20.xml"})
  public void testExternalTaskVariablesLocal() {

	VariableMap globalVars = Variables.putValue("globalVar", "globalVal");

    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess", globalVars).getId();

    final String workerId = "workerId";
    final String topicName = "testTopic";

	List<LockedExternalTask> lockedExternalTasks = externalTaskService.fetchAndLock(10, workerId)
			.topic(topicName, 60_000)
			.execute();

	assertEquals(1, lockedExternalTasks.size());

	LockedExternalTask lockedExternalTask = lockedExternalTasks.get(0);
	VariableMap variables = lockedExternalTask.getVariables();
	assertEquals(2, variables.size());
	assertEquals("globalVal", variables.getValue("globalVar", String.class));
	assertEquals("localVal", variables.getValue("localVar", String.class));

	externalTaskService.unlock(lockedExternalTask.getId());

	lockedExternalTasks = externalTaskService.fetchAndLock(10, workerId)
			.topic(topicName, 60_000)
			.localVariables(true)
			.execute();

	assertEquals(1, lockedExternalTasks.size());

	lockedExternalTask = lockedExternalTasks.get(0);
	variables = lockedExternalTask.getVariables();
	assertEquals(1, variables.size());
	assertEquals("localVal", variables.getValue("localVar", String.class));
  }

}