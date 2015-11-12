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
package org.camunda.bpm.engine.test.concurrency;

import static org.camunda.bpm.engine.variable.Variables.createVariables;
import static org.camunda.bpm.model.bpmn.Bpmn.createExecutableProcess;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.CachedDbEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;

/**
 * thread1:
 *  t=1: fetch byte variable
 *  t=4: update byte variable value
 *
 * thread2:
 *  t=2: fetch and delete byte variable and entity
 *  t=3: commit transaction
 *
 * This test ensures that thread1's command fails with an OptimisticLockingException,
 * not with a NullPointerException or something in that direction.
 *
 * @author Thorben Lindhauer
 */
public class CompetingByteVariableAccessTest extends ConcurrencyTestCase {

  private ThreadControl asyncThread;

  public void testConcurrentVariableRemoval() {
    deployment(createExecutableProcess("test")
        .startEvent()
          .userTask()
        .endEvent()
        .done());

    final byte[] byteVar = "asd".getBytes();

    String pid = runtimeService.startProcessInstanceByKey("test", createVariables().putValue("byteVar", byteVar)).getId();

    // start a controlled Fetch and Update variable command
    asyncThread = executeControllableCommand(new FetchAndUpdateVariableCmd(pid, "byteVar", "bsd".getBytes()));

    asyncThread.waitForSync();

    // now delete the process instance, deleting the variable and its byte array entity
    runtimeService.deleteProcessInstance(pid, null);

    // make the second thread continue
    // => this will a flush the FetchVariableCmd Context.
    // if the flush performs an update to the variable, it will fail with an OLE
    asyncThread.reportInterrupts();
    asyncThread.waitUntilDone();

    Throwable exception = asyncThread.getException();
    assertNotNull(exception);
    assertTrue(exception instanceof OptimisticLockingException);


  }

  static class FetchAndUpdateVariableCmd extends ControllableCommand<Void> {

    protected String executionId;
    protected String varName;
    protected Object newValue;

    public FetchAndUpdateVariableCmd(String executionId, String varName, Object newValue) {
      this.executionId = executionId;
      this.varName = varName;
      this.newValue = newValue;
    }

    public Void execute(CommandContext commandContext) {

      ExecutionEntity execution = commandContext.getExecutionManager()
        .findExecutionById(executionId);

      // fetch the variable instance but not the value (make sure the byte array is lazily fetched)
      VariableInstanceEntity varInstance = (VariableInstanceEntity) execution.getVariableInstanceLocal(varName);
      String byteArrayValueId = varInstance.getByteArrayValueId();
      assertNotNull("Byte array id is expected to be not null", byteArrayValueId);

      CachedDbEntity cachedByteArray = commandContext.getDbEntityManager().getDbEntityCache()
        .getCachedEntity(ByteArrayEntity.class, byteArrayValueId);

      assertNull("Byte array is expected to be not fetched yet / lazily fetched.", cachedByteArray);

      monitor.sync();

      // now update the value
      execution.setVariableLocal(varInstance.getName(), newValue);

      return null;
    }

  }
}
