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
package org.camunda.bpm.engine.test.bpmn.async;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class AsyncCallActivityTest extends PluggableProcessEngineTestCase {


  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/AsyncCallActivityTest.asyncStartEvent.bpmn20.xml",
  "org/camunda/bpm/engine/test/bpmn/async/AsyncCallActivityTest.testCallSubProcess.bpmn20.xml" })
  public void testCallProcessWithAsyncOnStartEvent() {

    runtimeService.startProcessInstanceByKey("callAsyncSubProcess");

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    managementService.executeJob(job.getId());

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

  }
}
