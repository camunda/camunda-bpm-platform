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
package org.camunda.bpm.engine.impl.delegate;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.impl.interceptor.DelegateInterceptor;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;

/**
 * Provides context about the invocation of usercode and handles the actual
 * invocation
 *
 * @author Daniel Meyer
 * @see DelegateInterceptor
 */
public abstract class DelegateInvocation {

  protected Object invocationResult;
  protected BaseDelegateExecution contextExecution;
  protected ResourceDefinitionEntity contextResource;

  /**
   * Provide a context execution or resource definition in which context the invocation
   *   should be performed. If both parameters are null, the invocation is performed in the
   *   current context.
   *
   * @param contextExecution set to an execution
   */
  public DelegateInvocation(BaseDelegateExecution contextExecution, ResourceDefinitionEntity contextResource) {
    // This constructor forces sub classes to call it, thereby making it more visible
    // whether a context switch is going to be performed for them.
    this.contextExecution = contextExecution;
    this.contextResource = contextResource;
  }

  /**
   * make the invocation proceed, performing the actual invocation of the user
   * code.
   *
   * @throws Exception
   *           the exception thrown by the user code
   */
  public void proceed() throws Exception {
    invoke();
  }

  protected abstract void invoke() throws Exception;

  /**
   * @return the result of the invocation (can be null if the invocation does
   *         not return a result)
   */
  public Object getInvocationResult() {
    return invocationResult;
  }

  /**
   * returns the execution in which context this delegate is invoked. may be null
   */
  public BaseDelegateExecution getContextExecution() {
    return contextExecution;
  }

  public ResourceDefinitionEntity getContextResource() {
    return contextResource;
  }
}
