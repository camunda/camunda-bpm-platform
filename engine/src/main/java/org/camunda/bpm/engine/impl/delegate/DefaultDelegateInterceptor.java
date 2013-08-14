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
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.interceptor.DelegateInterceptor;

/**
 * Default implementation, simply proceeding the call. 
 * 
 * @author Daniel Meyer
 */
public class DefaultDelegateInterceptor implements DelegateInterceptor {

  public void handleInvocation(final DelegateInvocation invocation) throws Exception {
    ProcessApplicationReference processApplication = 
        ProcessApplicationContextUtil.getTargetProcessApplication(invocation.getContextExecution());
    
    if (processApplication != null && ProcessApplicationContextUtil.requiresContextSwitch(processApplication)) {
      Context.executeWithinProcessApplication(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          handleInvocation(invocation);
          return null;
        }
      }, processApplication);
    } else {
      invocation.proceed();
    }
  }

}
