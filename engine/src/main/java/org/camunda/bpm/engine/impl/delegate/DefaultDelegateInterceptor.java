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

import java.util.concurrent.Callable;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.invocation.DecisionInvocation;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.DelegateInterceptor;
import org.camunda.bpm.engine.repository.DecisionDefinition;

/**
 * The default implementation of the DelegateInterceptor.
 *<p/>
 * This implementation has the following features:
 * <ul>
 * <li>it performs context switch into the target process application (if applicable)</li>
 * <li>it checks autorizations if {@link ProcessEngineConfigurationImpl#isAuthorizationEnabledForCustomCode()} is true</li>
 * </ul>
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 */
public class DefaultDelegateInterceptor implements DelegateInterceptor {

  public void handleInvocation(final DelegateInvocation invocation) throws Exception {

    final ProcessApplicationReference processApplication = getProcessApplicationForInvocation(invocation);

    if (processApplication != null && ProcessApplicationContextUtil.requiresContextSwitch(processApplication)) {
      Context.executeWithinProcessApplication(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          handleInvocation(invocation);
          return null;
        }
      }, processApplication);
    }
    else {
      handleInvocationInContext(invocation);
    }

  }

  protected void handleInvocationInContext(final DelegateInvocation invocation) throws Exception {
    CommandContext commandContext = Context.getCommandContext();
    boolean oldValue = commandContext.isAuthorizationCheckEnabled();

    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    if (!configuration.isAuthorizationEnabledForCustomCode()) {
      // the custom code should be executed without authorization
      commandContext.disableAuthorizationCheck();
    }

    try {
      invocation.proceed();
    }
    finally {
      if (oldValue) {
        // the last "one" set the flag back to true
        commandContext.enableAuthorizationCheck();
      }
    }
  }

  protected ProcessApplicationReference getProcessApplicationForInvocation(final DelegateInvocation invocation) {
    if (invocation instanceof DecisionInvocation) {
      DecisionInvocation decisionInvocation = (DecisionInvocation) invocation;
      DecisionDefinition decisionDefinition = decisionInvocation.getDecisionDefinition();
      return ProcessApplicationContextUtil.getTargetProcessApplication((DecisionDefinitionEntity) decisionDefinition);

    }
    else {
      BaseDelegateExecution contextExecution = invocation.getContextExecution();
      return ProcessApplicationContextUtil.getTargetProcessApplication((CoreExecution) contextExecution);
    }
  }

}
