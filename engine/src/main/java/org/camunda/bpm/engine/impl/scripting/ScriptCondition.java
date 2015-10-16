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

package org.camunda.bpm.engine.impl.scripting;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureInstanceOf;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.Condition;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.delegate.ScriptInvocation;

/**
 * A {@link Condition} which invokes a {@link ExecutableScript} when evaluated.
 *
 * @author Sebastian Menski
 */
public class ScriptCondition implements Condition {

  protected final ExecutableScript script;

  public ScriptCondition(ExecutableScript script) {
    this.script = script;
  }

  public boolean evaluate(DelegateExecution execution) {
    ScriptInvocation invocation = new ScriptInvocation(script, execution);
    try {
      Context
        .getProcessEngineConfiguration()
        .getDelegateInterceptor()
        .handleInvocation(invocation);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new ProcessEngineException(e);
    }

    Object result = invocation.getInvocationResult();

    if (result instanceof DmnDecisionResult) {
      result = getDecisionResult((DmnDecisionResult) result);
    }

    ensureNotNull("condition script returns null", "result", result);
    ensureInstanceOf("condition script returns non-Boolean", "result", result, Boolean.class);

    return (Boolean) result;
  }

  public ExecutableScript getScript() {
    return script;
  }

  public Object  getDecisionResult(DmnDecisionResult decisionResult) {
    if (decisionResult.size() == 1) {
      DmnDecisionOutput decisionOutput = decisionResult.getSingleOutput();
      if (decisionOutput.size() == 1) {
        return decisionOutput.getSingleValue();
      }
      else {
        throw new ProcessEngineException("Condition decision does not return single output. Got: " + decisionResult);
      }
    }
    else {
      throw new ProcessEngineException("Condition decision does not return single result. Got: " + decisionResult);
    }
  }

}
