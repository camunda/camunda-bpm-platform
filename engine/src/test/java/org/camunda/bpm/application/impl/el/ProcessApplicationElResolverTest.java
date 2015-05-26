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
package org.camunda.bpm.application.impl.el;

import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.Ignore;

/**
 * @author Thorben Lindhauer
 *
 */
@Ignore
public class ProcessApplicationElResolverTest extends PluggableProcessEngineTestCase {

  RuntimeContainerDelegate runtimeContainerDelegate = null;

  CallingProcessApplication callingApp;
  CalledProcessApplication calledApp;

  protected void setUp() throws Exception {
    runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
    runtimeContainerDelegate.registerProcessEngine(processEngine);

    callingApp = new CallingProcessApplication();
    calledApp = new CalledProcessApplication();

    callingApp.deploy();
    calledApp.deploy();
  }

  public void tearDown() {

    callingApp.undeploy();
    calledApp.undeploy();

    if (runtimeContainerDelegate != null) {
      runtimeContainerDelegate.unregisterProcessEngine(processEngine);
    }
  }

  /**
   * Note: please remove the @Ignore annotation of the class if at least one test is inside this class
   */
  public void FAILING_testCallActivityOutputExpression() {
    // given an instance of the calling process that calls the called process
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("callingProcess");

    // when the called process is completed
    Task calledProcessTask = taskService.createTaskQuery().singleResult();
    taskService.complete(calledProcessTask.getId());

    // then the output mapping should have successfully resolved the expression
    String outVariable = (String) runtimeService.getVariable(instance.getId(), "outVar");
    assertEquals(CallingProcessApplication.STRING_VARIABLE_VALUE, outVariable);
  }
}
