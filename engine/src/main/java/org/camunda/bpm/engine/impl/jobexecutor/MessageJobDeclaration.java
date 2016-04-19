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
package org.camunda.bpm.engine.impl.jobexecutor;

import org.camunda.bpm.engine.impl.interceptor.AtomicOperationInvocation;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler.AsyncContinuationConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.runtime.AtomicOperation;

/**
 * <p>Declaration of a Message Job (Asynchronous continuation job)</p>
 *
 * @author Daniel Meyer
 *
 */
public class MessageJobDeclaration extends JobDeclaration<AtomicOperationInvocation, MessageEntity> {

  public static final String ASYNC_BEFORE = "async-before";
  public static final String ASYNC_AFTER = "async-after";

  private static final long serialVersionUID = 1L;

  protected String[] operationIdentifier;

  public MessageJobDeclaration(String[] operationsIdentifier) {
    super(AsyncContinuationJobHandler.TYPE);
    this.operationIdentifier = operationsIdentifier;
  }

  @Override
  protected MessageEntity newJobInstance(AtomicOperationInvocation context) {
    MessageEntity message = new MessageEntity();
    message.setExecution(context.getExecution());

    return message;
  }

  public boolean isApplicableForOperation(AtomicOperation operation) {
    for (String identifier : operationIdentifier) {
      if (operation.getCanonicalName().equals(identifier)) {
        return true;
      }
    }
    return false;
  }

  protected ExecutionEntity resolveExecution(AtomicOperationInvocation context) {
    return context.getExecution();
  }

  @Override
  protected JobHandlerConfiguration resolveJobHandlerConfiguration(AtomicOperationInvocation context) {
    AsyncContinuationConfiguration configuration = new AsyncContinuationConfiguration();

    configuration.setAtomicOperation(context.getOperation().getCanonicalName());

    ExecutionEntity execution = context.getExecution();
    PvmActivity activity = execution.getActivity();
    if(activity != null && activity.isAsyncAfter()) {
      if(execution.getTransition() != null) {
        // store id of selected transition in case this is async after.
        // id is not serialized with the execution -> we need to remember it as
        // job handler configuration.
        configuration.setTransitionId(execution.getTransition().getId());
      }
    }

    return configuration;
  }
  
}
