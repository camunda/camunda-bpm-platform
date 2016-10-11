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
package org.camunda.bpm.engine.impl.batch;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ConstantValueProvider;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;


public class BatchJobDeclaration extends JobDeclaration<BatchJobContext, MessageEntity> {

  public BatchJobDeclaration(String jobHandlerType) {
    super(jobHandlerType);
  }

  @Override
  protected ExecutionEntity resolveExecution(BatchJobContext context) {
    return null;
  }

  @Override
  protected MessageEntity newJobInstance(BatchJobContext context) {
    return new MessageEntity();
  }

  @Override
  protected JobHandlerConfiguration resolveJobHandlerConfiguration(BatchJobContext context) {
    return new BatchJobConfiguration(context.getConfiguration().getId());
  }

  @Override
  protected String resolveJobDefinitionId(BatchJobContext context) {
    return context.getBatch().getBatchJobDefinitionId();
  }

  public ParameterValueProvider getJobPriorityProvider() {
    long batchJobPriority = Context.getProcessEngineConfiguration()
        .getBatchJobPriority();
    return new ConstantValueProvider(batchJobPriority);
  }

}
