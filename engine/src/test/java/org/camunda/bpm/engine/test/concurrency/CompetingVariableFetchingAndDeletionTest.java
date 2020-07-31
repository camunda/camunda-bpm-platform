/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.test.concurrency;

import static org.camunda.bpm.engine.variable.Variables.createVariables;
import static org.camunda.bpm.model.bpmn.Bpmn.createExecutableProcess;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.impl.db.entitymanager.cache.CachedDbEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.junit.Test;

/**
 * This test makes sure that if one thread loads a variable
 * of type object, it does not fail with a OLE if the variable is also
 * concurrently deleted.
 *
 * Some context: this failed if the variable instance entity was first loaded,
 * and, before loading the byte array, both the variable and the byte array were
 * deleted by a concurrent transaction AND that transaction was comitted, before
 * the bytearray was loaded.
 * => loading the byte array returned null which triggered setting to null the
 * byteArrayId value on the VariableInstanceEntity which in turn triggered an
 * update of the variable instance entity itself which failed with OLE because
 * the VariableInstanceEntity was already deleted.
 *
 * +
 * |
 * |    Test Thread           Async Thread
 * |   +-----------+         +------------+
 * |      start PI
 * |      (with var)               +
 * |         +                     |
 * |         |                     v
 * |         |                 fetch VarInst
 * |         |                 (not byte array)
 * |         |                     +
 * |         v                     |
 * |      delete PI                |
 * |         +                     v
 * |         |                 fetch byte array (=>null)
 * |         |                     +
 * |         |                     |
 * |         |                     v
 * |         |                 flush()
 * |         |                 (this must not perform
 * |         v                 update to VarInst)
 * v  time

 *
 * @author Daniel Meyer
 *
 */
public class CompetingVariableFetchingAndDeletionTest extends ConcurrencyTestCase {

  private ThreadControl asyncThread;

  @Test
  public void testConcurrentFetchAndDelete() {

   testRule.deploy(createExecutableProcess("test")
        .startEvent()
          .userTask()
        .endEvent()
        .done());

    final List<String> listVar = Arrays.asList("a", "b");
    String pid = runtimeService.startProcessInstanceByKey("test", createVariables().putValue("listVar", listVar)).getId();

    // start a controlled Fetch variable command
    asyncThread = executeControllableCommand(new FetchVariableCmd(pid, "listVar"));

    // wait for async thread to load the variable (but not the byte array)
    asyncThread.waitForSync();

    // now delete the process instance
    runtimeService.deleteProcessInstance(pid, null);

    // make the second thread continue
    // => this will a flush the FetchVariableCmd Context.
    // if the flush performs an update to the variable, it will fail with an OLE
    asyncThread.makeContinue();
    asyncThread.waitUntilDone();

  }

  static class FetchVariableCmd extends ControllableCommand<Void> {

    protected String executionId;
    protected String varName;

    public FetchVariableCmd(String executionId, String varName) {
      this.executionId = executionId;
      this.varName = varName;
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

      // now trigger the fetching of the byte array
      Object value = varInstance.getValue();
      assertNull("Expecting the value to be null (deleted)", value);

      return null;
    }

  }

}
