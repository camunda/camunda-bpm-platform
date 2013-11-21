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

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;

/**
 * <p>Declaration of a Message Job (Asynchronous continuation job)</p>
 *
 * @author Daniel Meyer
 *
 */
public class MessageJobDeclaration extends JobDeclaration<MessageEntity> {

  public MessageJobDeclaration() {
    super(AsyncContinuationJobHandler.TYPE);
  }

  private static final long serialVersionUID = 1L;

  protected MessageEntity newJobInstance(ExecutionEntity execution) {
    MessageEntity message = new MessageEntity();
    message.setExecution(execution);
    message.setExclusive(execution.getActivity().isExclusive());
    message.setJobHandlerType(AsyncContinuationJobHandler.TYPE);
    return message;
  }


}
