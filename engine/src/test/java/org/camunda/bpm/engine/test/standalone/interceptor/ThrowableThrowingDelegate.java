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
package org.camunda.bpm.engine.test.standalone.interceptor;

import javax.script.ScriptEngine;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;

public class ThrowableThrowingDelegate implements JavaDelegate {

  protected boolean executed;

  public ThrowableThrowingDelegate() {
    executed = false;
  }

  public void execute(DelegateExecution delegateExecution) throws Exception {
    ExecutionEntity executionEntity = new ExecutionEntity();
    executionEntity.setId("foo");
    Context.getCommandContext().getDbEntityManager().insert(executionEntity);
    executed = true;
    throw new Error();

  }


  protected static void rethrowUnchecked(Throwable ex) {
    rethrow(ex);
  }

  protected static void rethrow(Throwable t) {
    throw (RuntimeException) t;
  }
}